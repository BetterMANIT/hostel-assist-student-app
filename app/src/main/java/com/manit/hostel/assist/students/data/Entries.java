package com.manit.hostel.assist.students.data;
public class Entries {
    private String entryNo;
    private String name;
    private String roomNo;
    private String scholarNo;
    private String exitTime;
    private String purpose;
    private String entryTime;

    // Constructor
    public Entries(String entryNo, String name, String roomNo, String scholarNo, String exitTime, String entryTime) {
        this.entryNo = entryNo;
        this.name = name;
        this.roomNo = roomNo;
        this.scholarNo = scholarNo;
        this.exitTime = exitTime;
        this.entryTime = entryTime;
    }

    public Entries(String name) {
        this.entryNo = "24092024C12938";
        this.name = name;
        this.roomNo = "10C103";
        this.scholarNo = "241100112233";
        this.exitTime = "11:14 AM";
        this.entryTime = "";
    }

    // Getters and Setters
    public String getEntryNo() {
        return entryNo;
    }

    public void setEntryNo(String entryNo) {
        this.entryNo = entryNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public String getScholarNo() {
        return scholarNo;
    }

    public void setScholarNo(String scholarNo) {
        this.scholarNo = scholarNo;
    }

    public String getExitTime() {
        return exitTime;
    }

    public void setExitTime(String exitTime) {
        this.exitTime = exitTime;
    }

    public String getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
