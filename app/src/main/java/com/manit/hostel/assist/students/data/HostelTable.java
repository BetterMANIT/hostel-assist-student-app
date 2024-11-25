package com.manit.hostel.assist.students.data;

public class HostelTable {
    private int id;
    private String tableName;
    private String hostelName;
    private String purpose;

    public HostelTable(int id, String tableName, String hostelName, String purpose) {
        this.id = id;
        this.tableName = tableName;
        this.hostelName = hostelName;
        this.purpose = purpose;
    }

    public HostelTable(String purpose) {
        this.purpose = purpose;
    }

    public int getId() { return id; }
    public String getTableName() { return tableName; }
    public String getHostelName() { return hostelName; }
    public String getPurpose() { return purpose; }
}
