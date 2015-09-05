package com.android.tvapp.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.conn.util.InetAddressUtils;

public class Utils {

    public static final boolean USE_TEST_MODE = false;
    public static final String TASK_COMPLETE = "com.android.tvapp.intent.action.TASK_COMPLETE";
    public static final String FINISH_ACTIVITY = "com.android.tvapp.intent.action.FINISH_ACTIVITY";
    public static final String NONEW_VERSION = "com.android.tvapp.intent.action.NONEW_VERSION";

    private static final String INTERNET_URL = "http://218.92.26.6:8011/";
    private static final String LAN_URL = "http://192.168.5.254:8080/";
    private static final String HOST_URL = LAN_URL;
    @Deprecated
    public static final String UPGRADE_URL = HOST_URL + "multimedia/upload/version/update.json";
    @Deprecated
    public static final String TASKLIST_URL = HOST_URL + "multimedia/upload/config/task.json";

    public static final String URL_UPGRADE = HOST_URL + "multimedia/config/upgrade.do";
    public static final String URL_TASKLIST = HOST_URL + "multimedia/config/task.do";
    public static final String URL_POLL = HOST_URL + "multimedia/config/poll.do";

    public static String genParamUrl(String apiUrl, Map<String, String> params) {
        params = genCommonParams(params);
        StringBuilder sb = new StringBuilder();
        sb.append(apiUrl + "?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key);
            sb.append("=");
            sb.append(value);
            sb.append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static String genParamUrl(String apiUrl) {
        Map<String, String> params = genCommonParams(null);
        StringBuilder sb = new StringBuilder();
        sb.append(apiUrl + "?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key);
            sb.append("=");
            sb.append(value);
            sb.append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private static Map<String, String> genCommonParams(Map<String, String> hashMap) {
        if (hashMap == null) {
            hashMap = new HashMap<String, String>();
        }
        hashMap.put("ip", getIpAddress());
        return hashMap;
    }

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
