package navegg.main;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;

import com.google.gson.Gson;

import navegg.bean.User;
import navegg.broadcast.VerifyStateConnection;
import navegg.connection.WebService;

public class NaveggAPI {

    private Context context;
    private SharedPreferences sharedPreference;
    private Util util;
    protected WebService webService;
    private User user;
    private Handler handler;

    public NaveggAPI(Context context, final int accountId) {
        this.user = new User(context, accountId);


        this.context = context;
        webService = new WebService(context, this.user);
        handler = new Handler();
        setDataDevice();
        this.sharedPreference = context.getSharedPreferences("NVGSDK"+accountId, Context.MODE_PRIVATE);
        boolean broadCast = sharedPreference.getBoolean("broadCastRunning", false);
        if(!broadCast) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(new VerifyStateConnection(), intentFilter);
        }
    }

    public void setDataDevice() {

        util = new Util(context);

        util.getCallPage();
        util.getVisibleFragment();

        Gson gson = new Gson();
        String json = sharedPreference.getString("user", "");
        user = gson.fromJson(json, User.class);

        if(user == null) {
            webService.sendFirstData();
        }
    }

    public void setTrackPage(String mActivity){
        webService.trackMobile(mActivity);
    }

    public void setCustom(int id_custom){
        webService.setCustomInMobile(id_custom);
    }

    public String getSegments(String segment) {
        return util.getSegments(segment);
    }

    public long getUserId() {
        Gson gson = new Gson();
        String json = sharedPreference.getString("user", "");
        return gson.fromJson(json, User.class).getId();
    }

    public void setOnBoarding(String params, String OnBoarding) {
        webService.setOnBoardingMobile(params,OnBoarding);
    }

}
