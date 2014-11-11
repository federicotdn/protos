require 'faker'
require 'maildir'

maildir = Maildir.new(ARGV[0])

1.times do 
	text = "From: Author #{ Faker::Internet.email }\r\n"
	text += "To: Recipient <mike@aol.com>\r\n"
	text += "Subject: #{ Faker::Hacker.adjective + " " + Faker::Hacker.noun }\r\n"
	text += "\r\n"
	text += Faker::Hacker.say_something_smart + "\r\n"
	text + ".\r\n"
	maildir.add(text)
end
