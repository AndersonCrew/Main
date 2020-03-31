package com.crewcloud.crewmain.datamodel;


import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.util.PreferenceUtilities;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Login_v2_Result {
    public String SessionID;
    public String Domain;

    public String userID;
    public String FullName;
    public int Id;
    public String session;
    public String avatar;
    public String Password;
    public int PermissionType;
    public String NameCompany;
    public String MailAddress;
    public int CompanyNo;
    public String EntranceDate;
    public String BirthDate;
    public String CellPhone;
    public String CompanyPhone;

    public PreferenceUtilities prefs = CrewCloudApplication.getInstance().getPreferenceUtilities();

    public Login_v2_Result() {
        prefs = CrewCloudApplication.getInstance().getPreferenceUtilities();
    }
}