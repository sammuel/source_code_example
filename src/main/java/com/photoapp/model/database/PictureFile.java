package com.photoapp.model.database;

/**
 * Class entity for picture files
 */
public class PictureFile {
    private Long id;
    private String full_path_on_device;
    private String filename;
    private Integer status;
    private Double lat;
    private Double lon;
    private String geo_name;
    private Long time;
    private Boolean isDeleted;

    public PictureFile(Long id) {
        this.id = id;
    }

    public PictureFile(Long id, String full_path_on_device, String filename, Integer status, Double lat, Double lon, String geo_name, Long time, Boolean isDeleted) {
        this.id = id;
        this.full_path_on_device = full_path_on_device;
        this.filename = filename;
        this.status = status;
        this.lat = lat;
        this.lon = lon;
        this.geo_name = geo_name;
        this.time = time;
        this.isDeleted = isDeleted;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFull_path_on_device() {
        return full_path_on_device;
    }

    public void setFull_path_on_device(String full_path_on_device) {
        this.full_path_on_device = full_path_on_device;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getGeo_name() {
        return geo_name;
    }

    public void setGeo_name(String geo_name) {
        this.geo_name = geo_name;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
