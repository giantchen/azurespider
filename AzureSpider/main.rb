#!/usr/bin/env ruby
require 'rubygems'
require 'hpricot'

def get1()
  doc = open("2001_u8.html") { |f| Hpricot(f) }
  (doc/"a").each do |link|
    if link['href'] =~ /00.shtml$/
      puts "http://www.dangdang.com" + link['href']
    end
  end
end

def get2()
  Dir.glob('01*.shtml').each do |fn|
#    puts fn
    found = false
    doc = open(fn) {|f| Hpricot(f)}
    doc.search("a").each do |link|
      if link.inner_html =~ /strong/
        #puts link['href'].split('/')[4]
        puts "http://www.dangdang.com" + link['href']
        found = true
      end
    end
    if (!found) 
#      puts "******"
    end
  end
end

def get3()
  Dir.glob('txt/01.*.shtml').each do |fn|
  #["txt/01.01.01.00.00.00txt.shtml"].each do |fn|
    fn_base = fn.split('/')[1][0..19]
    puts fn_base
    max = 0
    maxurl = ""
    doc = open(fn) {|f| Hpricot(f)}
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
      puts maxurl.sub("_#{max}", "_#{page}")
    end
  end  
end

def get4
  Dir.glob('txt/01.*.shtml').each do |fn|
    #puts fn
    doc = open(fn) {|f| Hpricot(f)}
    doc.search("a").each do |link|
      if link['href'] =~ /product/
        puts "http://www.dangdang.com" + link['href']
      end
    end    
  end  
end

get4()