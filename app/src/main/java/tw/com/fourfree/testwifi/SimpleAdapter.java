package tw.com.fourfree.testwifi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by eric on 2015/4/8.
 */
public class SimpleAdapter extends ArrayAdapter<AccessPoint> {

    private final Context mContext;
    private final int mItemLayoutId;
    private final LayoutInflater mLayoutInflater;
    private final List<AccessPoint> mAccessPointList;

    private static class ViewHolder {
        TextView bssid;
        TextView ssid;
        TextView signal;
    }

    public SimpleAdapter(Context context, int resource, List<AccessPoint> accessPointList) {
        super(context, resource, accessPointList);
        mContext = context;
        mItemLayoutId = resource;
        mLayoutInflater = LayoutInflater.from(context);
        mAccessPointList = accessPointList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(mItemLayoutId, parent, false);
            vh = new ViewHolder();
            vh.bssid = (TextView) convertView.findViewById(R.id.bssid);
            vh.ssid = (TextView) convertView.findViewById(R.id.ssid);
            vh.signal = (TextView) convertView.findViewById(R.id.signal);

            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        AccessPoint item = mAccessPointList.get(position);
        vh.bssid.setText(mAccessPointList.get(position).getBSSID());
        vh.ssid.setText(mAccessPointList.get(position).getSSID());
        vh.signal.setText(mAccessPointList.get(position).getSignal());

        return convertView;
    }
}
