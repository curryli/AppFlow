# -*- coding: utf-8 -*-
import pandas as pd
import numpy as np
import os                          #python miscellaneous OS system tool
import json
from datetime import datetime

def store(data):
    with open('data.json', 'w') as json_file:
        json_file.write(json.dumps(data))

def load(data):
    with open(data) as json_file:
        data = json.load(json_file)
        return data


def format_time(str):
    t_norm = r"2018-03-01 00:00:00.000"
    t0 = datetime.strptime(t_norm, "%Y-%m-%d %H:%M:%S.%f")
    t1 = datetime.strptime(str, "%Y-%m-%d %H:%M:%S.%f")
    return (t1 - t0).seconds

if __name__ == '__main__':
    ori_df = pd.read_csv("data/0301_1000_new.csv", sep=",", low_memory=False, error_bad_lines=False)
    ori_df.columns = ["user_id","event_id","time","$is_first_day","event","date","staytime","$os","$ip","$country","$province","$city","$manufacturer"]

    ori_df["format_time"] = ori_df["time"].map(lambda x: format_time(str(x)))



    eventid_set = set(ori_df.event_id.values)
    idx_eid = {int(idx): e_id for idx, e_id in enumerate(eventid_set)}
    eid_idx = {e_id: int(idx) for idx, e_id in enumerate(eventid_set)}

    with open('idx_eid_1000.json', 'w') as json_file:
        json_file.write(json.dumps(idx_eid))

    with open('eid_idx_1000.json', 'w') as json_file:
        json_file.write(json.dumps(eid_idx))

    userid_set = set(ori_df.user_id.values)
    uid_idx = {u_id: idx for idx, u_id in enumerate(userid_set)}

    sort_df = ori_df.sort_values(["user_id", "format_time"], ascending=True)
    # #print sort_df.head(5)
    # sort_df.to_csv("sorted.csv",index=False)

    sort_df["event_id"] = sort_df["event_id"].map(lambda x: eid_idx[x])
    sort_df["user_id"] = sort_df["user_id"].map(lambda x: uid_idx[x])


    save_df = sort_df[["user_id","event_id","format_time"]]
    #print save_df.head(5)
    save_df.to_csv("sorted_1000_formated.csv", index=False, header=False, sep=',')





