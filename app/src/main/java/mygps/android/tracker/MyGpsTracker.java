package mygps.android.tracker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

class GPSPoint {
    public double Latitude;
    public double Longitude;
    public boolean isCorrect = false;
}


/**
 * @version 0.1
 * @author Petrov Anton
 */
public class MyGpsTracker extends Service implements LocationListener {

    private final Context context;

    Location location;
    protected double latitude;
    protected double longitude;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5; // 5 secs

    // Declaring a Location Manager
    protected LocationManager locationManager;
    private volatile String nmeaString;

    public String getNMEA() {
        return nmeaString;
    }

    protected boolean isGPSEnabled() {
        if (locationManager != null)
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        else
            return false;
    }

    protected boolean isNetworkEnabled() {
        if (locationManager != null)
            return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        else
            return false;
    }

    public MyGpsTracker(Context context) {
        this.context = context;
        nmeaString = "";
        getLocation();
    }

    private boolean nmeaListenerAdded = false;

    public Location getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            if (!isGPSEnabled()) {
            } else {
                if(!nmeaListenerAdded) {
                    locationManager.addNmeaListener(new GpsStatus.NmeaListener() {
                        public void onNmeaReceived(long timestamp, String nmea) {
                            Log.d("NMEA", nmea);
                            nmeaString = nmea;
                        }
                    });
                    nmeaListenerAdded = true;
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled()) {
                    if (locationManager != null) {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            return null;
                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            locationManager.removeUpdates(MyGpsTracker.this);
        }
    }

    public double getLatitude(){
        location = getLocation();
        if(location != null){
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude(){
        location = getLocation();
        if(location != null){
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public GPSPoint getPosition() {
        location = getLocation();
        GPSPoint point = new GPSPoint();
        if(location != null) {
            point.Longitude = location.getLongitude();
            point.Latitude = location.getLatitude();
            point.isCorrect = true;
        }
        return point;
    }
    /**
     * Function to check GPS enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return isGPSEnabled();
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("GPS Settings");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}