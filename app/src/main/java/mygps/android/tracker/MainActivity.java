package mygps.android.tracker;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.*;
import java.io.*;

public class MainActivity extends Activity  {

    Button btnShowLocation, btnStartTracking;

    boolean isTrackingStarted = false;
    Thread gpsThread;

    // GPSTracker class
    private volatile MyGpsTracker gps;

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
    private volatile GPSPoint currentPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        btnStartTracking = (Button) findViewById(R.id.btnStartTracking);

        btnStartTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gps = new MyGpsTracker(MainActivity.this);
                if (!isTrackingStarted)
                {
                    gpsThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                synchronized (gps) {
                                    if (gps.canGetLocation()) {
                                        currentPosition = gps.getPosition();
                                    }
                                }
                                if (currentPosition != null) {
                                    String message = currentPosition.Latitude + ";" + currentPosition.Longitude + "\n";
                                    try {
                                        Log.d("IP", message);
                                        // 91.195.230.75
                                        sendUDPMessage(message, "91.195.230.75", 11111);
                                        Thread.sleep(5000);
                                    } catch (Exception e) {

                                    }
                                }
                            }
                        }
                    });
                    gpsThread.start();
                    isTrackingStarted = true;
                }
                else {
                    gpsThread.interrupt();
                    isTrackingStarted = false;
                }
            }
        });
        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0)  {

                // check if GPS enabled
                if(gps.canGetLocation()){

                    if(!currentPosition.isCorrect) {
                        Log.e("ERROR", "GPS is not ready!");
                        Toast.makeText(getApplicationContext(), "GPS is not ready!", Toast.LENGTH_LONG);
                        return;
                    }
                    else {

                        Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " +
                                        currentPosition.Latitude + "\nLong: " +
                                        currentPosition.Longitude +
                                        "\nNMEA: " + gps.getNMEA() + "\n",
                                Toast.LENGTH_LONG).show();
                    }
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }

            }
        });
    }
}