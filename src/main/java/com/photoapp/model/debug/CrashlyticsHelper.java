package com.photoapp.model.debug;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

public class CrashlyticsHelper {

    public static void setLatLon(double lat, double lon) {
        Crashlytics.setDouble("lat", lat);
        Crashlytics.setDouble("lon", lon);

        Answers.getInstance().logCustom(
                new CustomEvent("Location")
                        .putCustomAttribute("latlon", "" + lat + "x" + lon)

        );
    }

    public static void setGeoname(double lat, double lon, String name) {
        Answers.getInstance().logCustom(
                new CustomEvent("Geoname")
                        .putCustomAttribute("geoname", "" + lat + "x" + lon +"x" +name)
        );
        if(name != null) {
            Crashlytics.setString("geoname", name);
        } else {
            Crashlytics.setString("geoname", "");
        }
    }

    public static void setInternetExist(boolean exist) {
        Crashlytics.setBool("internetConnectExist", exist);
        Answers.getInstance().logCustom(
                new CustomEvent("internet exist")
                        .putCustomAttribute("exist", exist?"true":"false")
        );
    }
}
