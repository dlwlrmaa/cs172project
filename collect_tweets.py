#!/usr/bin/env python
from urllib import request 
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
import queue
from threading import Thread

output = 'tweets_'
file_num = 1
output_file_name = output + str(file_num) + '.json'
maxfilesize = 10 * 1024 * 1024
filesmax = 200 #10 files * 200
api_key = 'ds8VYTy9fQ6eYMMomHTnO05bE' #config.API_key
api_secret = 'igiWuhBeCmYdHF8JbrymycCEq7jNxk4m6yFq9jPXzgO2O4RPiV' #config.API_secret
access_token = '1945957765-O1XB4W4LuST4twPkXeb16RzpoFy3HdHAG8gmT14' #config.access_token
access_secret = 'azMIGPu15pImXHJuZ3XlvamQmTWL7oXhSQwvcmDz4KLi0' #config.access_secret
tweetcount = 0
tweetfile = open(output_file_name, 'a+')
#StreamLister can classify most common twitter messages and routes
#them to appropriately named methods, but these methods
#are only stubs
#http://docs.tweepy.org/en/v3.4.0/streaming_how_to.html
class streamListener(StreamListener):

#on_status method of a stream listener receives all statuses
    def __init__(self, q = queue.Queue()):
        super(streamListener, self).__init__() #use this to not get attribute error 
        num_threads = 4
        self.q = q
        for i in range(num_threads):
            t = Thread(target=self.do_stuff)
            t.daemon = True #we set to true so tweepy does not disconnect accidently before we are finished
            t.start() 

    def on_status(self, status):
        #print(status)
        #print('\n')
        #print('\n')

        global tweetcount
        tweetdata = status._json
        text = tweetdata.get('text')
        url = tweetdata['entities']['urls']

        #if the tweet is truncated then we get the expanded tweet
        try:
            text = status.extended_tweet['full_text']
        except Exception as e:
            pass
        #retrieve the expanded url(s) in the tweet if there are any
        try:
            url = tweetdata['entities']['urls'][0]['expanded_url']
            url_bool = True
        except Exception as e:
            url_bool = False

        dictionary = {
            'user': tweetdata['user']['screen_name'],
            'text': text,
            'city': tweetdata['place']['full_name'],
            'country': tweetdata['place']['country'],
            'created_at': tweetdata.get('created_at'),
            'geolocation': tweetdata['place']['bounding_box']['coordinates'],
            'urls': url,
            'url_bool': url_bool,
            'url_title' : None,                                                     #get title in a different function
            'hashtags': tweetdata['entities']['hashtags'],
            'retweets': tweetdata['retweet_count'],
            'favorites': tweetdata['favorite_count'],
            'replies': tweetdata['reply_count'],
            'quotes': tweetdata['quote_count'],
            'followers': tweetdata['user']['followers_count'],
            'verified': tweetdata['user']['verified']
        }
        self.q.put(dictionary)
        return True
        

    def on_error(self, status):
        print(status)
        return False

    def do_stuff(self):
        while True:
            tweet = self.q.get()
            URLTitleFinder(tweet)
            self.q.task_done()
#end of streamListener

def URLTitleFinder(tweet):
    global file_num, tweetfile, output_file_name, tweetcount
    #title = []
    #print(tweetcount)
    tweetcount+=1
    has_url = tweet['url_bool']
    #print(f"{test} + '\n'")
    #print(tweet['url_bool'])
    if(has_url and "twitter" not in tweet['urls']):
        url = tweet['urls']
        #html = request.urlopen(url).read().decode('utf8')
        html = requests.get(url)
        #html[:60]
        soup = BeautifulSoup(html, 'html.parser')
        #title = soup.find('title')
        title = str(soup.find('title'))
        title = title[7:-8]
        tweet['url_title'] = title
        #print(f'{title}')
        #print(soup.title.string)

    tweetfile.write(json.dumps(tweet) + '\n')

    if(file_num > filesmax): #only get 2gb
        myStream.disconnect()

    page_info = os.stat(output_file_name)
    if(page_info.st_size > maxfilesize):
        print('Creating new json file...' + '\n')
        tweetfile.close()
        file_num+=1
        output_file_name = output + str(file_num) + '.json'
        tweetfile = open(output_file_name, 'a+')              
#end of URLTitleFinder

if __name__ == '__main__':
    #numtweets = 0
    new_stream_listener = streamListener()
    auth = OAuthHandler(api_key, api_secret)
    auth.set_access_token(access_token, access_secret)
    myStream = Stream(auth, new_stream_listener)

    myStream.filter(locations = [-123.90, 32.52, -64.24, 48.54], languages = ['en'] ) #coordinates are for The United States of America and only getting tweets in english
    #myStream.filter(languages = ['en'] )
    #URLTitleFinder('output_tweets.json')
#resource http://docs.tweepy.org/en/latest/streaming_how_to.html
