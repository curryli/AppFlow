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

    print register_stat_df.shape

    register_stat_df = shuffle(register_stat_df)

    df_X = register_stat_df[["hour", "$os","$country","$province","$city", "$manufacturer"]]
    df_X = pd.get_dummies(df_X)
    df_y = register_stat_df["event_id"]
    X_train, X_test, y_train, y_test = train_test_split(df_X, df_y, test_size=0.2)
    clf = GradientBoostingClassifier(learning_rate=0.01, n_estimators=200, max_depth=10, min_samples_leaf=50, min_samples_split=100, max_features=10, subsample=0.7, random_state=10)
    clf = clf.fit(X_train, y_train)
    print clf.score(X_test, y_test)
    pred = clf.predict(X_test)
    cm1 = confusion_matrix(y_test, pred)
    print  cm1
    precision_p = float(cm1[0][0]) / float((cm1[0][0] + cm1[1][0]))
    recall_p = float(cm1[0][0]) / float((cm1[0][0] + cm1[0][1]))
    F1_Score = 2 * precision_p * recall_p / (precision_p + recall_p)
    print ("Precision:", precision_p)
    print ("Recall:", recall_p)
    print ("F1_Score:", F1_Score)



    # has_registerSubmit = register_stat_df[register_stat_df["event_id"] > 0]
    # register_set = set(has_registerSubmit.index.values)
    # print "有注册行为的用户个数：", len(register_set)
    #
    # no_registerSubmit = register_stat_df[register_stat_df["event_id"] == 0]
    # noregister_set = set(no_registerSubmit.index.values)
    # print "没有注册行为的用户个数：", len(noregister_set)

    # ori_df["has_regis"] = ori_df["user_id"].map(lambda x: 1 if(x in register_set) else 0)
    # df_X = ori_df[["hour", "$os","$country","$province","$city", "$manufacturer"]]
    # df_X = pd.get_dummies(df_X)
    # df_y = ori_df["has_regis"]
    # X_train, X_test, y_train, y_test = train_test_split(df_X, df_y, test_size=0.2)
    # clf = GradientBoostingClassifier(learning_rate=0.01, n_estimators=200, max_depth=10, min_samples_leaf=50, min_samples_split=100, max_features=10, subsample=0.7, random_state=10)
    # clf = clf.fit(X_train, y_train)
    # print clf.score(X_test, y_test)
    # pred = clf.predict(X_test)
    # cm1 = confusion_matrix(y_test, pred)
    # print  cm1
    # precision_p = float(cm1[0][0]) / float((cm1[0][0] + cm1[1][0]))
    # recall_p = float(cm1[0][0]) / float((cm1[0][0] + cm1[0][1]))
    # F1_Score = 2 * precision_p * recall_p / (precision_p + recall_p)
    # print ("Precision:", precision_p)
    # print ("Recall:", recall_p)
    # print ("F1_Score:", F1_Score)



