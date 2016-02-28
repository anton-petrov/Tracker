package mygps.android.tracker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends Activity  {

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
}