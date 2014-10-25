require 'faker'
require 'maildir'

maildir = Maildir.new(ARGV[0])

50.times do 
	text = "From: Author #{ Faker::Internet.email }\n"
	text += "To: Recipient <mike@aol.com>\n"
	text += "Subject: #{ Faker::Hacker.adjective + " " + Faker::Hacker.noun }\n\n"
	text += Faker::Hacker.say_something_smart + "\n\n"
	maildir.add(text)
end
