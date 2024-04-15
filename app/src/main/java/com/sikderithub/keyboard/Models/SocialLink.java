package com.sikderithub.keyboard.Models;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "social_info_table")

public class SocialLink {

    @PrimaryKey
    public int id;
    public String web_link;
    public String facebook_link;
    public String youtube_link;
}
