package com.android.tvapp.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

public class Utils {

    public static final boolean USE_TEST_MODE = true;
    public static final String TASK_COMPLETE = "com.android.tvapp.intent.action.TASK_COMPLETE";
    public static final String FINISH_ACTIVITY = "com.android.tvapp.intent.action.FINISH_ACTIVITY";
    public static final String NONEW_VERSION = "com.android.tvapp.intent.action.NONEW_VERSION";
    
    public static final String UPGRADE_URL = "http://218.92.26.6:8000/multimedia/upload/version/update.json";
    public static final String TASKLIST_URL = "http://218.92.26.6:8000/multimedia/upload/config/task.json";

    public static String getIpAddress() {
        String ipaddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // �������õ�����ӿ�
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// �õ�ÿһ������ӿڰ󶨵�����ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // ����ÿһ���ӿڰ󶨵�����ip
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
