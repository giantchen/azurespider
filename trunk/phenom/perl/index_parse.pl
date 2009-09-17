#!/usr/bin/perl
use warnings;
use strict;
use HTML::TreeBuilder;
use Data::Dumper;
use JSON;
use HTTP::Request;
use LWP::UserAgent;
use Spreadsheet::ParseExcel;

$| = 1;

# Main
my %code_map = (
    shanghai => 'sh',
    shenzhen => 'sz',
    hkg => 'hk');

## Zhong Zhen Index Company
my $codes = getZZIndexCodes();
my $code_xls = getZZXLS($codes);
parseZZXLS($code_xls);


## Shanghai Stock Exchange
 getSHAIndex();

## ShenZheng Stock Exchange
getSZAIndex();

#############################################
# Processing the index data from Zhong Zhen #
#############################################
# Parse Excel files
sub parseZZXLS {
    my $code_xls = shift;

    for my $code (keys %$code_xls) {
	my $file = $code_xls->{$code};
	print STDERR "processing $file\n";
	my $parser  = Spreadsheet::ParseExcel->new();
	my $workbook = $parser->Parse($file);

	for my $worksheet ($workbook->worksheets()) {
	    my $cell = $worksheet->get_cell(0, 0);
	    if (not $cell->value =~ /Code/) {
		print STDERR "$code is not an index xls\n";
		next;

	    }

	    my ($row_min, $row_max) = $worksheet->row_range();
	    my ( $col_min, $col_max) = $worksheet->col_range();

	    for my $row ($row_min + 1 .. $row_max) {
		my $stock_code = $worksheet->get_cell($row, 0);
		my $exchange = $worksheet->get_cell($row, 3);
		next unless $stock_code && $exchange;
		my $ex = $code_map{lc($exchange->value)};
		my $name = $codes->{$code};
		my $sh = "";
		$sh = ".sh" if $code !~ /[^0-9]/;
		if ($ex eq 'hk') {
		    print "$code$sh,$name,", $stock_code->value, ",", $ex, "\n";
		} else {
		    print "$code$sh,$name,", $stock_code->value, ".$ex,", $ex, "\n";
		}
	    }
	}
    }
}

# Get the xls files
sub getZZXLS {
    my $codes = shift;
    my %ret;
    for my $code (keys %$codes) {
	my $url = "http://www.csindex.com.cn/sseportal/csiportal/zs/jbxx/report.do?code=$code&&subdir=5";
	my $file = "report.do?code=$code&&subdir=5";
	qx(wget "$url") unless -e $file;
	my $parser = HTML::TreeBuilder->new();
	$parser->parse_file($file);
	for my $href ($parser->find_by_tag_name("a")) {
	    next unless $href->{href} =~ /xls/i;
	    my $xls = $href->{href};
	    my $xls_file = $xls;
	    $xls_file =~ s|.*/||;
	    my $xls_url = "http://www.csindex.com.cn$xls";
	    qx(wget '$xls_url') unless -e $xls_file;
            $ret{$code} = $xls_file;
	}
	$parser->delete;
	undef $parser;
    }
    return \%ret;
}

# Get the index code from the drop down list
sub getZZIndexCodes {
    my $url = 'http://www.csindex.com.cn/sseportal/csiportal/zs/jbxx/report.do?code=000300&&subdir=1';
    my $file = 'report.do?code=000300&&subdir=1';
    my %ret;
    # unlink $file;
    qx(wget '$url') unless -e $file;
    my $parser = HTML::TreeBuilder->new();
    $parser->parse_file($file);
    for my $select ($parser->find_by_tag_name("select")) {
	for my $option ($select->find_by_tag_name('option')) {
	    my $value = $option->{value};
	    my $name = $option->as_trimmed_text;
	    $value =~ /code=(.*?)&/;
	    $ret{$1} = $name;
	}
    }
    $parser->delete;
    undef $parser;
    return \%ret;
}


#########################################################
# Procesing the index data from Shanghai Stock Exchange #
#########################################################
sub getSHAIndex {
    my $start_page = 'http://www.sse.com.cn/sseportal/webapp/datapresent/queryindexcnp?indexCode=000010';

    my $start_file = 'queryindexcnp?indexCode=000010';
    my %codes;
    qx(wget '$start_page');
    my $parser = HTML::TreeBuilder->new();
    $parser->parse_file($start_file);
    for my $select ($parser->find_by_tag_name("select")) {
	for my $option ($select->find_by_tag_name('option')) {
	    my $code = $option->{value};
	    my $name = $option->as_trimmed_text;
	    print STDERR "code = $code name = $name\n";
	    $codes{$code} = $name;
	}
    }
    
    $parser->delete;
    print Dumper(\%codes), "\n";
    for my $code (keys %codes) {
	print STDERR "processing $code\n";
	my $url = "http://www.sse.com.cn/sseportal/webapp/datapresent/queryindexcnp?indexCode=$code";
	my $file = "queryindexcnp?indexCode=$code";
	qx(wget '$url') unless -e $file;
	my $parser = HTML::TreeBuilder->new();
	$parser->parse_file($file);
	for my $table ($parser->find_by_tag_name('table')) {
	    next unless exists $table->{class} && $table->{class} eq 'content';
	    for my $href ($table->find_by_tag_name('a')) {
		my $name = $href->as_trimmed_text;
		$code =~ s/h/0/i;
		if ($name =~ /(.*?)\s+\((.*?)\)/) {
		    print "$code.sh,$1,$2.sh,sh\n";
		}
	    }
	}
	$parser->delete;
    }
}


###########################################################
# Processing the index data from ShenZheng Stock Exchange #
###########################################################
sub getSZAIndex {
    my @codes = qw(399001 399002 399003 399004 399005 399100 399101 399106 399107 399108 399110 399120 399130 399131 399132 399133 399134 399135 399136 399137 399138 399139 399140 399150 399160 399170 399180 399190 399200 399210 399220 399230 399300 399305);

    for my $code (@codes) {
	my $url = "http://www.szse.cn/szseWeb/FrontController.szse?ACTIONID=7&CATALOGID=1747&TABKEY=tab1&ZSDM=$code";	
	my $file = "FrontController.szse?ACTIONID=7&CATALOGID=1747&TABKEY=tab1&ZSDM=$code";
	qx(wget '$url') unless -e $file;
	my $parser = HTML::TreeBuilder->new();
        $parser->parse_file($file);

	# Get the index name
	my $indexName = "";
	for my $span ($parser->find_by_tag_name('span')) {
	    next unless exists $span->{class} && $span->{class} eq 'cls-subtitle';
	    $indexName = $span->as_trimmed_text;
	    $indexName =~ s/\d+\s(.*?)\s+.*/$1/;
	}

	for my $table ($parser->find_by_tag_name('table')) {
	    next unless exists $table->{id} && $table->{id} eq 'REPORTID_tab1';
	    for my $tr ($table->find_by_tag_name('tr')) {
		my @tds = $tr->find_by_tag_name('td');
		print "$code.sz,$indexName,", $tds[0]->as_trimmed_text, ".sz,sz\n";
	    }
	}
    }
}
