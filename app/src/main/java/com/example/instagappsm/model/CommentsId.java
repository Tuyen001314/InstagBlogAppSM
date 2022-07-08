package com.example.instagappsm.model;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

import java.security.PublicKey;

public class CommentsId {
    @Exclude
    public String ComentsId;
    public <T extends CommentsId> T withId (@NonNull final String id) {
        this.ComentsId = id;
        return (T) this;
    }
}
