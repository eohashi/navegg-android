package navegg.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import navegg.bean.OnBoarding;
import navegg.bean.User;
import navegg.connection.WebService;
import navegg.main.Utils;

public class VerifyStateConnection extends BroadcastReceiver {
    private Utils utils;
    private WebService webService;
    private User user;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;

    public VerifyStateConnection(User user){
        this.user = user;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        utils = new Utils(context);
        this.user.setBroadcastRunning(true);

        if (utils.verifyConnection()) {
            webService = new WebService(context, this.user);

            //Send Track
            if(this.user.getTrackPageViewList() != null)
                webService.sendDataTrack(this.user.getTrackPageViewList());

            //Send Custom
            if(this.user.getCustomList() != null) {
                webService.sendCustomList(this.user.getCustomList());
            }

            // Send Onboarding
            if(this.user.hasToSendOnBoarding()){
                webService.sendOnBoarding(this.user.getOnBoarding());
            }
        }

    }

}
