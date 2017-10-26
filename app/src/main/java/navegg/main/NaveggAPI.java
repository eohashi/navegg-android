package navegg.main;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;

import com.google.gson.Gson;

import navegg.bean.User;
import navegg.broadcast.VerifyStateConnection;
import navegg.connection.SendData;

//import android.support.v7.widget.Toolbar;


public class NaveggAPI {

    private Context context;
    private SharedPreferences mSharedPreferences;
    private Util util;
    protected SendData sendData;
    private User user = new User();
    private Handler handler;

    public NaveggAPI(Context ctx, final int codAccount) {
        this.context = ctx;
        sendData = new SendData(ctx, codAccount);
        handler = new Handler();
        setDataDevice();
        this.mSharedPreferences = context.getSharedPreferences("SDK", Context.MODE_PRIVATE);
        boolean broadCast = mSharedPreferences.getBoolean("broadCastRunning", false);
        System.out.println("BROAD CAST RECEIVER "+ broadCast);
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

        this.mSharedPreferences = context.getSharedPreferences("SDK", Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = mSharedPreferences.getString("user", "");
        user = gson.fromJson(json, User.class);

        if(user == null) {
            sendData.sendFirstData();
        }
    }



    public void setTrackPage(String mActivity){
        sendData.trackMobile(mActivity);
    }


    public void setCustom(int id_custom){
        sendData.setCustomInMobile(id_custom);
    }

    public String getSegments(String segment) {
        return util.getSegments(segment);
    }





}
