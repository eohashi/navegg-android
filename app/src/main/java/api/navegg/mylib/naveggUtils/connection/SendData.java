package api.navegg.mylib.naveggUtils.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.InvalidProtocolBufferException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import api.navegg.mylib.naveggUtils.base.App;
import api.navegg.mylib.naveggUtils.base.ServerAPI;
import api.navegg.mylib.naveggUtils.bean.Package;
import api.navegg.mylib.naveggUtils.bean.User;
import api.navegg.mylib.naveggUtils.main.Util;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SendData {

    private User user;
    private transient Package.Track track;
    private transient Package.MobileInfo mMobileInfo;
    private int codAccount;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private Util util;
    private ServerAPI apiServiceAccount, apiServiceMobile;
    private Gson gson = new Gson();
    private List<String> trackList = new ArrayList<>();
    private List<String> mobileInfoList = new ArrayList<>();


    public SendData(Context context, int codAccount) {

        this.context = context;
        util = new Util(this.context);

        apiServiceAccount = App.getClient().create(ServerAPI.class);
        apiServiceMobile = App.sendDataProto().create(ServerAPI.class);
        this.codAccount = codAccount;

        this.mSharedPreferences = context.getSharedPreferences("SDK", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();

        getListMobileAndTrack();
        Gson gson = new Gson();
        String json = mSharedPreferences.getString("user", "");
        user = gson.fromJson(json, User.class);
    }

    private void getListMobileAndTrack() {

/*        Gson gsonMobile = new Gson();
        Type typeMobile = new TypeToken<List<String>>() {
        }.getType();
        String mMobileInfo = mSharedPreferences.getString("listAppMobileInfo", "");
        mobileInfoList = gsonMobile.fromJson(mMobileInfo, typeMobile);*/

        Gson gsonTrack = new Gson();
        String json = mSharedPreferences.getString("listAppTrack", "");
        Type type = new TypeToken<List<String>>() {
        }.getType();
        trackList = gsonTrack.fromJson(json, type);


    }


    public void trackMobile(String mActivity) {

        if (user != null) {

            if(!mSharedPreferences.getBoolean("sendDataMobile", true)){
                sendDataMobile(util.setDataMobile(user));
            }
            sendDataTrack(util.setDataTrack(mActivity, user));


        } else {

            setListTrackInShared(util.setDataTrack(mActivity, user));

        }

    }


    // se caso user for null envio as info para WS
    public void sendFirstData() {
        String deviceId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        if (util.verifyConnection()) {

            Call<User> call1 = apiServiceAccount.getUser(codAccount, deviceId);
            call1.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    user = response.body();
                    user.setCodConta(codAccount);

                    Gson gson = new Gson();
                    String mUserObject = gson.toJson(user);
                    editor.putString("user", mUserObject);
                    editor.commit();

                    sendDataMobile(util.setDataMobile(user));

                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    call.cancel();
                }
            });

        } else {
            editor.putInt("codConta", codAccount);
            editor.commit();
        }
    }


    // envio os dados do mobile
    public void sendDataMobile(Package.MobileInfo mobileInfo) {


        if (util.verifyConnection()) {

            RequestBody body =
                    RequestBody.create(MediaType.parse("text/mobile"), Base64.encodeToString(mobileInfo.toByteArray(), Base64.NO_WRAP));
            Call<Void> call1 = apiServiceMobile.sendDataMobile(body);


            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    editor.putBoolean("sendDataMobile",true);
                    editor.commit();

                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {

                    t.printStackTrace();

                    call.cancel();
                }
            });

        }else{

            editor.putBoolean("sendDataMobile",false);
            editor.commit();

        }

    }




    // envio os dados do track para o WS
    public void sendDataTrack(Package.Track track) {


        if (util.verifyConnectionWifi()) {

            RequestBody body =
                    RequestBody.create(MediaType.parse("text/track"), Base64.encodeToString(track.toByteArray(), Base64.NO_WRAP));
            Call<Void> call1 = apiServiceMobile.sendDataTrack(body);


            call1.enqueue(new Callback<Void>() {

                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    trackList = gson.fromJson(mSharedPreferences.getString("listAppTrack", ""),
                            new TypeToken<List<String>>() {
                            }.getType());


                    if (trackList != null) {

                        //sendDataListTrack.doInBackground(trackList);

                        for (String objTrack : trackList) {


                            // removo o track da lista
                            trackList.remove(objTrack);

                            Package.Track trackMob = null;
                            try {
                                trackMob = Package.Track.parseFrom(Base64.decode(objTrack, Base64.NO_WRAP));
                                trackMob.toBuilder()
                                        .setAcc(user.getCodConta())
                                        .setUserId(user.getmNvgId()).build();


                                String jsonTrack = gson.toJson(trackList);

                                //limpo shared preferences
                                // editor.remove("listAppTrack").commit();
                                editor.putString("listAppTrack", jsonTrack);
                                editor.commit();

                                sendDataTrack(trackMob);
                                break;
                            } catch (InvalidProtocolBufferException e) {
                                e.printStackTrace();
                            }

                        }

                    }

                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    t.printStackTrace();
                    call.cancel();
                }
            });

        } else {
            setListTrackInShared(track);
        }

    }







    public void setListTrackInShared(Package.Track trackMobile) {

        if (trackList == null) {
            trackList = new ArrayList<>();
        }

        trackList.add(Base64.encodeToString(trackMobile.toByteArray(), Base64.NO_WRAP));


        Gson gson = new Gson();
        String json = gson.toJson(trackList);
        editor.remove("listAppTrack").commit();
        editor.putString("listAppTrack", json);
        editor.commit();

    }

/*    public void setListMobileInfoInShared(Package.MobileInfo mobileInfo) {

        if (mobileInfoList == null) {
            mobileInfoList = new ArrayList<>();
        }

        mobileInfoList.add(Base64.encodeToString(mobileInfo.toByteArray(), Base64.NO_WRAP));

        Gson gson = new Gson();
        String json = gson.toJson(mobileInfoList);
        editor.remove("listAppMobileInfo").commit();
        editor.putString("listAppMobileInfo", json);
        editor.commit();

    }*/

}

