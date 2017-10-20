package navegg.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import navegg.bean.User;
import navegg.connection.SendData;
import navegg.main.Util;

/**
 * Created by william on 20/10/17.
 */

public class VerifyStateConnection extends BroadcastReceiver {
    private Util util;
    private SendData sendData;
    User user;
    @Override
    public void onReceive(Context context, Intent intent) {
        util = new Util(context);
        System.out.println("TROCANDO DE STATE MOBILE");
        if(user != null) {
            sendData = new SendData(context, user.getCodConta());
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
