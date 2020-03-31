package com.crewcloud.crewmain.datamodel;

public class Application {
    public int ApplicationNo;
    public String ProjectCode;
    public String ApplicationName;
    public String PackageName;
    public String totalUnreadCount="";

    public int getApplicationNo() {
        return ApplicationNo;
    }

    public void setApplicationNo(int applicationNo) {
        ApplicationNo = applicationNo;
    }

    public String getProjectCode() {
        return ProjectCode;
    }

    public void setProjectCode(String projectCode) {
        ProjectCode = projectCode;
    }

    public String getApplicationName() {
        return ApplicationName;
    }

    public void setApplicationName(String applicationName) {
        ApplicationName = applicationName;
    }

    public String getPackageName() {
        return PackageName;
    }

    public void setPackageName(String packageName) {
        PackageName = packageName;
    }

    public String getTotalUnreadCount() {
        return totalUnreadCount;
    }

    public void setTotalUnreadCount(String totalUnreadCount) {
        this.totalUnreadCount = totalUnreadCount;
    }
}
