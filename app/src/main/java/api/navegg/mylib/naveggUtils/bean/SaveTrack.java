package api.navegg.mylib.naveggUtils.bean;


public class SaveTrack {


    String Activity;
    long timeVisit;



    public SaveTrack() {
    }


    public String getActivity() {
        return Activity;
    }

    public void setActivity(String activity) {
        Activity = activity;
    }

    public long getTimeVisit() {
        return timeVisit;
    }

    public void setTimeVisit(long timeVisit) {
        this.timeVisit = timeVisit;
    }
}
