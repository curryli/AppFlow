# -*- coding: utf-8 -*-
import pandas as pd
import numpy as np
import os                          #python miscellaneous OS system tool
import json
import re
import codecs



with open('ori_event.txt', 'r') as fin:
    eve_dict = {}
    while True:
        line = fin.readline()
        if not line:
            break
        if "event_id" in line:
            line = line.replace(r'"event_id：', r':: ')
            line = line.replace(r'"event_id:', r':: ')
            line = line.replace(r'event_id：', r':: ')
            line = line.replace(r'event_id:', r':: ')
            line = re.sub(r'\s+', "", line)
            arr = line.split(r"::")
            eve_dict[arr[1]] = arr[0]

with open('event_chinese.json', 'w') as json_file:
    json_file.write(json.dumps(eve_dict, ensure_ascii=False))

