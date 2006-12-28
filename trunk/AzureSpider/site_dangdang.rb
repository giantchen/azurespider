#!/usr/bin/env ruby
require 'rubygems'
require 'hpricot'
require 'open-uri'

SITE_URL = "http://www.dangdang.com"

def putu(url, text='')
  printf("%s%s\t%s\n", SITE_URL, url, text)
end

def get_all_txt_pages(txt_urls)
  txt_urls.each_key do |txturl|
    url = SITE_URL + txturl
    max = 0
    maxurl = ""
    fn_base = txturl.sub(".shtml", "")
    
    filename = "txt/" + File.basename(txturl)
    if File.exist? filename
      url = filename
    end
    puts url
    
    doc = Hpricot(open(url))
    doc.search("a").each do |link|
      if link.inner_html =~ /^\d+$/
        page = link['href'].sub(".shtml", "").split('_')[1].to_i
        if page > max
          max = page
          maxurl = link['href']
        end
        #puts link['href']# + ", " + link.inner_html
      elsif link['href'].include?(fn_base + "_") && link.inner_html != ">>"
        puts link['href'] + ", " + link.inner_html + "**"
      end
    end  

    (2..max).each do |page|
      puts SITE_URL + maxurl.sub("_#{max}", "_#{page}")
    end

  end
end

def get_base_txt_pages
  #doc = Hpricot(open("http://www.dangdang.com/zhuanti2006/book/2001.shtml"))
  txt_urls = Hash.new
  doc = Hpricot(open("2001.shtml"))
  doc.search("a[@href]").each do |link|
    href = link['href']
    if (href.include? '00.shtml') && (!href.include? '.00.00.00.00.shtml')
      txturl = href.sub(".shtml", "txt.shtml")
      # putu txturl, link.inner_html
      txt_urls[txturl] = 1
    end
  end
  puts txt_urls.length
  get_all_txt_pages(txt_urls)
end

def get_book_url
  Dir.glob("details/01*.shtml").each do |fn|
    #puts fn
    doc = Hpricot(open(fn))
    doc.search("a").each do |link|
      href = link['href']
      if href.include? 'product'
        puts SITE_URL + href
      end 
    end
  end
end

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
    # "title: #{@values[:title]}\n" + "author: #{@values[:author]}\n"
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
      s.gsub(/&nbsp;/, ' ')#.gsub(/<br>/, "\n")
    else
      nil
    end
  end
end

# get_base_txt_pages
# get_all_txt_pages('01.47.07.00.00.00txt.shtml'=>1, '01.54.04.00.00.00txt.shtml' => 1)

def get_details(list_file)
  total_books = 0
  cnt = 1
  book_id_to_isbn = {}
  File.open(list_file).each_line do |file|
    file.chomp!.sub!(/^http:\/\//, '')
    if File.exist? file
      book = Book.new(file)
      total_books += 1
      if cnt == 1000
        STDERR.print "#{total_books}, "
        cnt = 1
      else
        cnt += 1
      end
      puts file, book, ""
      book_id_to_isbn[book.book_id] = book.isbn
    end
  end
  
  File.open("book_id_to_isbn", "w") do |id_file|
  	id_file.puts "# book_id\tISBN"
    book_id_to_isbn.each {|key, value| id_file.puts "#{key}\t#{value}"}
  end
end

def get_relative(list_file)
  rels = {}
  total_books = 0
  cnt = 1
  File.open(list_file).each_line do |file|
  	file.chomp!
  	file =~ /product\/\d+\/(\d+)\.shtml/
  	book_id = $1
  	# print "%s\t'%s'\n" % [file, book_id]
  	url = "http://product.dangdang.com/script/alsoBuy.aspx?product_id=" + book_id;
  	# puts url
  	text = open(url).gets(nil)
  	# puts text
  	if text
  	  rels[book_id] = []
  	end
  	while (text =~ /product_id=(\d+)/)
  	  # printf("%d, ", $1)
  	  rels[book_id] << $1
  	  text = $'
    end
    if rels.key? book_id 
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

#get_relative(ARGV[0])
get_details(ARGV[0])
