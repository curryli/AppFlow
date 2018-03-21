# -*- coding: utf-8 -*-
import pandas as pd
import numpy as np
import os                          #python miscellaneous OS system tool
import json
from collections import Counter
import re

import sys
reload(sys)
sys.setdefaultencoding('utf8')


if __name__ == '__main__':
    with open("data/event_chinese_detail.json") as json_file:
        event_dict = json.load(json_file)


#https://www.cnblogs.com/dplearning/p/5834628.html
#http://www.jb51.net/article/115866.htm

    patern = re.compile("(\\[[^\\]]*\\])")

    with open("data/spark_out.txt") as input:
        for line in input:
            result1 = re.sub(patern, lambda m: event_dict[m.group(0)[1:-1]] + "-->" if(m.group(0)[1:-1] in event_dict.keys()) else m.group(0)[1:-1] + "-->", line)
            print result1











