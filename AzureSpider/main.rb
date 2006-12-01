require 'rubygems'
require 'hpricot'

doc = open("2001.shtml") { |f| Hpricot(f) }

(doc/"a").each do |link|
  if link['href'] =~ /00.shtml$/
    puts link['href'], link.inner_html
  end
end
