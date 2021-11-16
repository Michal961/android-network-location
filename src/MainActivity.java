package com.example.currentlocation;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.*;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.*;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The MainActivity program Activity implements an application that:
 * simply displays user location (according to NETWORK_PROVIDER only) on the screen and sends it to the connected server.
 *
 * {@link FindIdentifier} - Finding IP Address.
 * Finding Location - implemented in this class.
 */


public class MainActivity extends AppCompatActivity {
    private Socket socket;
    private final int PERMISSION_ID = 44; //  If >= 0, this code will be returned in onActivityResult() when the activity exits.
    private final String SERVER_URL = Configuration.SERVER; //change the string according to the server URL
    private String IPAddress;
    private Button btLocation,btSpotTracking;
    private TextView latitudeTextView, longitudeTextView, countryTextView, localityTextView, addressTextView, IPTextView, MacTextView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    String macAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //connect to the server
        serverConnection();

        //Initialization of the elements - find them
        btLocation = findViewById(R.id.bt_location);
        latitudeTextView = findViewById(R.id.text_view1);
        longitudeTextView = findViewById(R.id.text_view2);
        countryTextView = findViewById(R.id.text_view3);
        localityTextView = findViewById(R.id.text_view4);
        addressTextView = findViewById(R.id.text_view5);
        MacTextView = findViewById(R.id.text_view7);
        btSpotTracking=findViewById(R.id.bt_Stop_location);
        IPTextView=findViewById(R.id.text_view6);

        // initializing FusedLocationProviderClient object
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // initializing  locationRequest object
        locationRequest = LocationRequest.create();
        //receive location updates every 4-2 sec
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(2000);

        // PRIORITY_HIGH_ACCURACY - This will return the finest location available.
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //start
        startApp();
    }


    /**
     * Connection to the server with socket.io
     * @throws URISyntaxException if failed to connect
     */
    private void serverConnection() {
        try {
            socket = IO.socket(SERVER_URL);
            socket.connect();
            Log.d("success", "success to connect "+socket.toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.d("fail", "Failed to connect");
        }
    }

    /**
     * Starts the activity:
     * Finds the network (IP address)
     * Checking permissions when there is a click on a location button:
     * If all permissions are valid the location displayed and sent to the server
     * Otherwise, request for enable permissions
     */
    protected void startApp(){
        FindIdentifier network=new FindIdentifier(this);
        IPAddress = network.NetworkDetect();
        macAddress = getMacAddress();
        if (IPAddress!=null){
            IPTextView.setText(Html.fromHtml(
                    "<font> <b> YOUR IP : </b> <br></font>"
                            + IPAddress));

            MacTextView.setText(Html.fromHtml(
                    "<font> <b> YOUR MAC : </b> <br></font>"
                            + macAddress));

        }
        else { // not connected to the network
            IPTextView.setText(Html.fromHtml(
                    "<font> <b> Location not available - The device is not connected to the network</b></font>"));
            btLocation.setEnabled(false);
        }

        btLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    //PERMISSION GRANTED
                    if (isLocationEnabled()) {
                        //find location
                        getLocation();
                    } else {
                        Toast.makeText(MainActivity.this, "Please turn on your network location...", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                } else { //PERMISSION NOT GRANTED
                    // if permissions aren't available, request for permissions
                    requestPermissions();
                }
            }
        });
    }
    /**
     * Check for permissions
     * ACCESS_FINE_LOCATION - allows to access precise location.
     * @return true if you have the or false if not
     */
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if location is enabled by using NETWORK_PROVIDER only (not a GPS)
     * NETWORK_PROVIDER - this provider determines location based on nearby of cell tower and WiFi access points.
     * @return true if location is enabled or false if not
     */
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        /*   if you want the provider to be by GPR or by internet&GPS add :
             return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
             locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        */
    }

    /**
     * If there is no access for precise location - ask for an access
     * Opens the device setting (location access) to allow it
     */
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    /**
     * Send a location request according to locationRequest settings
     * Stops the tracking after clicking on Spot Tracking button
     * @see com.google.android.gms.location;
     */
    private void getLocation() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        //spot tracking
        btSpotTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                String macAddressToReset="{"+macAddress+"}";
                try {
                    JSONObject jsonMAC=new JSONObject(macAddressToReset);
                    socket.emit("ready to disconnect user", jsonMAC);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                btSpotTracking.setVisibility(View.INVISIBLE);
                btLocation.setEnabled(true);
                latitudeTextView.setVisibility(View.INVISIBLE);
                longitudeTextView.setVisibility(View.INVISIBLE);
                countryTextView.setVisibility(View.INVISIBLE);
                localityTextView.setVisibility(View.INVISIBLE);
                addressTextView.setVisibility(View.INVISIBLE);
                startApp();
            }
        });
    }

    /**
     * For each location received, do:
     * Send a Json with the IP, Longitude, Latitude to the server
     * Display the location (and more details) on screen - set the TextViews
     *
     * @param locationResult - > according to requestLocationUpdates
     * @throws JSONException if the parse fails or doesn't yield a JSONObject
     * @throws IOException if the network is unavailable or any other I/O problem
     */

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            if (locationResult == null) {
                return;
            }

            latitudeTextView.setVisibility(View.VISIBLE);
            longitudeTextView.setVisibility(View.VISIBLE);
            countryTextView.setVisibility(View.VISIBLE);
            localityTextView.setVisibility(View.VISIBLE);
            addressTextView.setVisibility(View.VISIBLE);

            for (Location location : locationResult.getLocations()) {
                Log.d("MainActivity", "onLocationResult: " + location.toString());
                String longitude="\"longitude\":" +"\""+location.getLongitude()+"\"";
                String latitude="\"latitude\":" +"\""+location.getLatitude()+"\"";

                macAddress = "\"macAddress\":" +"\""+getMacAddress()+"\"";
                Log.d("MainActivity", macAddress);
                String ip="\"ip\":" +"\""+IPAddress+"\"";

                try {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> address = geocoder.getFromLocation(
                            location.getLatitude(), location.getLongitude(), 1);
                    String addressLine = "\"address\":" +"\""+ address.get(0).getAddressLine(0)+"\"";
                    Log.d("MainActivity", "hgj" + addressLine);
                    String currentLocation="{"+ip+","+latitude+","+longitude+","+addressLine+","+macAddress+"}";
                    JSONObject jsonLocation=new JSONObject(currentLocation);

                   socket.emit("location", jsonLocation);

               } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> address = geocoder.getFromLocation(
                            location.getLatitude(), location.getLongitude(), 1);

                    //set latitude - first number is between 90 to -90. Show only five decimal places
                    latitudeTextView.setText(Html.fromHtml(
                            "<font> <b> Latitude : </b> <br></font>"
                                    + new DecimalFormat("##.#######").format(location.getLatitude())));

                    //set Longitude - first number is between 180 to -180. Show only five decimal places
                    longitudeTextView.setText(Html.fromHtml(
                            "<font> <b> Longitude : </b> <br></font>"
                                    + new DecimalFormat("###.#######").format(location.getLongitude())));

                    //set country
                    countryTextView.setText(Html.fromHtml(
                            "<font> <b> Country Name : </b> <br></font>"
                                    + address.get(0).getCountryName()));

                    //set Address
                    localityTextView.setText(Html.fromHtml(
                            "<font> <b> Locality : </b> <br></font>"
                                    + address.get(0).getLocality()));

                    //set Address
                    addressTextView.setText(Html.fromHtml(
                            "<font> <b> Address : </b> <br></font>"
                                    + address.get(0).getAddressLine(0)));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            btLocation.setEnabled(false);
            btSpotTracking.setVisibility(View.VISIBLE);
        }
    };

    public String getMacAddress(){
        try {
            List<NetworkInterface> networkInterfaceList = Collections.list(NetworkInterface.getNetworkInterfaces());

            String stringMac = "";

            for (NetworkInterface networkInterface:networkInterfaceList){
                if (networkInterface.getName().equalsIgnoreCase("wlan0")){
                    for (int i=0; i<networkInterface.getHardwareAddress().length;i++){
                        String stringMacByte = Integer.toHexString(networkInterface.getHardwareAddress()[i] & 0xFF);

                        if (stringMacByte.length() == 1){
                            stringMacByte = "0" + stringMacByte;
                        }

                        stringMac = stringMac + stringMacByte.toUpperCase() + ":";
                    }
                    break;
                }
            }

            return stringMac;


        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }
}
