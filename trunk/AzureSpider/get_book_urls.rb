#!/usr/bin/env ruby
require 'rubygems'
require 'hpricot'
require 'open-uri'

def get_book_url(path)
  book_types = {}
  Dir.glob("#{path}/01*.shtml").each do |fn|
    # puts fn
    fn =~ /01\.(\d+?)\./
    type_id = $1
    doc = Hpricot(open(fn))
    doc.search("a").each do |link|
      href = link['href']
      if href.include? 'product'
        puts href
        href =~ /(\d+)\D*$/
        book_id = $1
        book_types[book_id] = type_id
      end 
    end
  end
  
  # output the book type.
  File.open("book_types.txt", "w") do |type_file|
  	type_file.puts "book_id\ttype"
    book_types.each {|key, value| type_file.puts "#{key}\t#{value}"}
  end
end

get_book_url(ARGV[0] ? ARGV[0] : ".")