#!/usr/bin/env python

import config
import requests
import json 
import os
import re
import time
from tweepy import OAuthHandler, Stream
from tweepy.streaming import StreamListener

output_file = 'output_tweets.json'
api_key = config.API_key
api_secret = config.API_secret
access_token = config.access_token
access_secret = config.access_secret
tweetcount = 0


class streamListener(StreamListener):

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
                "url" : str(urls),
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
        if(tweetcount >= 1000):
            #timedone = time.process_time()
            #print(f"{timedone/60} minutes")
            print("Done collecting tweets!")
            exit()

        with open(output_file, 'a+') as output:
            json.dump(dictionary, output)
            output.write('\n')
            tweetcount+=1
            return True
        

    def on_error(self, status):
        print(status)
#end of streamListener

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

#resource http://docs.tweepy.org/en/latest/streaming_how_to.html