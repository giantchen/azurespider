#!/usr/bin/perl
use warnings;
use strict;
use HTML::TreeBuilder;
use Data::Dumper;
use Encode;
use Thread::Pool;
use Thread::Semaphore; 
my $semaphore = new Thread::Semaphore; 

$| = 1;
open FH, "<stocklist" || die $!;
open BONUS, ">bonus.csv" || die $!;
print BONUS "#symbol,announce_date,bonus_stock,transit_stock,dividen,total_share,x_date,registration_date,list_date\n";
open ALLOC, ">alloc.csv" || die $!;
print ALLOC "#symbol,announce_date,alloc_stock,alloc_price,total_share,x_date,registration_date,paydate_start,paydate_end,list_date,amount\n";

while (<FH>) {
    chomp;
    print "$_\n";

    my $exchange = $_;
    my $stock_id = $exchange;
    $exchange =~ s/\d+//g;
    $stock_id =~ s/[^\d]//g;

    print STDERR "exchange = $exchange, stock = $stock_id\n";    
    my $url = "http://money.finance.sina.com.cn/corp/go.php/vISSUE_ShareBonus/stockid/$stock_id.phtml";
    my $file = "$stock_id.phtml";
    qx(wget "$url") unless -e $file;

    my $parser = HTML::TreeBuilder->new();
    $parser->parse_file($file);
    my $stockcode = getStockCode($parser);
    next unless $exchange.$stock_id eq $stockcode;

    # get the data from the page
    for my $table ($parser->find_by_tag_name("table")) {
	if ($table->id && $table->id eq "sharebonus_1") {
	    #  bonus
	    my $tbody = $table->find_by_tag_name('tbody');
	    for my $tr ($tbody->find_by_tag_name('tr')) {
		my @values;
		my $flag = 0;
		for my $td ($tr->find_by_tag_name('td')) {
		    my $val = $td->as_trimmed_text;
		    $val = "" unless $val =~ /[0-9a-zA-Z\.]/;
		    $val =~ s/[-|,]//g;
		    push @values, $val;
		    $flag = 1 if $val;
		}
		next unless $flag;
		$semaphore->down;
		print BONUS "$stock_id.$exchange,", join(",", @values), "\n";
		$semaphore->up;
	    }
	} elsif ($table->id && $table->id eq "sharebonus_2") {
	    # allocate stocks
	    my $tbody = $table->find_by_tag_name('tbody');
	    for my $tr ($tbody->find_by_tag_name('tr')) {
		my @values;
		my $flag = 0;
		for my $td ($tr->find_by_tag_name('td')) {
		    my $val = $td->as_trimmed_text;
		    $val = "" unless $val =~ /[0-9a-zA-Z\.]/;
		    $val =~ s/[-|,]//g;
		    push @values, $val;
		    $flag = 1 if $val;
		}
		next unless $flag;
		$semaphore->down;
		print ALLOC "$stock_id.$exchange,", join(",", @values), "\n";
		$semaphore->up;
	    }
	}
    }
    $parser->delete;
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
