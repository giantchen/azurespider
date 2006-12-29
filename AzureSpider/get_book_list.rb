#!/usr/bin/env ruby
require 'rubygems'
require 'hpricot'
require 'open-uri'

SITE_URL = "http://www.dangdang.com"

def get_all_txt_pages(txt_urls)
  txt_urls.each_key do |txturl|
    url = SITE_URL + txturl
    max = 0
    maxurl = ""
    fn_base = txturl.sub(".shtml", "")
    
    # use local file as cache
    # filename = "txt/" + File.basename(txturl)
    # if File.exist? filename
    #   url = filename
    # end
    
    doc = Hpricot(open(url))
    doc.search("a").each do |link|
      if link.inner_html =~ /^\d+$/
        page = link['href'].sub(".shtml", "").split('_')[1].to_i
        if page > max
          max = page
          maxurl = link['href']
        end
        # puts link['href']# + ", " + link.inner_html
      elsif link['href'].include?(fn_base + "_") && link.inner_html != ">>"
        puts link['href'] + ", " + link.inner_html + "**"
      end
    end  

    # output all the index file.
    puts url
    (2..max).each do |page|
      puts SITE_URL + maxurl.sub("_#{max}", "_#{page}")
    end
  end
end

# read 
def get_base_txt_pages
  txt_urls = Hash.new
  doc = Hpricot(open("http://www.dangdang.com/zhuanti2006/book/2001.shtml"))
  doc.search("a[@href]").each do |link|
    href = link['href']
    if (href.include? '00.shtml') && (!href.include? '.00.00.00.00.shtml')
      txturl = href.sub(".shtml", "txt.shtml")
      txt_urls[txturl] = 1
    end
  end
  # puts txt_urls.length
  get_all_txt_pages(txt_urls)
end

get_base_txt_pages
