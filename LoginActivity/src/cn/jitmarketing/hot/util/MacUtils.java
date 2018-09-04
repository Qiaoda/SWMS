package cn.jitmarketing.hot.util;

import java.io.FileReader;
import java.io.Reader;
import org.apache.commons.lang3.exception.ExceptionUtils;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

public class MacUtils {
	/**
     * 获取失败默认返回值
     */
    public static final String ERROR_MAC_STR = "00:00:00:00:00:00";

    // Wifi 管理器
    private static WifiManager mWifiManager;

    /**
     * 实例化WifiManager对象
     *
     * @param context 当前上下文对象
     * @return
     */
    private static WifiManager getInstant(Context context) {
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }
        return mWifiManager;
    }

    /**
     * 开启wifi
     */
    public static void getStartWifiEnabled() {
        // 判断当前wifi状态是否为开启状态
        if (!mWifiManager.isWifiEnabled()) {
            // 打开wifi 有些设备需要授权
            mWifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 获取手机设备MAC地址
     * MAC地址：物理地址、硬件地址，用来定义网络设备的位置
     * modify by heliquan at 2018年1月17日
     *
     * @param context
     * @return
     */
    public static String getMobileMAC(Context context) {
        mWifiManager = getInstant(context);
        // 如果当前设备系统大于等于6.0 使用下面的方法
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.) {
            return getAndroidHighVersionMac();
        } else { // 当前设备在6.0以下
            return getAndroidLowVersionMac(mWifiManager);
        }*/
        return getAndroidLowVersionMac(mWifiManager);
    }

    /**
     * Android 6.0 以下设备获取mac地址 获取失败默认返回：02:00:00:00:00:00
     *
     * @param wifiManager
     * @return
     */
    @NonNull
    private static String getAndroidLowVersionMac(WifiManager wifiManager) {
        try {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String mac = wifiInfo.getMacAddress();
            if (TextUtils.isEmpty(mac)) {
                return ERROR_MAC_STR;
            } else {
                return mac;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.logOnFile("MacUtils:"+ExceptionUtils.getStackTrace(e));
            return ERROR_MAC_STR;
        }
    }

   

    public static String loadFileAsString(String fileName) throws Exception {
        FileReader reader = new FileReader(fileName);
        String text = loadReaderAsString(reader);
        reader.close();
        return text;
    }

    public static String loadReaderAsString(Reader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int readLength = reader.read(buffer);
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength);
            readLength = reader.read(buffer);
        }
        return builder.toString();
    }
}
