package com.example.eventorganizer.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Event {
    private String id;
    private String title;
    private String description;
    private String date;
    private String creatorId;
    private String creatorEmail;
    private String time;
    private String location;
    private String imageUrl; 

    // An empty constructor is required for Firestore
    public Event() { }

    // --- Getters ---
    public String getTime() { return time; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getCreatorId() { return creatorId; }
    public String getCreatorEmail() { return creatorEmail; }

    // This is the getter method that was missing
    public String getImageUrl() { return imageUrl; }


    // --- Setters ---
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public void setCreatorEmail(String creatorEmail) { this.creatorEmail = creatorEmail; }

    
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
