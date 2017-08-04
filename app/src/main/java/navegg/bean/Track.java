package navegg.bean;

import java.util.List;

/**
 * Created by william on 03/08/17.
 */

public class Track {


    long userId;
    int acc;
    String nameApp;
    String deviceIP;
    List<PageView> pageViews;
    String typeConnection;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getAcc() {
        return acc;
    }

    public int setAcc(int acc) {
        return this.acc = acc;
    }

    public String getNameApp() {
        return nameApp;
    }

    public void setNameApp(String nameApp) {
        this.nameApp = nameApp;
    }

    public String getDeviceIP() {
        return deviceIP;
    }

    public void setDeviceIP(String deviceIP) {
        this.deviceIP = deviceIP;
    }

    public String getTypeConnection() {
        return typeConnection;
    }

    public void setTypeConnection(String typeConnection) {
        this.typeConnection = typeConnection;
    }

    public List<PageView> getPageViews() {
        return pageViews;
    }

    public void setPageViews(List<PageView> pageViews) {
        this.pageViews = pageViews;
    }
}
