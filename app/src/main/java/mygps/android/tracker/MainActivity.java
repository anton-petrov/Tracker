package mygps.android.tracker;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends Activity implements LocationListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), false);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    //Request location updates:
                    locationManager.requestLocationUpdates(provider, 400, 1, this);
                }

            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.

            }
            return;
        }
    }

    Button btnShowLocation, btnStartTracking, btnClose;

    boolean isTrackingStarted = false;
    Thread gpsThread;

    // GPSTracker class
    private volatile MyGpsTracker gps;
    private volatile GPSPoint currentPosition = null;

    private static void sendUDPMessage(String message, String address, int port) throws Exception
    {
        int msg_length = message.length();
        byte[] messageB = message.getBytes();
        DatagramSocket socket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(address);
        Log.d("IP", IPAddress.getHostAddress());
        DatagramPacket packet = new DatagramPacket(messageB, msg_length, IPAddress, port);
        try
        {
            socket.send(packet);
        }
        catch(Exception e) {

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gps = new MyGpsTracker(MainActivity.this);

        checkLocationPermission();

        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        btnStartTracking = (Button) findViewById(R.id.btnStartTracking);
        btnClose = (Button) findViewById(R.id.btnClose);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnStartTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTrackingStarted) {
                    gpsThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                String message = "";
                                synchronized (gps) {
                                    if (gps.canGetLocation()) {
                                        currentPosition = gps.getPosition();
                                        message = gps.getNMEA();
                                    }
                                }
                                //if (currentPosition != null) {
                                //currentPosition.Latitude + ";" + currentPosition.Longitude + "\n";
                                try {
                                    Log.d("IP", message);
                                    // 91.195.230.75
                                    sendUDPMessage(message, "91.195.230.75", 11111);
                                    Thread.sleep(5000);
                                } catch (Exception e) {

                                }
                                //}
                            }
                        }
                    });
                    gpsThread.start();
                    isTrackingStarted = true;
                    btnStartTracking.post(new Runnable() {
                        @Override
                        public void run() {
                            btnStartTracking.setText(R.string.btn_stop);
                        }
                    });
                } else {
                    gpsThread.interrupt();
                    isTrackingStarted = false;
                    btnStartTracking.post(new Runnable() {
                        @Override
                        public void run() {
                            btnStartTracking.setText(R.string.btn_start);
                        }
                    });
                }
            }
        });

        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if GPS enabled
                if (gps.canGetLocation()) {
                    if (currentPosition == null) {
                        Log.e("ERROR", "GPS is not ready!");
                        Toast.makeText(getApplicationContext(), "GPS is not ready!", Toast.LENGTH_LONG).show();
                        return;
                    } else {

                        Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " +
                                        currentPosition.Latitude + "\nLong: " +
                                        currentPosition.Longitude +
                                        "\nNMEA: " + gps.getNMEA() + "\n",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                    }

            }
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location != null) {
            Log.d("GPS", String.format("%f %f", location.getLatitude(), location.getLongitude()));
        }
    }

}