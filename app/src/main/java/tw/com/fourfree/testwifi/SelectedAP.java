package tw.com.fourfree.testwifi;

/**
 * Created by eric on 2015/5/18.
 */
public class SelectedAP {

    private static AccessPoint sAccessPoint;

    public static AccessPoint getSelectedAP() {
        return sAccessPoint;
    }

    public static void setSelectedAP(AccessPoint accessPoint) {
        sAccessPoint = accessPoint;
    }
}
