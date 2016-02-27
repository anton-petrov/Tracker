package mygps.android.tracker;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity  {

    Button btnShowLocation;

    // GPSTracker class
    MyGpsTracker gps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);

        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // create class object
                gps = new MyGpsTracker(MainActivity.this);

                // check if GPS enabled
                if(gps.canGetLocation()){

                    GPSPoint currentPosition = gps.getPosition();
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