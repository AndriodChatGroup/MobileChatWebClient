package com.example.dell_pc.mobilechatwebclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class GPSDemoActivity extends Activity
{
    private static final String TAG = "GpsActivity";
    private LocationManager locationManager;
    private EditText editText;
    private LocationListener locationListener = getLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpsdemo);

        // TODO: 2018/8/8 editText
//        editText = findViewById(R.id.editText);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)))
        {
            Toast.makeText(this, "请打开网络或GPS定位功能！", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }

        try
        {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null)
            {
                Log.d(TAG, "onCreate.location = null");
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            Log.d(TAG, "onCreate.location = " + location);
            updateView(location);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 5, locationListener);
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy()
    {
        try
        {
            locationManager.removeUpdates(locationListener);
        }
        catch (SecurityException e)
        {

        }
        super.onDestroy();
    }

    private LocationListener getLocationListener()
    {
        return new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                Log.d(TAG, "onProviderDisabled.location = " + location);
                updateView(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle)
            {
                Log.d(TAG, "onStatusChanged() called with " + "provider = [" + s + "], status = [" + i + "], extras = [" + bundle + "]");
                switch (i)
                {
                    case LocationProvider.AVAILABLE:
                        Log.i(TAG, "AVAILABLE");
                        break;
                    case LocationProvider.OUT_OF_SERVICE:
                        Log.i(TAG, "OUT_OF_SERVICE");
                        break;
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        Log.i(TAG, "TEMPORARILY_UNAVAILABLE");
                        break;
                }
            }

            @Override
            public void onProviderEnabled(String s)
            {
                Log.d(TAG, "onProviderEnable() called with " + "provider = [" + s + "]");
                try
                {
                    Location location = locationManager.getLastKnownLocation(s);
                    Log.d(TAG, "onProviderDisable.location = " + location);
                    updateView(location);
                }
                catch (SecurityException e)
                {

                }
            }

            @Override
            public void onProviderDisabled(String s)
            {
                Log.d(TAG, "onProviderDisabled() called with " + "provider = [" + s + "]");
            }
        };
    }

    private void updateView(Location location)
    {
        Geocoder gc = new Geocoder(this);
        List<Address> addresses;
        String msg = "";
        Log.d(TAG, "updateView.location = " + location);
        if (location != null)
        {
            try
            {
                addresses = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                Log.d(TAG, "updateView.address = " + addresses);
                if (addresses.size() > 0)
                {
                    msg += addresses.get(0).getAdminArea().substring(0, 2);
                    msg += " " + addresses.get(0).getLocality().substring(0, 2);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            editText.setText("定位到位置：\n");
            editText.append(msg);
            editText.append("\n经度：");
            editText.append(String.valueOf(location.getLongitude()));
            editText.append("\n纬度：");
            editText.append(String.valueOf(location.getLatitude()));
        }
        else
        {
            editText.getEditableText().clear();
            editText.setText("定位中");
        }
    }
}
