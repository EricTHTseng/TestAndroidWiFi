package tw.com.fourfree.testwifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

            boolean bFound = false;

            AccessPoint accessPoint = mAccessPointList.get(position);

            List<WifiConfiguration> preprocessList = mWifiManager.getConfiguredNetworks();

            for(int i = 0; i < preprocessList.size(); i++) {
                // Remove connected wifi before
                if(preprocessList.get(i).SSID == null) {
                    // Remove this
                    mWifiManager.removeNetwork(preprocessList.get(i).networkId);
                    mWifiManager.saveConfiguration();
                    continue;
                }
            }

            List<WifiConfiguration> beforeList = mWifiManager.getConfiguredNetworks();

            Log.d("Sexy", "before list size:" + beforeList.size());

            for(int i = 0; i < beforeList.size(); i++) {

                Log.d("Sexy", "before list BSSID:" + beforeList.get(i).BSSID);
                Log.d("Sexy", "before list SSID:" + beforeList.get(i).SSID);
                Log.d("Sexy", "before list toString:" + beforeList.get(i).toString());
                Log.d("Sexy", "before list preSharedKey:" + beforeList.get(i).preSharedKey);
                Log.d("Sexy", "before list networkId:" + beforeList.get(i).networkId);
                Log.d("Sexy", "before list status:" + beforeList.get(i).status);
                Log.d("Sexy", "before list describeContents:" + beforeList.get(i).describeContents());
               // Log.d("Sexy", "before list SSID:" + beforeList.get(i).SSID);
                Log.d("Sexy", "accessPoint BSSID:" + accessPoint.getBSSID());

                if(beforeList.get(i).BSSID != null
                        && beforeList.get(i).BSSID.equals(accessPoint.getBSSID())) {
                    if(!mWifiManager.disconnect()) {
                        Log.d("CMWang", "disconnect fail");
                    }
                    if(!mWifiManager.enableNetwork(beforeList.get(i).networkId, true)) {
                        Log.d("CMWang", "enableNetwork fail");
                    }
                    if(!mWifiManager.reconnect()) {
                        Log.d("CMWang", "reconnect fail");
                    }
                    Log.d("CMWang", "same this!!!!!!!!!!!!!!!!");
                    Log.d("CMWang", "same BSSID:" + beforeList.get(i).BSSID);
                   // Log.d("CMWang", "same SSID:" + beforeList.get(i).SSID);
                    bFound = true;
                    break;
                }
            }

            if(!bFound) {
                WifiConfiguration config = new WifiConfiguration();
                config.BSSID = accessPoint.getBSSID();
                config.SSID = "\"" + accessPoint.getSSID() + "\"";

                Log.d("ERic Eric", "BSSID:" + config.BSSID);

                //config.SSID = "\"" + accessPoint.getSSID() + "\"";
                //config.SSID = "\"" + "Eric Tseng Test Hotspot" + "\"";

                ///////////
                if(accessPoint.getSSID().contains("4FRee")) {
                    Log.d("Eric Eric", "this is 4FRee");
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                }
                else {
                    Log.d("Eric Eric", "this is not 4FRee");
                    config.preSharedKey = "\"" + PASSWORD + "\"";
                }
                //////////
                mWifiManager.addNetwork(config);
                mWifiManager.saveConfiguration();

                List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
                Log.d("CMWang", "after configured size:" + list.size());
                for(int i = 0; i < list.size(); i++) {

                    Log.d("CMWang", "BSSID:" + list.get(i).BSSID);
                    Log.d("CMWang", "SSID:" + list.get(i).SSID);

                    if(list.get(i).BSSID != null
                            && list.get(i).BSSID.equals(config.BSSID)) {

                        final int nNetworkId = list.get(i).networkId;

                        Log.d("CMWang", "connect this");
                        if(!mWifiManager.disconnect()) {
                            Log.d("CMWang", "disconnect fail");
                        }

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!mWifiManager.enableNetwork(nNetworkId, true)) {
                                            Log.d("CMWang", "enableNetwork fail");
                                        }

                                        Handler myHandler = new Handler();
                                        myHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if(!mWifiManager.reconnect()) {
                                                            Log.d("CMWang", "reconnect fail");
                                                        }
                                                    }
                                                });
                                            }
                                        }, 3000);
                                    }
                                });
                            }
                        }, 3000);
                        break;

                    }
                }
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAccessPointList.clear();

            mScanResultList = mWifiManager.getScanResults();

            for(int i = 0; i < mScanResultList.size(); i++) {
                String strBSSID = mScanResultList.get(i).BSSID;
                String strSSID = mScanResultList.get(i).SSID;
                String strLevel = Integer.toString(mScanResultList.get(i).level);

                AccessPoint accessPoint = new AccessPoint(strBSSID, strSSID, strLevel);
                mAccessPointList.add(accessPoint);
            }


            mAdapter.notifyDataSetChanged();
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

        //for(int i = 0; i < 100; i++) {
       //     mWifiManager.removeNetwork(i);
       // }
      //  mWifiManager.saveConfiguration();

        mScanButton = (Button) findViewById(R.id.scan_button);
        mScanButton.setOnClickListener(mOnScanClickListener);

        mListView = (ListView) findViewById(R.id.listview);
        mAdapter = new SimpleAdapter(this, R.layout.layout_listitem, mAccessPointList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mOnListItemClickListener);


        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, filter);

        /*
        mAdapter = new SimpleAdapter(this, R.layout.layout_listitem, );

        mListView.setAdapter();
        */
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
