#!/usr/bin/env ruby
require 'rubygems'
require 'hpricot'
require 'open-uri'

def get_relatives(list_file)
  rels = {}
  total_books = 0
  cnt = 1

  File.open(list_file).each_line do |file|
  	file.chomp!
  	file =~ /(\d+)\D*$/
  	book_id = $1
  	# print "%s\t'%s'\n" % [file, book_id]
  	url = "http://product.dangdang.com/script/alsoBuy.aspx?product_id=" + book_id;
  	# puts url
  	text = open(url).gets(nil)
  	# puts text
  	if text
  	  rels[book_id] = []

      while (text =~ /product_id=(\d+)/)
  	    # printf("%d, ", $1)
  	    rels[book_id] << $1
  	    text = $'
      end

      printf("%s: %s\n", book_id, rels[book_id].join(", "))
    end

    total_books += 1
    if cnt == 100
      STDERR.print "#{total_books}, "
      cnt = 1
    else
      cnt += 1
    end
  end
end

get_relatives(ARGV[0])