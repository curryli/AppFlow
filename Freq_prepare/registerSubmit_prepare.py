# -*- coding: utf-8 -*-
import pandas as pd
import numpy as np
import os                          #python miscellaneous OS system tool
import json
from collections import Counter

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

if __name__ == '__main__':
    ori_df = pd.read_csv("data/0301_100000.csv", sep=",", low_memory=False, error_bad_lines=False)
    ori_df.columns = ["user_id", "event_id", "time", "$is_first_day", "event", "date", "staytime", "$os", "$ip",
                      "$country", "$province", "$city", "$manufacturer"]

    print ori_df.shape

    grouped = ori_df.groupby([ori_df['user_id']], group_keys=True)

    group_keys = []
    for name, group in grouped:
        group_keys.append(name)

    agg_dict = {}
    agg_dict["event_id"] = has_registerSubmit
    register_stat_df = grouped.agg(agg_dict)

    register_stat_df = register_stat_df[register_stat_df["event_id"] > 0]
    # print register_stat_df
    # register_stat_df.columns = register_stat_df.columns.map('{0[0]}-{0[1]}'.format)
    register_set = set(register_stat_df.index.values)
    print "有注册行为的用户个数：", len(register_set)

    ori_df = ori_df[ori_df["user_id"].isin(register_set)]
    print ori_df.shape

    eventid_set = set(ori_df.event_id.values)
    idx_eid = {int(idx): e_id for idx, e_id in enumerate(eventid_set)}
    eid_idx = {e_id: int(idx) for idx, e_id in enumerate(eventid_set)}

    with open('data/idx_eid_registerSubmit.json', 'w') as json_file:
        json_file.write(json.dumps(idx_eid))

    userid_set = set(ori_df.user_id.values)
    uid_idx = {u_id: idx for idx, u_id in enumerate(userid_set)}

    sort_df = ori_df.sort_values(["user_id", "time"], ascending=True)
    # #print sort_df.head(5)
    # sort_df.to_csv("sorted.csv",index=False)

    sort_df["event_id"] = sort_df["event_id"].map(lambda x: eid_idx[x])
    sort_df["user_id"] = sort_df["user_id"].map(lambda x: uid_idx[x])

    sort_df["fill"] = 1

    save_df = sort_df[["user_id","fill","event_id"]]
    #print save_df.head(5)
    save_df.to_csv("data/sorted_registerSubmit.csv", index=False, header=False, sep=' ')





