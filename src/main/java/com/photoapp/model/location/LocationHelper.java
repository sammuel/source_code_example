package com.photoapp.model.location;


import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.pwittchen.networkevents.library.ConnectivityStatus;
import com.github.pwittchen.networkevents.library.event.ConnectivityChanged;
import com.photoapp.R;
import com.photoapp.controller.dialog.DialogHelper;
import com.photoapp.controller.dialog.ToastHelper;
import com.photoapp.controller.events.DialogEvent;
import com.photoapp.model.debug.CrashlyticsHelper;
import com.photoapp.model.debug.Log;
import com.photoapp.model.events.Bus;
import com.photoapp.model.internet.InternetHelper;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationManagerProvider;


/**
 * Class for working with locations (GPS and Network)
 */
public class LocationHelper {

    private static final String LOG_TAG = LocationHelper.class.getCanonicalName();
    public static final int MAX_ADDRESSES_GEOCODER_RESULTS = 10;
    public static final float UNDEFINED = 0.00000f;

    private static LocationHelper instance;

    private Context mContext;
    private SmartLocation smartLocation;
    private OnLocationUpdatedListener onLocationUpdatedListener;
    private Geocoder geocoder;

    private double lat = UNDEFINED;
    private double lon = UNDEFINED;
    private String geoName;
    private AsyncTask<Void, Void, Boolean> asyncTaskGeocoder;

    public static LocationHelper getInstatce(Context context) {
        if (instance == null) {
            instance = new LocationHelper(context);
        }
        return instance;
    }

    private LocationHelper(final Context context) {
        mContext = context;
        Bus.register(this);
        smartLocation = new SmartLocation.Builder(mContext).build();

        geocoder = new Geocoder(mContext, Locale.ENGLISH);


        CrashlyticsHelper.setLatLon(lat, lon);
        CrashlyticsHelper.setGeoname(lat, lon, "");
        onLocationUpdatedListener = new OnLocationUpdatedListener() {
            @Override
            public void onLocationUpdated(Location location) {


                lat = location.getLatitude();
                lon = location.getLongitude();
                startRetrieveAsyncAddressFromLocation(lat, lon);
                CrashlyticsHelper.setLatLon(lat, lon);
                String logStr = "New coordinates (" + lat + ", " + lon + ") " + " from location provider = " + location.getProvider();
                Log.w(LOG_TAG, logStr);
            }
        };

    }

    public void startLocation() {

        Log.w(LOG_TAG, "startLocation");
        lat = 0;
        lon = 0;
        geoName = null;

        if (isLocationEnabled()) {
            LocationParams params = LocationParams.NAVIGATION;
            smartLocation.location().continuous().config(params).start(onLocationUpdatedListener);
        } else {
            Bus.post(new DialogEvent(DialogEvent.DialogType.NO_LOCATION_SERVICES));

        }

    }

    public void stopLocation() {
        Log.w(LOG_TAG, "stopLocation");
        smartLocation.location().stop();
    }

    public void onEvent(ConnectivityChanged event) {
        if (event.getConnectivityStatus().equals(ConnectivityStatus.WIFI_CONNECTED_HAS_INTERNET)
                || event.getConnectivityStatus().equals(ConnectivityStatus.MOBILE_CONNECTED)) {
            if (isCoordinatesObtained()) {
                startRetrieveAsyncAddressFromLocation(lat, lon);
            }

        }
    }

    public boolean isLocationEnabled() {
        boolean enabled = smartLocation.location(new LocationManagerProvider()).state().locationServicesEnabled();
        return enabled;
    }

    public boolean isCoordinatesObtained() {
        boolean result;
        if (lon == 0 && lat == 0) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    private void startRetrieveAsyncAddressFromLocation(final double latitude, final double longitude) {

        boolean geoCoderAsyncRunned = isGeoCoderAsyncRunned();
        if (geoCoderAsyncRunned) {
            cancelGeoCoderAsync();
        }


        asyncTaskGeocoder = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Boolean aBool) {
                super.onPostExecute(aBool);

                if (aBool) {
                    if (geoName == null || geoName.equals("")) {
//                        ToastHelper.showToast(mContext, R.string.dialog_text_address_decoder_not_available);
                    } else {
                        String addressFoundFormat = mContext.getString(R.string.dialog_text_address_decoded);
                        ToastHelper.showToast(mContext, String.format(addressFoundFormat, geoName));
                    }
                    CrashlyticsHelper.setGeoname(lat, lon, geoName);
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {

                String oldGeoName = geoName;
                Boolean result = false;

                String newGeoName = null;
                newGeoName = retrieveGeoName(latitude, longitude);

                geoName = newGeoName;

                if (geoName != null) {
                    result = !geoName.equals(oldGeoName);
                }

                return result;
            }
        };
        asyncTaskGeocoder.execute();
    }

    public boolean isGeoCoderAsyncRunned() {

        boolean runned = false;

        if (asyncTaskGeocoder != null) {
            AsyncTask.Status status = asyncTaskGeocoder.getStatus();

            switch (status) {
                case FINISHED:
                    runned = false;
                    break;
                case PENDING:
                case RUNNING:
                    runned = true;
                    break;
            }
        }
        return runned;
    }

    public void cancelGeoCoderAsync() {

        if (asyncTaskGeocoder != null) {
            AsyncTask.Status status = asyncTaskGeocoder.getStatus();

            switch (status) {
                case FINISHED:
                    break;
                case PENDING:
                case RUNNING:
                    boolean mayInterruptIfRunning = true;
                    asyncTaskGeocoder.cancel(mayInterruptIfRunning);
                    break;
            }
        }
        asyncTaskGeocoder = null;
    }

    private String retrieveGeoName(double latitude, double longitude) {
        String newGeoName = null;
        try {
            List<Address> addressList = geocoder.getFromLocation(
                    latitude, longitude, MAX_ADDRESSES_GEOCODER_RESULTS);
            if (addressList != null && addressList.size() > 0) {
                newGeoName = retrieveCityAndCountry(addressList);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Unable connect to Geocoder " + e.getMessage());
        }
        return newGeoName;
    }

    private String retrieveCityAndCountry(List<Address> addresses) {
        String result = null;

        Hashtable<String, Integer> countryToCount = new Hashtable<String, Integer>();
        Hashtable<String, Integer> cityToCount = new Hashtable<String, Integer>();

        for (Address address : addresses) {
            String country = address.getCountryName();
            String city = address.getLocality();

            if (country != null) {
                int countCountry;
                if (countryToCount.containsKey(country)) {
                    countCountry = countryToCount.get(country);
                    countCountry++;
                } else {
                    countCountry = 1;
                }
                countryToCount.put(country, countCountry);
            }

            if (city != null) {
                int countCity;
                if (cityToCount.containsKey(city)) {
                    countCity = cityToCount.get(city);
                    countCity++;
                } else {
                    countCity = 1;
                }
                cityToCount.put(city, countCity);
            }

        }

        String city = null;
        String country = null;

        int counterMaxCity = 0;
        int counterMaxCountry = 0;

        for (Map.Entry<String, Integer> entry : countryToCount.entrySet()) {
            int count = entry.getValue();
            if (count > counterMaxCountry) {
                counterMaxCountry = count;
                country = entry.getKey();
            }
        }

        for (Map.Entry<String, Integer> entry : cityToCount.entrySet()) {
            int count = entry.getValue();
            if (count > counterMaxCity) {
                counterMaxCity = count;
                city = entry.getKey();
            }
        }

        if (city != null && country != null) {
            result = city + "_" + country;
        } else {
            if (city != null) {
                result = city;
            }

            if (country != null) {
                result = country;
            }
        }


        result = result.replace(' ', '_');
        Log.w(LOG_TAG, " Address founded = " + result + " for " + lat + " " + lon);
        return result;
    }

    public String getAddressFromLocationSync(final double latitude, final double longitude) throws IOException {

        String newGeoName = null;
        newGeoName = retrieveGeoName(latitude, longitude);

        return newGeoName;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getGeoName() {
        return geoName;
    }

    public void startGeoCoderAsync() {
        if(lat != UNDEFINED && lon != UNDEFINED) {
            startRetrieveAsyncAddressFromLocation(lat, lon);
        }
    }
}
