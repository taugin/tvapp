package com.android.tvapp.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

public class Utils {

    public static final boolean USE_TEST_MODE = false;
    public static final String TASK_COMPLETE = "com.android.tvapp.intent.action.TASK_COMPLETE";
    public static final String FINISH_ACTIVITY = "com.android.tvapp.intent.action.FINISH_ACTIVITY";
    public static final String NONEW_VERSION = "com.android.tvapp.intent.action.NONEW_VERSION";

    private static final String INTERNET_URL = "http://218.92.26.6:8000/";
    private static final String LAN_URL = "http://192.168.5.254:8080/";
    private static final String HOST_URL = LAN_URL;
    public static final String UPGRADE_URL = HOST_URL + "multimedia/upload/version/update.json";
    public static final String TASKLIST_URL = HOST_URL + "multimedia/upload/config/task.json";

    public static String getIpAddress() {
        String ipaddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                while (inet.hasMoreElements()) {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(ip
                                    .getHostAddress())) {
                        ipaddress = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return ipaddress;
    }
}
