package com.crewcloud.crewmain.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.util.PreferenceUtilities;


public class AccountReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String senderPackageName = intent.getExtras().getString("senderPackageName");

        if (!context.getPackageName().equals(senderPackageName)) {
            Intent intentReceive = new Intent();
            intentReceive.setAction("com.dazone.crewcloud.account.receive");
            intentReceive.putExtra("senderPackageName", context.getPackageName());
            intentReceive.putExtra("receiverPackageName", senderPackageName);

            PreferenceUtilities pref = CrewCloudApplication.getInstance().getPreferenceUtilities();
            String userID = pref.getCurrentUserID();
            String companyID = rootLinkToDomain(pref.getCurrentCompanyDomain());

            intentReceive.putExtra("companyID", companyID);
            intentReceive.putExtra("userID", userID);
            intentReceive.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

            context.sendBroadcast(intentReceive);
        }
    }

    private String rootLinkToDomain(String server_site) {
        String result = server_site;
        result = result.replace("http://", "");
        result = result.replace("www.", "");

        if (result.contains(".bizsw.co.kr")) {
            result = result.split(":")[0];
        } else {
            result = result.replace(".crewcloud.net", "");
        }

        return result;
    }
}