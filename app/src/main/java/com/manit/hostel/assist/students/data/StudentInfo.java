package com.manit.hostel.assist.students.data;

import org.json.JSONException;
import org.json.JSONObject;

public class StudentInfo {
    private String scholarNo;
    private String name;
    private String roomNo;
    private String hostelName;
    private String photoUrl;
    private String phoneNo;
    private String section;
    private String guardianNo;
    private String entryExitTableName;

    // Constructor
    public StudentInfo(String scholarNo, String name, String roomNo, String hostelName, String photoUrl, String phoneNo, String section, String guardianNo, String entryExitTableName) {
        this.scholarNo = scholarNo;
        this.name = name;
        this.roomNo = roomNo;
        this.hostelName = hostelName;
        this.photoUrl = photoUrl;
        this.phoneNo = phoneNo;
        this.section = section;
        this.guardianNo = guardianNo;
        this.entryExitTableName = entryExitTableName;
    }

    // Getters
    public String getScholarNo() {
        return scholarNo;
    }

    public String getName() {
        return name;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public String getHostelName() {
        return hostelName;
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

    public String getGuardianNo() {
        return guardianNo;
    }

    public String getEntryExitTableName() {
        return entryExitTableName;
    }

    public String getJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("scholarNo", scholarNo);
        jsonObject.put("name", name);
        jsonObject.put("roomNo", roomNo);
        jsonObject.put("hostelName", hostelName);
        jsonObject.put("photoUrl", photoUrl);
        jsonObject.put("phoneNo", phoneNo);
        jsonObject.put("section", section);
        jsonObject.put("guardianNo", guardianNo);
        jsonObject.put("entryExitTableName", entryExitTableName);
        return jsonObject.toString();
    }

    // Static method to create an object from JSON string
    public static StudentInfo fromJSON(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        return new StudentInfo(
                jsonObject.optString("scholarNo"),
                jsonObject.optString("name"),
                jsonObject.optString("roomNo"),
                jsonObject.optString("hostelName"),
                jsonObject.optString("photoUrl"),
                jsonObject.optString("phoneNo"),
                jsonObject.optString("section"),
                jsonObject.optString("guardianNo"),
                jsonObject.optString("entryExitTableName")
        );
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setGuardianNo(String guardianNo) {
        this.guardianNo = guardianNo;
    }

    public void setPhotoUrl(String s) {
        this.photoUrl = s;
    }
}
