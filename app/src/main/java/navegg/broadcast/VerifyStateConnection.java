package navegg.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import navegg.bean.OnBoarding;
import navegg.bean.User;
import navegg.connection.SendData;
import navegg.main.Util;

public class VerifyStateConnection extends BroadcastReceiver {
    private Util util;
    private SendData sendData;
    User user;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    @Override
    public void onReceive(Context context, Intent intent) {

        util = new Util(context);
        mSharedPreferences = context.getSharedPreferences("SDK", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
        editor.putBoolean("broadCastRunning", true);
        editor.commit();


        Gson gson = new Gson();
        String json = mSharedPreferences.getString("user", "");
        user = gson.fromJson(json, User.class);
        if(user != null) {
            sendData = new SendData(context, user.getCodConta());
            if (util.verifyConnection()) {
                sendData.getListIdCustom();
                sendData.getListMobileAndTrack();
                sendData.getListOnBoarding();

                receiverSendCustom();
                receiverSendTrackMobile();
                receiverSendOnBoard();
            }
        }
    }

    private void receiverSendTrackMobile() {
        if(sendData.trackPageViewList != null)
            sendData.sendDataTrack(sendData.trackPageViewList);
    }

    private void receiverSendCustom(){
        if(sendData.customList != null) {
            for (int id_custom : sendData.customList) {
                sendData.sendIdCustom(sendData.customList, id_custom);
                break;
            }
        }
    }

    private void receiverSendOnBoard(){
        if(sendData.onBoardingList != null){
            for(OnBoarding onBoard : sendData.onBoardingList){
                sendData.sendOnBoardingMobile(sendData.onBoardingList,onBoard);
                break;
            }
        }

    }
}
