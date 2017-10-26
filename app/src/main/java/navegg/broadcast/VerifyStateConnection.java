package navegg.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;

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
        System.out.println("USER RECEIVER "+ user);
        if(user != null) {
            sendData = new SendData(context, user.getCodConta());
            System.out.println("TROCANDO DE STATE MOBILE");
            if (util.verifyConnection()) {
                System.out.println("ENVIANDO DADOS PELO BROADCAST RECEIVER");
                sendData.getListIdCustom();
                sendData.getListMobileAndTrack();

                receiverSendCustom();
                receiverSendTrackMobile();
            }
        }
    }

    private void receiverSendTrackMobile() {
        sendData.sendDataTrack(sendData.trackPageViewList);
    }

    private void receiverSendCustom(){
        for(int id_custom: sendData.customList){
            sendData.sendIdCustom(sendData.customList, id_custom);
            break;
        }
    }
}
