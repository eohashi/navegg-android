package api.navegg.mylib.naveggUtils.main;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import api.navegg.mylib.naveggUtils.base.ServerAPI;
import api.navegg.mylib.naveggUtils.bean.User;
import api.navegg.mylib.naveggUtils.connection.Connection;

//import android.support.v7.widget.Toolbar;


public class NaveggAPI {

    private Context context;
    private int codAccount;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    private Util util;
    private ServerAPI apiService;
    private String mActivity;
    private Connection connection;
    private User user;

    public NaveggAPI(Context ctx, final int codAccount) {
        this.context = ctx;
        this.codAccount = codAccount;

        System.out.println("CONSTRUTOR NAVEGGAPI");

        setDataDevice();
    }

    private void setDataDevice() {

        util = new Util(context);

        util.getCallPage();
        util.getVisibleFragment();

        connection = new Connection(context, codAccount);

        this.mSharedPreferences = context.getSharedPreferences("SDK", Context.MODE_PRIVATE);
        user = new User();
        Gson gson = new Gson();
        String json = mSharedPreferences.getString("user", "");
        user = gson.fromJson(json, User.class);

        System.out.println("USER "+ user);
        if(user == null) {
            connection.sendFirstData();
        }


    }



    public void setTrackPage(String mActivity){

        connection.trackMobile(mActivity);

    }






}
