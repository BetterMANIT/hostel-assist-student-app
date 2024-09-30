package com.manit.hostel.assist.students.data;

public class HostelTable {
    private int id;
    private String tableName;
    private String hostelName;
    private String categoryName;

    public HostelTable(int id, String tableName, String hostelName, String categoryName) {
        this.id = id;
        this.tableName = tableName;
        this.hostelName = hostelName;
        this.categoryName = categoryName;
    }

    // Getters (add setters if needed)
    public int getId() { return id; }
    public String getTableName() { return tableName; }
    public String getHostelName() { return hostelName; }
    public String getCategoryName() { return categoryName; }
}
