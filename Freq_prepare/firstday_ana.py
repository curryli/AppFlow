# -*- coding: utf-8 -*-
import pandas as pd
import numpy as np
import os                          #python miscellaneous OS system tool
import json
from collections import Counter
from sklearn.cross_validation import train_test_split
from sklearn.metrics import recall_score, precision_score

# 导入随机森林算法库
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.grid_search import GridSearchCV
from sklearn.grid_search import RandomizedSearchCV
from sklearn.metrics import confusion_matrix
from sklearn.utils import shuffle
from sklearn.ensemble import GradientBoostingClassifier

import matplotlib.pyplot as plt
import seaborn as sns
import matplotlib.gridspec as gridspec


from pylab import mpl

#mpl.rcParams['font.sans-serif'] = ['SimHei']
mpl.rcParams['font.sans-serif'] = ['Microsoft YaHei']    # 指定默认字体：解决plot不能显示中文问题
mpl.rcParams['axes.unicode_minus'] = False           # 解决保存图像是负号'-'显示为方块的问题


def store(data):
    with open('data.json', 'w') as json_file:
        json_file.write(json.dumps(data))

def load(data):
    with open(data) as json_file:
        data = json.load(json_file)
        return data

def has_registerSubmit(arr):
    cnt_pairs = Counter(arr)
    if(cnt_pairs["registerSubmit"]>0):
        return 1
    else:
        return 0


def getHour(str):
    return int(str[11:13])

def most_frequent_item(arr):  # 同一个人出现次数最多的元素
    cnt_set = Counter(arr)
    max_cnt_pair = cnt_set.most_common(1)[0]  # (maxitem,maxcount)
    return max_cnt_pair[0]

if __name__ == '__main__':
    ori_df = pd.read_csv("data/0301_100000.csv", sep=",", low_memory=False, error_bad_lines=False)
    ori_df.columns = ["user_id", "event_id", "time", "$is_first_day", "event", "date", "staytime", "$os", "$ip",
                      "$country", "$province", "$city", "$manufacturer"]

    print ori_df.shape
    ori_df = ori_df[ori_df["$is_first_day"]==1]
    print ori_df.shape

    ori_df["hour"] = ori_df["time"].map(lambda x: getHour(str(x)))

    grouped = ori_df.groupby([ori_df['user_id']], group_keys=True)

    group_keys = []
    for name, group in grouped:
        group_keys.append(name)

    agg_dict = {}
    agg_dict["hour"] =  'mean'

    agg_dict["$os"] = most_frequent_item
    agg_dict["$country"] = most_frequent_item
    agg_dict["$province"] = most_frequent_item
    agg_dict["$city"] = most_frequent_item
    agg_dict["$manufacturer"] = most_frequent_item

    agg_dict["event_id"] = has_registerSubmit

    register_stat_df = grouped.agg(agg_dict)

    register_stat_df["os"] = register_stat_df["$os"]

    # f, (ax1, ax2) = plt.subplots(2, 1, sharex=True, figsize=(12,4))
    # bins = 24
    # ax1.hist(register_stat_df.hour[register_stat_df.event_id == 1], bins = bins)
    # ax1.set_title('has_register')
    # ax2.hist(register_stat_df.hour[register_stat_df.event_id == 0], bins = bins)
    # ax2.set_title('no_register')
    # plt.xlabel('hour_avg')
    # plt.ylabel('account number')
    # plt.show()


    # has_registerSubmit = register_stat_df[register_stat_df["event_id"] > 0]
    # no_registerSubmit = register_stat_df[register_stat_df["event_id"] == 0]
    # var = has_registerSubmit.groupby('$os').hour.count()
    # print var
    # fig = plt.figure()
    # ax1 = fig.add_subplot(111)
    # ax1.set_xlabel('os')
    # ax1.set_ylabel('Account number')
    # ax1.set_title('HasRegister')
    # var.plot(kind='bar')
    # plt.show()


    # var = register_stat_df.groupby(['$province', 'event_id']).hour.count()
    # var.unstack().plot(kind='bar', stacked=True, color=['blue','red'])
    # plt.show()

    var = register_stat_df.groupby(['$os', 'event_id']).hour.count()
    var.unstack().plot(kind='bar', stacked=True, color=['blue','red'])
    plt.show()
