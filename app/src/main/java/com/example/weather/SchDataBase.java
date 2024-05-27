package com.example.weather;

public class SchDataBase {

    private Integer _id;
    private String title;
    private String place;
    private String date;
    private String time;
    private String description;

    public SchDataBase() {
    }

    public SchDataBase(Integer _id, String title, String place, String date, String time, String description) {
        this._id = _id;
        this.title = title;
        this.place = place;
        this.date = date;
        this.time = time;
        this.description = description;
    }

    public Integer get_id() {
        return _id;
    }

    public void set_id(Integer _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "主要内容："+ title  +",地点：" + place + ",时间："  + time +   ",备注：" + description ;
    }
}
