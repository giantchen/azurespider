#!/usr/bin/env ruby
require 'rubygems'
require 'hpricot'
require 'open-uri'

# usage: ruby get_details.rb book_url_list_file

class Book
  
  @@regexps = {
    'title' => /productname="(.*)"/,
    'author' => /作者：<\/span><span class="black">(.*?)<\/a>/,
    'publisher' => /出版社：<\/span><a href='.*'>(.*?)<\/a>/,
    'isbn' => /ISBN：<\/span><span class="black">(.*?)<\/span>/,
    'publish_date' => /出版日期：<\/span><span class="black">(.*?)<\/span>/,
    'version' => /版次：<\/span><span class="black">(.*?)<\/span>/,
    'price' => /productstr1="定价：(.*?)元/,
    'site_price' => /productstr1=".*当当价：(.*?)元/,
    'vip_price' => /productstr1=".*钻石VIP价：(.*?)元/,
    'summary' => /内容提要：<\/td>\s*<\/tr>\s*<tr>\s*<td>(.*?)<\/td>/,
    'author_intro' => /作者简介：<\/td>\s*<\/tr>\s*<tr>\s*<td>(.*?)<\/td>/,
    # 'type_' => />><a href="\/class\/book\/.*shtml">(.*?)<\/a>/
  }
  
  @@filters = {
    'summary' => lambda {|text| unhtml(text)},
    'author_intro' => lambda {|text| unhtml(text)},
  }
  
  def initialize(file)
    @values = {}
    parse(file)
  end

  def to_s
    s = ''
    @values.sort.each {|x| s << x.join(": ") << "\n"}
    s
  end

  def method_missing(method_id)
    key = method_id.id2name
    @values.key?(key) ? @values[key] : nil
  end

 private

  def parse(file)
    text = open(file).gets(nil)
   	file =~ /product\/\d+\/(\d+)\.shtml/
  	@values['book_id'] = $1

    @@regexps.each do |key, re|
      if text =~ re
        if @@filters.key?(key)
          @values[key] = @@filters[key].call($1.strip)
        else
          @values[key] = $1.strip
        end
      end
    end
  end

  def Book.unhtml(s)
    if (s)
      s.gsub(/&nbsp;/, ' ')
      # s.gsub(/<br>/, "\n")
    else
      nil
    end
  end
end

def get_details(list_file)
  total_books = 0
  cnt = 1
  book_id_to_isbn = {}
  File.open(list_file).each_line do |file|
    file.chomp!.sub!(/^http:\/\//, '')
    if File.exist? file
      book = Book.new(file)
      total_books += 1
      puts file, book, ""
      book_id_to_isbn[book.book_id] = book.isbn

	  # show progress      
      if cnt == 1000
        STDERR.print "#{total_books}, "
        cnt = 1
      else
        cnt += 1
      end
    end
  end
  
  # output the book_id to isbn file.
  File.open("book_id_to_isbn.txt", "w") do |id_file|
  	id_file.puts "book_id\tISBN"
    book_id_to_isbn.each {|key, value| id_file.puts "#{key}\t#{value}"}
  end
end

get_details(ARGV[0])