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
  def initialize(file)
    @regexps = {
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
      'author_intro' => /作者简介：<\/td>\s*<\/tr>\s*<tr>\s*<td>(.*?)<\/td>/
    }
    @filters = {
      'summary' => lambda {|text| unhtml(text)},
      'author_intro' => lambda {|text| unhtml(text)},
    }
    @values = {}
    parse(file)
  end

  def to_s
    # "title: #{@values[:title]}\n" + "author: #{@values[:author]}\n"
    s = ''
    @values.sort.each {|x| s << x.join(": ") << "\n"}
    s
  end

 private

  def parse(file)
    text = open(file).gets(nil)
    @regexps.each do |key, re|
      if text =~ re
        if @filters.key?(key)
          @values[key] = @filters[key].call($1.strip)
        else
          @values[key] = $1.strip
        end
      end
    end
  end

  def unhtml(s)
    if (s)
      s.gsub(/&nbsp;/, ' ')#.gsub(/<br>/, "\n")
    else
      nil
    end
  end
end

# get_base_txt_pages
# get_all_txt_pages('01.47.07.00.00.00txt.shtml'=>1, '01.54.04.00.00.00txt.shtml' => 1)

File.open("b2.txt").each_line do |file|
  file.chomp!.sub!(/^http:\//, '.')
  if File.exist? file
    book = Book.new(file)
    #printf '.'
    puts file, book
  end
end
