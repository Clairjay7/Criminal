package com.example.criminalgalorpot.model;

import java.util.Date;
import java.util.UUID;

public class Crime {
    private final UUID id;
    private String title;
    private Date date;
    private boolean solved;
    private String suspect;
    private String photoPath;

    public Crime() {
        this(UUID.randomUUID(), "", new Date(), false, null, null);
    }

    public Crime(String title) {
        this(UUID.randomUUID(), title, new Date(), false, null, null);
    }

    public Crime(UUID id, String title, Date date, boolean solved, String suspect, String photoPath) {
        this.id = id;
        this.title = title != null ? title : "";
        this.date = date != null ? date : new Date();
        this.solved = solved;
        this.suspect = suspect;
        this.photoPath = photoPath;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title : "";
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date != null ? date : new Date();
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public String getSuspect() {
        return suspect;
    }

    public void setSuspect(String suspect) {
        this.suspect = (suspect != null && !suspect.trim().isEmpty()) ? suspect : null;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}
