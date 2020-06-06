#!/usr/bin/env python
import os
import json
output = 'tweets_'
file_num = 1
output_file_name = output + str(file_num) + '.json'
page_info = os.stat(output_file_name)
print(page_info.st_size)