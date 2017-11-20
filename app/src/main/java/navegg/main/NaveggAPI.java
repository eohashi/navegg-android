package navegg.main;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import com.google.gson.Gson;

import navegg.bean.User;
import navegg.broadcast.VerifyStateConnection;
import navegg.connection.WebService;

public class NaveggAPI {

    private Context context;
    private SharedPreferences sharedPreference;
    private Utils utils;
    protected WebService webService;
    private User user;

    public NaveggAPI(Context context, final int accountId) {
        this.user = new User(context, accountId);


        this.context = context;
        this.webService = new WebService(context, this.user);
        this.utils = new Utils(context);

        this.user.setLastActivityName(utils.getActivityName());

        if(this.user.getUserId() == null) {
            this.webService.createUserId();
        }

        this.sharedPreference = context.getSharedPreferences("NVGSDK"+accountId, Context.MODE_PRIVATE);
        boolean broadCast = sharedPreference.getBoolean("broadCastRunning", false);
        if(!broadCast) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(new VerifyStateConnection(this.user), intentFilter);
        }
    }


    public void setTrackPage(String activity){
        if (!this.user.hasToSendDataMobileInfo()) {
            this.webService.sendDataMobileInfo(this.user.getDataMobileInfo());
        }

        this.user.makeAPageView(activity);
        this.webService.sendDataTrack(this.user.getTrackPageViewList());
    }

    public void setCustom(int id_custom){
        if (!this.user.hasToSendDataMobileInfo()) {
            this.webService.sendDataMobileInfo(this.user.getDataMobileInfo());
        }
        this.user.setCustom(id_custom);
        this.webService.sendCustomList(this.user.getCustomList());
    }

    public String getSegments(String segment) {
        return this.user.getSegments(segment);
    }

    public String getUserId() {
        Gson gson = new Gson();
        String json = sharedPreference.getString("user", "");
        return gson.fromJson(json, User.class).getId();
    }

    public void setOnBoarding(String key, String value) {
        this.user.setOnBoarding(key, value);
    }

}
