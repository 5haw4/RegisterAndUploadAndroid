package com.test.registerandupload.model;

public class Post {

    private String postId;
    private String imageUrl;
    private String description;
    private String creationTime;
    private String creatorUid;
    private String creatorUsername;
    private boolean didUserCreatePost;

    public Post(String postId, String imageUrl, String description, String creationTime, String creatorUid, String creatorUsername, boolean didUserCreatePost) {
        this.postId = postId;
        this.imageUrl = imageUrl;
        this.description = description;
        this.creationTime = creationTime;
        this.creatorUid = creatorUid;
        this.creatorUsername = creatorUsername;
        this.didUserCreatePost = didUserCreatePost;
    }

    public String getPostId() {
        return postId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public String getCreatorUid() {
        return creatorUid;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public boolean didUserCreatePost() {
        return didUserCreatePost;
    }
}
