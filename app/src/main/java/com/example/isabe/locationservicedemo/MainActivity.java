package com.example.isabe.locationservicedemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements LocationListener {

    /*For Log*/
    private static final String LOG_TAG = "com.location";

    /*Request code to check on in onRequestPermissionsResult*/
    private final int REQUEST_LOCATION_PERMISSION_CODE = 0;

    private TextView latitudeTextView, longitudeTextView;
    private LocationManager locationManager;
    private Location location;
    private double latitude;
    private double longitude;
    private Boolean getCanLocation = false;
    Boolean isGpsEnabled = false;
    Boolean isNetworkEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeTextView = (TextView) findViewById(R.id.latitude);
        longitudeTextView = (TextView) findViewById(R.id.longitude);
    }

    @Override
    protected void onResume() {
        super.onResume();

        retrieveLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUEST_LOCATION_PERMISSION_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    retrieveLocation();
                }
                else{
                    Toast.makeText(this, getString(R.string.persmission_location_denied), Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        //When the location changes, we update field values
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        latitudeTextView.setText(String.valueOf(latitude));
        longitudeTextView.setText(String.valueOf(longitude));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, getString(R.string.location_provider_enabled)+" :"+provider, Toast.LENGTH_SHORT).show();
        retrieveLocation();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, getString(R.string.location_provider_disabled)+" :"+provider, Toast.LENGTH_SHORT).show();
    }

    public void retrieveLocation() {

        //We store permission values
        int FineLocationPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int CoarseLocationPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (FineLocationPermissionCheck != PackageManager.PERMISSION_GRANTED && CoarseLocationPermissionCheck != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Here and for UX matters, we display a comprehensive message to the user explaining why we need to get his position
                showMessageOKCancel(getString(R.string.permission_location_rationale), new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION_CODE);
                    }
                });

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION_CODE);
            }

            return;
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        //If both gps and network are enabled, we try to retrieve the last known location
        if (isNetworkEnabled && isNetworkEnabled) {

            //We set the getcanlocation to true, else later, we will prompt the user to turn on the gps setting
            getCanLocation = true;

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.LOCATION_MIN_TIME_BETWEEN_UPDATES, Constants.LOCATION_MIN_DISTANCE, this);
                Location lastKnownPosition = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (lastKnownPosition != null) {
                    latitude = lastKnownPosition.getLatitude();
                    latitudeTextView.setText(String.valueOf(latitude));
                    longitude = lastKnownPosition.getLongitude();
                    longitudeTextView.setText(String.valueOf(longitude));
                }

            }

            if (isGpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.LOCATION_MIN_TIME_BETWEEN_UPDATES, Constants.LOCATION_MIN_DISTANCE, this);
                Location lastKnownPosition = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownPosition != null) {
                    latitude = lastKnownPosition.getLatitude();
                    latitudeTextView.setText(String.valueOf(latitude));
                    longitude = lastKnownPosition.getLongitude();
                    longitudeTextView.setText(String.valueOf(longitude));
                }
            }
        }
    }

    //Dialog to display rationale message that explains why we need location
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.common_ok_label), okListener)
                .setNegativeButton(getString(R.string.common_cancel_label), null)
                .create()
                .show();
    }
}
