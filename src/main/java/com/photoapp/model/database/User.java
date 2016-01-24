package com.photoapp.model.database;


/**
 *
 */
public class User {
    private Long id;
    private String name;
    private Boolean wifi_only_upload;
    private String flashState;

    public User(Long id) {
        this.id = id;
    }

    public User(Long id, String name, Boolean wifi_only_upload, String flashState) {
        this.id = id;
        this.name = name;
        this.wifi_only_upload = wifi_only_upload;
        this.flashState = flashState;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isWifi_only_upload() {
        return wifi_only_upload;
    }

    public void setWifi_only_upload(Boolean wifi_only_upload) {
        this.wifi_only_upload = wifi_only_upload;
    }

    public String getFlashState() {
        return flashState;
    }

    public void setFlashState(String flashState) {
        this.flashState = flashState;
    }
}
