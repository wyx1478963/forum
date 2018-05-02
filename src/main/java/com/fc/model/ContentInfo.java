package com.fc.model;


public class ContentInfo {
    private Long id;
    private Long authorId;
    private String title;
    private String content;
    private double[] vector;
    private double distance;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getAuthorId() {
        return authorId;
    }
    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public double[] getVector() {
        return vector;
    }
    public void setVector(double[] vector) {
        this.vector = vector;
    }
    public double getDistance() {
        return distance;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }
}