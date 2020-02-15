package com.example.nirbhay.Users;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class PostDetails {
    private String ownerUserId, postId, ownerName;
    private long upVoteCount, downVoteCount;
    private long noOfReports;
    private String postBody;
    private String pincode;
    private Date date;
    private HashMap<String, String> upVoteUsers;
    public PostDetails(){

    }

    public PostDetails(String pincode, String ownerName, String ownerUserId, String postId, long upVoteCount, long downVoteCount, ArrayList<String> upVoteUsers, ArrayList<String> downVoteUsers, long noOfReports, String postBody, Date date) {
        this.pincode = pincode;
        this.ownerName = ownerName;
        this.ownerUserId = ownerUserId;
        this.postId = postId;
        this.upVoteCount = upVoteCount;
        this.downVoteCount = downVoteCount;
        this.noOfReports = noOfReports;
        this.postBody = postBody;
        this.date = date;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getPincode() {
        return pincode;
    }

    public HashMap<String, String> getUpVoteUsers() {
        return upVoteUsers;
    }

    public void setUpVoteUsers(HashMap<String, String> upVoteUsers) {
        this.upVoteUsers = upVoteUsers;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setUpVoteCount(long upVoteCount) {
        this.upVoteCount = upVoteCount;
    }

    public void setDownVoteCount(long downVoteCount) {
        this.downVoteCount = downVoteCount;
    }


    public void setNoOfReports(long noOfReports) {
        this.noOfReports = noOfReports;
    }

    public void setPostBody(String postBody) {
        this.postBody = postBody;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getPostId() {
        return postId;
    }

    public long getUpVoteCount() {
        return upVoteCount;
    }

    public long getDownVoteCount() {
        return downVoteCount;
    }


    public long getNoOfReports() {
        return noOfReports;
    }

    public String getPostBody() {
        return postBody;
    }

    public Date getDate() {
        return date;
    }
}
