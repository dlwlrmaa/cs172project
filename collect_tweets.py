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

#on_status method of a stream listener receives all statuses
    def on_status(self, status):
        #print(status)
        #print('\n')

        global tweetcount
        tweetdata = status._json
        text = tweetdata.get('text')
        try:
            text = status.extended_tweet['full_text']
        except Exception as e:
            pass

        dictionary = {
            'user': tweetdata['user']['screen_name'],
            'text': text,
            'urls': tweetdata['entities']['urls'],
            'location': tweetdata['place']['full_name'],
            'created_at': tweetdata.get('created_at'),
            'geolocation': tweetdata['place']['bounding_box']['coordinates']
        }
        if(tweetcount >= 50):
            print("Done collecting tweets!")
            return False

        with open(output_file, 'a+') as output:
            json.dump(dictionary, output)
            output.write('\n')
            tweetcount+=1
            return True

    def on_error(self, status):
        print(status)
        return False
#end of streamListener

def URLTitleFinder(tweetFile):
    print("Finding URL Titles!")
    expanded_url = 'expanded_url'
    with open(tweetFile, "a+") as f:
        for line in f:
            data = json.loads(line)
            pageURL = re.findall('http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\), ]|(?:%[0-9a-fA-F][0-9a-fA-F]))+', data["text"])
            if data["url"] != [] and (expanded_url in data["url"][0]) and pageURL != []:
				#print(data["url"][0])
                pageURL = re.findall('http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\), ]|(?:%[0-9a-fA-F][0-9a-fA-F]))+', data['url'][0]['expanded_url'])
                if pageURL!= []:
                    for i in range(len(pageURL)):
						#print(pageURL[i])
                        soup = BeautifulSoup(urllib.request.urlopen(pageURL[i]).read(), "html.parser")
                        title = soup.title.string.encode("utf-8")
                        data.update({"title" : str(title)})
                        json.dump(data, f)
#end of URLTitleFinder

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
