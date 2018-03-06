package com.hichip;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * WiFi配置帮助类，从demo中移植而来
 */
public class HiSmartWifiSetHelper {

    public static class ConnectedWiFiInfo {
        private String mSsid;
        private byte mAuthMode;

        ConnectedWiFiInfo(String ssid, byte AuthMode) {
            this.mSsid = ssid;
            this.mAuthMode = AuthMode;
        }

        public String getSsid() {
            return mSsid;
        }

        public byte getAuth() {
            return mAuthMode;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static ConnectedWiFiInfo getConnectedWifiInfo(Context context) throws HiSmartException {
        String ssid;
        Byte authMode;

        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (mWifiManager == null) {
            throw new HiSmartWifiNotFound("无法获取WifiManager");
        }

        if (!mWifiManager.isWifiEnabled()) {
            throw new HiSmartWifiNotEnabled("WiFi未开启");
        }

        WifiInfo WifiInfo = mWifiManager.getConnectionInfo();

        ssid = WifiInfo.getSSID();
        int ssidLen = ssid.length();
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssidLen - 1);
        }

        List<ScanResult> scanResults = mWifiManager.getScanResults();
        for (int i = 0, len = scanResults.size(); i < len; i++) {
            ScanResult scanResult = scanResults.get(i);
            if (scanResult.SSID.equals(ssid)) {
                authMode = parseAuthMode(scanResult);
                return new ConnectedWiFiInfo(ssid, authMode);
            }
        }

        throw new HiSmartWifiNotFound("未找到连接的WiFi信息");
    }

    private static byte parseAuthMode(ScanResult scanResult) {
        String authModeString;
        byte authMode;

        Log.i("HiSmartWifiSetHelper", "WIFI info = " + scanResult.capabilities);

        boolean WpaPsk = scanResult.capabilities.contains("WPA-PSK");
        boolean Wpa2Psk = scanResult.capabilities.contains("WPA2-PSK");
        boolean Wpa = scanResult.capabilities.contains("WPA-EAP");
        boolean Wpa2 = scanResult.capabilities.contains("WPA2-EAP");
        boolean Tkip = scanResult.capabilities.contains("TKIP");
        boolean AES = scanResult.capabilities.contains("CCMP");

        if (scanResult.capabilities.contains("WEP")) {
            authModeString = "OPEN-WEP";
            authMode = HiSmartWifiSet.AuthModeWEPOPEN;
        } else if (WpaPsk && Wpa2Psk && AES && Tkip) {// WPAPSK WPA2PSK Auto
            authModeString = "WPA-PSK WPA2-PSK TKIP AES";
            authMode = HiSmartWifiSet.AuthModeWPA1PSKWPA2PSKTKIPAES;
        } else if (WpaPsk && Wpa2Psk && Tkip) {
            authModeString = "WPA-PSK WPA2-PSK TKIP";
            authMode = HiSmartWifiSet.AuthModeWPA1PSKWPA2PSKTKIP;
        } else if (WpaPsk && Wpa2Psk && AES) {
            authModeString = "WPA-PSK WPA2-PSK AES";
            authMode = HiSmartWifiSet.AuthModeWPA1PSKWPA2PSKAES;
        } else if (Wpa2Psk && AES && Tkip) { //WPA2PSK
            authModeString = "WPA2-PSK TKIP";
            authMode = HiSmartWifiSet.AuthModeWPA2PSKTKIPAES;
        } else if (Wpa2Psk && Tkip) {
            authModeString = "WPA2-PSK TKIP";
            authMode = HiSmartWifiSet.AuthModeWPA2PSKTKIP;
        } else if (Wpa2Psk && AES) {
            authModeString = "WPA2-PSK AES";
            authMode = HiSmartWifiSet.AuthModeWPA2PSKAES;
        } else if (WpaPsk && AES && Tkip) {//WPAPSK
            authModeString = "WPA-PSK TKIP";
            authMode = HiSmartWifiSet.AuthModeWPAPSKTKIPAES;
        } else if (WpaPsk && Tkip) {
            authModeString = "WPA-PSK TKIP";
            authMode = HiSmartWifiSet.AuthModeWPAPSKTKIP;
        } else if (WpaPsk && AES) {
            authModeString = "WPA-PSK AES";
            authMode = HiSmartWifiSet.AuthModeWPAPSKAES;
        } else if (Wpa && Wpa2) { //WPA-EAP  WPA-EAP2 AUTO
            authModeString = "WPA-EAP WPA2-EAP";
            authMode = HiSmartWifiSet.AuthModeWPA1EAPWPA2EAP;
        } else if (Wpa2) { //WPA-EAP2
            authModeString = "WPA2-EAP";
            authMode = HiSmartWifiSet.AuthModeWPA2EAP;
        } else if (Wpa) { //WPA-EAP
            authModeString = "WPA-EAP";
            authMode = HiSmartWifiSet.AuthModeWPAEAP;
        } else { //Open
            authModeString = "OPEN";
            authMode = HiSmartWifiSet.AuthModeOpen;
        }

        Log.i("HiSmartWifiSetHelper", "Auth Mode  = " + authModeString);
        return authMode;
    }

    /**
     * 配置设备连接当前ssid
     */
    public static int start(Context context, String password) throws HiSmartException {
        ConnectedWiFiInfo connectedWiFiInfo = getConnectedWifiInfo(context);
        return HiSmartWifiSet.HiStartSmartConnection(connectedWiFiInfo.getSsid(), password, connectedWiFiInfo.getAuth());
    }

    /**
     * 停止配置
     */
    public static int stop() {
        return HiSmartWifiSet.HiStopSmartConnection();
    }

    public static class HiSmartException extends Exception {
        HiSmartException(String msg) {
            super(msg);
        }
    }

    public static class HiSmartWifiNotEnabled extends HiSmartException {
        HiSmartWifiNotEnabled(String msg) {
            super(msg);
        }
    }

    public static class HiSmartWifiNotFound extends HiSmartException {
        HiSmartWifiNotFound(String msg) {
            super(msg);
        }
    }
}
