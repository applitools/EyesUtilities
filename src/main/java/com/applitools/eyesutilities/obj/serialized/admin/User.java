package com.applitools.eyesutilities.obj.serialized.admin;

import java.util.HashMap;

public class User {
    String id;
    String email;
    String fullName;
    String sessionId;
    String lastSessionStartedAt;
    String addedAt;
    int numberOfSuccesfulLogins;
    HashMap<String, String> settings;
    HashMap<String, Object> plgSettings;

    public User() {
    }

    public User(String id, String email, String fullName) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getLastSessionStartedAt() {
        return lastSessionStartedAt;
    }

    public void setLastSessionStartedAt(String lastSessionStartedAt) {
        this.lastSessionStartedAt = lastSessionStartedAt;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }

    public int getNumberOfSuccesfulLogins() { return numberOfSuccesfulLogins; }

    public void setNumberOfSuccesfulLogins(int numberOfSuccesfulLogins) {
        this.numberOfSuccesfulLogins = numberOfSuccesfulLogins;
    }

    public HashMap<String, String> getSettings() {
        return settings;
    }

    public void setSettings(HashMap<String, String> settings) {
        this.settings = settings;
    }

    public HashMap<String, Object> getPlgSettings() {
        return plgSettings;
    }

    public void setPlgSettings(HashMap<String, Object> plgSettings) {
        this.plgSettings = plgSettings;
    }

}
