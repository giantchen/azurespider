#!/usr/bin/perl
use warnings;
use strict;
use HTML::TreeBuilder;
# use encoding "gb2312";
# binmode(STDOUT, ":encoding(gb2312)");
use Encode;

$| = 1;
# print header
print "Stock,", encode('gb2312', decode('utf8',"截止日期,公告日期,每股净资产,每股收益,每股现金含量,每股资本公积金,固定资产合计,流动资产合计,资产总计,长期负债合计,主营业务收入,财务费用,净利润\n"));

while (<>) {
    chomp;
    my $exchange = $_;
    my $stock_id = $exchange;
    $exchange =~ s/\d+//g;
    $stock_id =~ s/[^\d]//g;

    print STDERR "exchange = $exchange, stock = $stock_id\n";    
    my $url = "http://money.finance.sina.com.cn/corp/go.php/vFD_FinanceSummary/stockid/$stock_id.phtml";
    my $file = "$stock_id.phtml";
    qx(wget "$url") unless -e $file;

    my $parser = HTML::TreeBuilder->new();
    $parser->parse_file($file);
    my $stockcode = getStockCode($parser);
    next unless $exchange.$stock_id eq $stockcode;

    # get the data from the page
    for my $table ($parser->find_by_tag_name("table")) {
	if ($table->id && $table->id eq "FundHoldSharesTable") {
	    # Finance data
	    my $count = 0;
	    print "$stockcode,";
	    for my $tr ($table->find_by_tag_name('tr')) {
		my @tds = $tr->find_by_tag_name('td');
		next unless scalar @tds >= 2;
		my $column = $tds[0]->as_trimmed_text;
		my $value = $tds[1]->as_trimmed_text;
		$value =~ s/[^0-9\.\-]//g;
		$value = 0 unless $value;
		print "$value,";
		print "\n$stockcode," if ++$count % 13 == 0;
		print STDERR "[$column]=[$value]\n";
		print STDERR "\n\n" if $count % 13 == 0;
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
