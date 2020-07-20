package com.crewcloud.crewmain.datamodel;


import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.util.PreferenceUtilities;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginDto {
    public String userID;
    public String FullName;
    public int Id;
    public String session;
    public String avatar;
    public String NameCompany;
    public String MailAddress;
    public int CompanyNo;

    public PreferenceUtilities prefs = CrewCloudApplication.getInstance().getPreferenceUtilities();

    public LoginDto() {
        prefs = CrewCloudApplication.getInstance().getPreferenceUtilities();
    }
}