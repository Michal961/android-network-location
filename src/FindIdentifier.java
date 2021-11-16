package com.example.currentlocation;

import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import android.content.Context;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * The FindNetwork class implements an program that:
 * Finds the IP address of the device (network connection or WiFi connection)
 * Finds all the versions (IPv4+IPv6) -> for now only IPv4 in use.
 */


class FindIdentifier {
    private Context mContext; // the context of the current state of the application -> from MainActivity

    /**
     * constructor
     * @param mContext for setting context
    **/

    public FindIdentifier(Context mContext){
        this.mContext=mContext;
    }


    /**
     * Checks if the network is based on the device or on WIFI, returns the IP address
     * @return a Network Detect, or null
     */

    protected String  NetworkDetect() {
        boolean WIFI = false;
        boolean MOBILE = false;
        String  IPAddress=null;

        ConnectivityManager CM = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = CM.getAllNetworkInfo();
        for (NetworkInfo netInfo : networkInfo) {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
                if (netInfo.isConnected())
                    WIFI = true;
            if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                if (netInfo.isConnected())
                    MOBILE = true;
        }
        if(WIFI == true) {
            IPAddress = GetDeviceIpWiFiData();

        }
        if(MOBILE == true) {
            IPAddress = GetDeviceIpMobileData();
        }

        return  IPAddress;
    }

    /**
     * returns the IP address when the network based on the device
     * Finds all the address, currently returns only IPv4
     * @return a Network Detect, or null
     *
     *  @throws Exception if no more elements exist (enumeration)
     */

    private String GetDeviceIpMobileData(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface networkinterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkinterface.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String sAddr=inetAddress.getHostAddress().toString();
                        if (inetAddress instanceof Inet4Address) {
                            return sAddr;

                            //for future use ---> IPv6 :
                            //  int delim = sAddr.indexOf('%'); // drop ip6 port suffix "%"
                            //  return delim<0 ? sAddr : sAddr.substring(0, delim);
                        }

                    }
                }
            }
        } catch (Exception ex) {
            Log.e("Current IP", ex.toString());
        }
        return null;
    }


    /**
     * returns the IP address when the network based on WIFI
     * @return a Network Detect
     */

    private String GetDeviceIpWiFiData() {
        WifiManager wm = (WifiManager) mContext.getApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        @SuppressWarnings("deprecation")
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }
}
