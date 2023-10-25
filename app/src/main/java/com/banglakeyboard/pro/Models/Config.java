package com.banglakeyboard.pro.Models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "config_table")
public class Config {
    @PrimaryKey
    public int id;

    @ColumnInfo(defaultValue = "0")

    public int ad_hold_time = 2;
    @ColumnInfo(defaultValue = "0")

    public int emoji_view_ad_status=0;

    @ColumnInfo(defaultValue = "0")
    public int emoji_view_ad_type = 0;


    @ColumnInfo(defaultValue = "2")
    public int content_interval=1;
    @Ignore
    public Update update;
    public String tutorial_link;
    public int top_ad_interval = 480;




}
