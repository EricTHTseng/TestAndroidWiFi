package tw.com.fourfree.testwifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private List<ScanResult> mScanResultList;
    private List<AccessPoint> mAccessPointList = new ArrayList<AccessPoint>();
    private WifiManager mWifiManager;
    private ListView mListView;
    private SimpleAdapter mAdapter;
    private Button mScanButton;
    private static final String PASSWORD = "4freedraytek";

    private AdapterView.OnItemClickListener mOnListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            AccessPoint accessPoint = mAccessPointList.get(position);

            // If the selected AccessPoint is not Ours, do nothing, return
            if (!(accessPoint.getSSID().contains("4FRee Headquarter") || accessPoint.getSSID().contains("CanauxMedia"))) {
                return;
            }


            Log.d("Yeh Yeh", "on click set selected AP:" + accessPoint);
            SelectedAP.setSelectedAP(accessPoint);

            Log.d("Yeh Yeh", "call disconnect 222222");

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                if (!mWifiManager.disconnect()) {
                    Log.d("CMWang", "disconnect fail");
                }
            }
            else {
                Log.d("Yeh Yeh", "sup disconnected run");
                State.s_bConnectedFinished = false;

                if (SelectedAP.getSelectedAP() == null) {
                    Log.d("Yeh Yeh", "selected AP null");
                    return;
                }

                Log.d("Yeh Yeh", "before m_bReconnecting:" + State.s_bReconnecting);

                if (State.s_bReconnecting) {
                    return;
                }

                State.s_bReconnecting = true;

                Log.d("Yeh Yeh", "after m_bReconnecting:" + State.s_bReconnecting);

                Log.d("Yeh Yeh", "enabling and reconnecting!!!!!!!!!!!!");
                WifiConfiguration config = new WifiConfiguration();
                config.BSSID = SelectedAP.getSelectedAP().getBSSID();
                config.SSID = "\"" + SelectedAP.getSelectedAP().getSSID() + "\"";

                if (SelectedAP.getSelectedAP().getSSID().contains("4FRee")) {
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                } else {
                    config.preSharedKey = "\"" + PASSWORD + "\"";
                }

                mWifiManager.addNetwork(config);
                mWifiManager.saveConfiguration();

                List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).BSSID != null && list.get(i).BSSID.equals(config.BSSID)) {
                        mWifiManager.enableNetwork(list.get(i).networkId, true);
                        mWifiManager.reconnect();
                        break;
                    }
                }
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Yeh Yeh", "intent:" + intent.getAction());

            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                mAccessPointList.clear();
                mScanResultList = mWifiManager.getScanResults();
                for (int i = 0; i < mScanResultList.size(); i++) {
                    String strBSSID = mScanResultList.get(i).BSSID;
                    String strSSID = mScanResultList.get(i).SSID;
                    String strLevel = Integer.toString(mScanResultList.get(i).level);

                    AccessPoint accessPoint = new AccessPoint(strBSSID, strSSID, strLevel);
                    mAccessPointList.add(accessPoint);
                }
                mAdapter.notifyDataSetChanged();
            } else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                NetworkInfo.State state = networkInfo.getState();

                Log.d("Yeh Yeh", "state:" + state);

                if (state == NetworkInfo.State.CONNECTED) {
                    String connectingToSsid = wifiManager.getConnectionInfo().getSSID().replace("\"", "");
                    String connectingToBSsid = wifiManager.getConnectionInfo().getBSSID();
                    int nConnectingNetworkId = wifiManager.getConnectionInfo().getNetworkId();

                    if (connectingToBSsid == null || connectingToBSsid.isEmpty()) {
                        return;
                    }

                    Log.d("Yeh Yeh", "connected ssid:" + connectingToSsid);
                    Log.d("Yeh Yeh", "connected bssid:" + connectingToBSsid);

                    if(State.s_bConnectedFinished) {
                        return;
                    }

                    State.s_bConnectedFinished = true;
                    State.s_bReconnecting = false;
                    SelectedAP.setSelectedAP(null);
                    Log.d("Yeh Yeh", "set selected AP null finished");

                    // Remove others
                    List<WifiConfiguration> savedConfig = wifiManager.getConfiguredNetworks();
                    for (int i = 0; i < savedConfig.size(); i++) {
                        if (savedConfig.get(i).networkId != nConnectingNetworkId) {
                            // Remove this (Check our SSID List, if the SSID match our SSID List, remove it
                            if (savedConfig.get(i).SSID.contains("4FRee Headquarter") ||
                                    savedConfig.get(i).SSID.contains("CanauxMedia")) {
                                wifiManager.removeNetwork(savedConfig.get(i).networkId);
                            }
                        }
                    }


                }
                if (state == NetworkInfo.State.DISCONNECTED) {
                    Log.d("Yeh Yeh", "sup disconnected run");
                    State.s_bConnectedFinished = false;

                    if (SelectedAP.getSelectedAP() == null) {
                        Log.d("Yeh Yeh", "selected AP null");
                        return;
                    }

                    Log.d("Yeh Yeh", "before m_bReconnecting:" + State.s_bReconnecting);

                    if (State.s_bReconnecting) {
                        return;
                    }

                    State.s_bReconnecting = true;

                    Log.d("Yeh Yeh", "after m_bReconnecting:" + State.s_bReconnecting);

                    Log.d("Yeh Yeh", "enabling and reconnecting!!!!!!!!!!!!");
/*
                    // enable and reconnect
                    List<WifiConfiguration> beforeConfiguredList = wifiManager.getConfiguredNetworks();
                    boolean bFound = false;
                    for (int i = 0; i < beforeConfiguredList.size(); i++) {

                        Log.d("Yian Yian", "BSSID:" + beforeConfiguredList.get(i).BSSID);

                        if (beforeConfiguredList.get(i).BSSID != null
                                && beforeConfiguredList.get(i).BSSID.equals(SelectedAP.getSelectedAP().getBSSID())) {
                            wifiManager.enableNetwork(beforeConfiguredList.get(i).networkId, true);
                            wifiManager.reconnect();
                            bFound = true;
                            break;
                        }
                    }
                    */

                    //  if (!bFound) {
                    WifiConfiguration config = new WifiConfiguration();
                    config.BSSID = SelectedAP.getSelectedAP().getBSSID();
                    config.SSID = "\"" + SelectedAP.getSelectedAP().getSSID() + "\"";

                    if (SelectedAP.getSelectedAP().getSSID().contains("4FRee")) {
                        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    } else {
                        config.preSharedKey = "\"" + PASSWORD + "\"";
                    }

                    wifiManager.addNetwork(config);
                    wifiManager.saveConfiguration();

                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).BSSID != null && list.get(i).BSSID.equals(config.BSSID)) {
                            wifiManager.enableNetwork(list.get(i).networkId, true);
                            wifiManager.reconnect();
                            break;
                        }
                    }
                    // }
                }
            }
        }
    };

    private View.OnClickListener mOnScanClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mWifiManager.startScan();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiManager.setWifiEnabled(true);

        mScanButton = (Button) findViewById(R.id.scan_button);
        mScanButton.setOnClickListener(mOnScanClickListener);

        mListView = (ListView) findViewById(R.id.listview);
        mAdapter = new SimpleAdapter(this, R.layout.layout_listitem, mAccessPointList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mOnListItemClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
