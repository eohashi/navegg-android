package navegg.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import navegg.bean.OnBoarding;
import navegg.bean.User;
import navegg.connection.WebService;
import navegg.main.Util;

public class VerifyStateConnection extends BroadcastReceiver {
    private Util util;
    private WebService webService;
    private User user;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;

    VerifyStateConnection(User user){
        this.user = user;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        util = new Util(context);
        this.user.setBroadcastRunning(true);


        Gson gson = new Gson();
        String json = mSharedPreferences.getString("user", "");
        user = gson.fromJson(json, User.class);
        if (util.verifyConnection()) {
            webService = new WebService(context, this.user);

            //Send Track
            if(this.user.getTrackPageViewList() != null)
                webService.sendDataTrack(this.user.getTrackPageViewList());

            //Send Custom
            if(this.user.getCustomList() != null) {
                for (int id_custom : this.user.getCustomList()) {
                    webService.sendIdCustom(this.user.getCustomList, id_custom);
                    break;
                }
            }
        }

    }


    private void receiverSendCustom(){
        if(webService.customList != null) {
            for (int id_custom : webService.customList) {
                webService.sendIdCustom(webService.customList, id_custom);
                break;
            }
        }
    }

    private void receiverSendOnBoard(){
        if(webService.onBoardingList != null){
            for(OnBoarding onBoard : webService.onBoardingList){
                webService.sendOnBoardingMobile(webService.onBoardingList,onBoard);
                break;
            }
        }

    }
}
