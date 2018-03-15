# -*- coding: utf-8 -*-
import pandas as pd
import numpy as np
import os                          #python miscellaneous OS system tool
import json
from collections import Counter

import sys
reload(sys)
sys.setdefaultencoding('utf8')

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
    # ori_df = pd.read_csv("data/0301_10000_new.csv", sep=",", low_memory=False, error_bad_lines=False)
    # ori_df.columns = ["user_id","event_id","time","$is_first_day","event","date","staytime","$os","$ip","$country","$province","$city","$manufacturer"]
    #
    # regiter_list = ["registerApply","registerSubmit","registerCode","registerPwd","registerSuccess","registerFail","registerApply","login_user","login_pwd","loginMeetProblem","loginSubmit","loginSuccess","loginFail","gesturePatternOther","gesturePatternForget","unlockPatternSuccess","patternLockByFingerprint","login_forget_pwd","forgetPasswordApply","refind_pwd_submit","refind_pwd_get_msg","refind_pwd_msg_submit","forgetPasswordMethod","forgetPasswordBank","reset_pwd_submit","reset_pwd_success","reset_pwd_fail","close_start_page","startAdsPage","startAdsPageEnter","startAdsPageSkip","logoutApply","logoutConfirm","logoutSuccess","city_search","city_select","city_autoselect"]
    # ori_df = ori_df[ori_df["event_id"].isin(regiter_list)]

    ori_df = pd.read_csv("data/0301_10000_new.csv", sep=",", low_memory=False, error_bad_lines=False)
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
    #print register_stat_df
    # register_stat_df.columns = register_stat_df.columns.map('{0[0]}-{0[1]}'.format)
    register_set = set(register_stat_df.index.values)

    ori_df = ori_df[ori_df["user_id"].isin(register_set)]
    print ori_df.shape

    with open("data/event_chinese.json") as json_file:
        event_dict = json.load(json_file)

    sort_df = ori_df.sort_values(["user_id", "time"], ascending=True)


    sort_df["event_id"] = sort_df["event_id"].map(lambda x: event_dict[x] if(x in event_dict.keys()) else x)

    save_df = sort_df[["user_id","event_id", "time"]]

    save_df.to_csv("data/sorted_registerSubmit_chinese.csv", index=False, header=False, sep=' ')





