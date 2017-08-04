package navegg.bean;

/**
 * Created by william on 03/08/17.
 */

public class PageView {

    String activity;
    long dateTime;
    String titlePage;
    String callPage;

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public String getTitlePage() {
        return titlePage;
    }

    public void setTitlePage(String titlePage) {
        this.titlePage = titlePage;
    }

    public String getCallPage() {
        return callPage;
    }

    public void setCallPage(String callPage) {
        this.callPage = callPage;
    }
}
