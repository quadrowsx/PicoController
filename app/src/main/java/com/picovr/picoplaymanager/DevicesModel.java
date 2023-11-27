package com.picovr.picoplaymanager;

public class DevicesModel {

    public DevicesModel(String device_id, boolean exit, int id, int state, String url, int videoType) {
        this.device_id = device_id;
        this.exit = exit;
        this.id = id;
        this.state = state;
        this.url = url;
        this.videoType = videoType;
    }

    public DevicesModel(){

    }
    String device_id;
    boolean exit;
    int id;
    int state;
    String url;
    int videoType;

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public boolean isExit() {
        return exit;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getVideoType() {
        return videoType;
    }

    public void setVideoType(int videoType) {
        this.videoType = videoType;
    }
}
