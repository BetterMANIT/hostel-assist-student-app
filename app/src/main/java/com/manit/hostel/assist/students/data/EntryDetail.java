package com.manit.hostel.assist.students.data;

public class EntryDetail {
    private String id;
    private String scholarNo;
    private String name;
    private String roomNo;
    private String photoUrl;
    private String phoneNo;
    private String section;
    private String openTime;
    private String closeTime;
    private String updatedAt;

    public EntryDetail(String id, String scholarNo, String name, String roomNo, String photoUrl,
                       String phoneNo, String section, String openTime, String closeTime, String updatedAt) {
        this.id = id;
        this.scholarNo = scholarNo;
        this.name = name;
        this.roomNo = roomNo;
        this.photoUrl = photoUrl;
        this.phoneNo = phoneNo;
        this.section = section;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getScholarNo() {
        return scholarNo;
    }

    public String getName() {
        return name;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getSection() {
        return section;
    }

    public String getOpenTime() {
        return openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setScholarNo(String scholarNo) {
        this.scholarNo = scholarNo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
