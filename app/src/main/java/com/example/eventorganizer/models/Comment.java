package com.example.eventorganizer.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

@IgnoreExtraProperties
public class Comment {
    private String id;
    private String text;
    private String creatorId;
    private String creatorName; // To display the user's full name
    private String creatorAvatarUrl;
    @ServerTimestamp
    private Date timestamp;// For sorting comments and showing time

    // An empty constructor is required for Firestore to automatically map data
    public Comment() {}


    // --- Getters ---
    public String getId() { return id; }
    public String getText() { return text; }
    public String getCreatorId() { return creatorId; }
    public String getCreatorName() { return creatorName; }
    public Date getTimestamp() { return timestamp; }
    public String getCreatorAvatarUrl() { return creatorAvatarUrl; }

    // --- Setters ---
    public void setId(String id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setCreatorAvatarUrl(String creatorAvatarUrl) { this.creatorAvatarUrl = creatorAvatarUrl; }
}