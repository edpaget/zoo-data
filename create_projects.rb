require 'json'
require 'net/http'

projects = File.open('projects.json', 'r').map{ |l| JSON.parse(l) }.first

puts projects

http = Net::HTTP.new('localhost', 3002)
projects.each do |project|
  http.post('/projects', project.to_json, {"Content-Type" => "application/json"})
end