package navegg.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.multidex.MultiDexApplication;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Set;
import java.util.TreeSet;

import navegg.bean.User;
import navegg.connection.WebService;
import navegg.main.Utils;

public class VerifyStateConnection extends BroadcastReceiver {
    private Utils utils;
    private WebService webService;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences shaPref = context.getSharedPreferences("NVGSDKS", Context.MODE_PRIVATE);
        shaPref.edit().putBoolean("receiverRunning", true);
        Gson gsonTrack = new Gson();
        String json = shaPref.getString("accounts","");
        Set<Integer> accounts = gsonTrack.fromJson(json, new TypeToken<TreeSet<Integer>>(){}.getType());

        if(accounts==null)
            accounts = new TreeSet<>();


        utils = new Utils(context);


        if (utils.verifyConnection()) {
            for(Integer accountId : accounts) {
                webService = new WebService(context);
                User user = new User(context, accountId);

                //First contact with WS
                if(user.getUserId().equals("0"))
                    webService.createUserId(user);

                //Send Track
                if (user.getTrackPageViewList() != null)
                    webService.sendDataTrack(user, user.getTrackPageViewList());

                //Send Custom
                if (user.getCustomList() != null) {
                    webService.sendCustomList(user, user.getCustomList());
                }

                // Send Onboarding
                if (user.getOnBoarding().hasToSendOnBoarding()) {
                    webService.sendOnBoarding(user , user.getOnBoarding());
                }
            }
        }

    }




}
