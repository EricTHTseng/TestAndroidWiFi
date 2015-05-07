package tw.com.fourfree.testwifi;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;

/**
 * Created by eric on 2015/4/8.
 */
public class AccessPoint {

    private String mBSSID;

    private String mSSID;

    private String mSignal;

    public AccessPoint(String bssid, String ssid, String signal) {
        mBSSID = bssid;
        mSSID = ssid;
        mSignal = signal;
    }

    public String getBSSID() {
        return mBSSID;
    }

    public void setBSSID(String bssid) {
        mBSSID = bssid;
    }

    public String getSSID() {
        return mSSID;
    }

    public void setSSID(String ssid) {
        mSSID = ssid;
    }

    public String getSignal() {
        return mSignal;
    }

    public void setSignal(String signal) {
        mSignal = signal;
    }

}
