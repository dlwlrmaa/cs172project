#!/usr/bin/env python
import urllib.request
import config
import requests
import json 
import os
import re
import time
from bs4 import BeautifulSoup
from tweepy import OAuthHandler, Stream
from tweepy.streaming import StreamListener

output_file = 'output_tweets.json'
api_key = 'ds8VYTy9fQ6eYMMomHTnO05bE' #config.API_key
api_secret = 'igiWuhBeCmYdHF8JbrymycCEq7jNxk4m6yFq9jPXzgO2O4RPiV' #config.API_secret
access_token = '1945957765-O1XB4W4LuST4twPkXeb16RzpoFy3HdHAG8gmT14' #config.access_token
access_secret = 'azMIGPu15pImXHJuZ3XlvamQmTWL7oXhSQwvcmDz4KLi0' #config.access_secret
tweetcount = 0

#StreamLister can classify most common twitter messages and routes
#them to appropriately named methods, but these methods
#are only stubs
#http://docs.tweepy.org/en/v3.4.0/streaming_how_to.html
class streamListener(StreamListener):

#on_data method of a stream listener receives all messages
#and calls function according to the message type. 
    def on_data(self, data):
        global tweetcount
        parsed_json = json.loads(data)
        user = parsed_json["user"]["screen_name"]
        urls  = parsed_json["entities"]["urls"]
        #parsed_url = parsed_json["expanded_url"]
        location = parsed_json["place"]["full_name"]
        timestamp = parsed_json["created_at"]
        truncated = parsed_json["truncated"]
        if( not truncated):
            text = parsed_json["text"]

            dictionary ={
                "user": str(user),
                "text": str(text),
                "url" : str(urls), #
                "location" : str(location),
                "timestamp" : str(timestamp)
            }
            print("Not extended")
        
        else:
            extendedtext = parsed_json["extended_tweet"]["full_text"]

            dictionary = {
                "user": str(user),
                "text": str(extendedtext),
                "url" : str(urls),
                "location" : str(location),
                "timestamp" : str(timestamp)
            }
            print("Extended")
        #timedone = 0
        if(tweetcount >= 10): #1000
            #timedone = time.process_time()
            #print(f"{timedone/60} minutes")
            print("Done collecting tweets!")
            #exit()
            return False

        with open(output_file, 'a+') as output:
            json.dump(dictionary, output)
            output.write('\n')
            tweetcount+=1
            return True
        

    def on_error(self, status):
        print(status)
#end of streamListener

def URLTitleFinder(tweetFile):
    #TODO: Find URLs in Tweets

    #TODO: Add title of URL into Respective JSON object as new field
    data = ""
    title = ""
    with open(tweetFile) as f:
        for line in f:
            data = json.loads(line) #(a-z)(A-Z)(0-9)($_@.&+)(!*/(), )()
            pageURL = re.findall('http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\), ]|(?:%[0-9a-fA-F][0-9a-fA-F]))+\.html', data["text"])
            if pageURL != []:
                for i in pageURL:
                    soup = BeautifulSoup(urllib.request.urlopen(pageURL[i]), "html.parser")
                    title = soup.title.string
            line.update({"Title": title})

if __name__ == '__main__':
    #numtweets = 0
    new_stream_listener = streamListener()
    auth = OAuthHandler(api_key, api_secret)
    auth.set_access_token(access_token, access_secret)
    myStream = Stream(auth, new_stream_listener)

    if os.path.isfile(output_file):
        os.remove(output_file)
        print(f"File {output_file} has been reinitialized")

    myStream.filter(locations = [-118.69, 33.73, -117.85, 34.22]) #coordinates are for Los Angeles
    URLTitleFinder('output_tweets.json')
#resource http://docs.tweepy.org/en/latest/streaming_how_to.html
