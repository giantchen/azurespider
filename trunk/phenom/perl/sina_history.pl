#!/usr/bin/perl
use warnings;
use strict;
use HTML::TreeBuilder;
use Data::Dumper;
use Encode;
use Thread::Pool;
use Thread::Semaphore; 
use JSON;
use HTTP::Request;
use LWP::UserAgent;

my $semaphore = new Thread::Semaphore; 
my $today = `date +%Y-%m-%d`;
$| = 1;

my %date_map = (
		1 => {
		      start_date => '01-01',
		      end_date => '03-31',
		     },
		2 => {
		      start_date => '04-01',
		      end_date => '06-30',
		     },
		3 => {
		      start_date => '07-01',
		      end_date => '09-30',
		     },
		4 => {
		      start_date => '10-01',
		      end_date => '12-30',
		     }
	       );


# open FH, "<stocklist" || die $!;

my $pool = Thread::Pool->new(
			     {
			      optimize => 'cpu', # default: 'memory'
			      'do' => \&aciton,
			      frequency => 1000,
			      autoshutdown => 1, # default: 1 = yes
			      workers => 20,     # default: 1
			      maxjobs => 100, # default: 5 * workers
			      minjobs => 10,  # default: maxjobs / 2
			     }
			    );


while (<>) {
  chomp;
  $pool->job($_);
}
# $pool->job('sh510050');

sub process_fund_sohu {
  my ($stock_id, $exchange) = @_;
  my $ua = LWP::UserAgent->new;
  my $json = new JSON;

  for my $year (2000..2009) {
    for my $quater (1..4) {
      print STDERR "processing sohu fund data year=$year, quarter=$quater, stock=$stock_id\n";
      my $sohu_url = "http://q.stock.sohu.com/app2/history.up?method=history&code=cn_$stock_id&sd=$year-$date_map{$quater}->{start_date}&ed=$year-$date_map{$quater}->{end_date}&t=d&res=js";
      my $sohu_file = "sohu-$stock_id-$year-$quater.json";

      unless (-e $sohu_file && -s $sohu_file > 0 && $today le "$year-$date_map{$quater}->{end_date}") {
	# print STDERR $sohu_url, "\n";
	my $request = HTTP::Request->new(GET => $sohu_url);
	my $res = $ua->request($request) || die $!;
	open SOHU_FH, ">$sohu_file" || die $!;
	print SOHU_FH $res->content;
	close SOHU_FH;
      }
      open SOHU_FH, "<$sohu_file" || die $!;
      my $s = "";
      while (<SOHU_FH>) {
	# chomp;
	$s .= $_;
      }
      close SOHU_FH;
      $s =~ s/PEAK_ODIA\(//g;
      $s =~ s/\)//g;
      $s =~ s/\'/\"/g;

      my $obj = JSON->new->utf8(0)->decode($s) || die "data is not correct\n";
      next if (scalar @$obj < 2);
      $semaphore->down;
      for my $data (@{$obj->[1]}) {
	my $date = $data->[0];
	$date =~ s/-//g;
	# symbol, date, open, high, close, low, amount, volumn, exchange
	print Dumper($data), "\n";
	print "$stock_id.$exchange,$date,$data->[1],$data->[6],$data->[2],$data->[5],$data->[8], $data->[7]\n";
      }
      $semaphore->up;
    }
  }
}

sub aciton {
  my $exchange = shift;
  my $stock_id = $exchange;
  $exchange =~ s/\d+//g;
  $stock_id =~ s/[^\d]//g;

  print STDERR "exchange = $exchange, stock = $stock_id\n";
  my $found = 0;
  for my $year (2000..2009) {
    for my $quater (1..4) {
      my $url = "http://money.finance.sina.com.cn/corp/go.php/vMS_FuQuanMarketHistory/stockid/$stock_id.phtml?year=$year&jidu=$quater";
      my $file = "$stock_id.phtml?year=$year&jidu=$quater";
      qx(wget "$url") unless -e $file && -s $file > 0 && $today gt "$year-$date_map{$quater}->{end_date}";
      my $parser = HTML::TreeBuilder->new();
      $parser->parse_file($file);

      my $stockcode = getStockCode($parser);

      unless ($exchange.$stock_id eq $stockcode) {
	$url = "http://vip.stock.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/$stock_id/type/S.phtml?year=$year&jidu=$quater";
	$file = "S-$stock_id.phtml?year=$year&jidu=$quater";
	$semaphore->down;
	qx(wget "$url") unless -e $file &&  -s $file > 0;
	rename "S.phtml?year=$year&jidu=$quater", $file;
	$semaphore->up;
	$parser->delete;
	$parser = HTML::TreeBuilder->new();
	$parser->parse_file($file);
	$stockcode = getStockCode($parser);
	next unless $exchange.$stock_id eq $stockcode;
      }

      print STDERR "year=$year, quarter=$quater, stock=$stockcode\n";
      $found = 1;
      # get the data from the page
      for my $table ($parser->find_by_tag_name("table")) {
	next unless $table->id && $table->id eq "FundHoldSharesTable";
	for my $tr ($table->find_by_tag_name('tr')) {
	  # skip the <tr> without any data
	  my @values;
	  for my $div ($tr->find_by_tag_name('div')) {
	    next unless $div->as_trimmed_text =~ /\d/;
	    my $val = $div->as_trimmed_text;
	    $val =~ s/[-|,]//g;
	    push @values, $val;
	  }
	  next unless scalar @values > 0;
	  push @values, 1 if scalar @values == 7;
	  $semaphore->down;
	  my $s = "$stock_id.$exchange," . join(",", @values) . "\n";
	  print $s;
	  $semaphore->up;
	}
      }
      $parser->delete;
    }
  }

  process_fund_sohu($stock_id, $exchange) unless ($found);
}

sub getStockCode {
  my $parser = shift;
  my $full_stock_code = 0;
  for ($parser->find_by_tag_name("script")) {
    # retrieve the stock id from the page
    next unless $_ && $_->content;
    for my $script (@{$_->content}) {
      for (split /\n/, $script) {
	if ($_ =~ /.*fullcode=\"(.*)\"/) {
	  $full_stock_code = $1;
	  print STDERR "Resolved to [$full_stock_code]\n";
	  return $full_stock_code;
	}
      }
    }
  }
  return 0;
}
