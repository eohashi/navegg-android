package api.navegg.mylib.naveggUtils.connection;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.InvalidProtocolBufferException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import api.navegg.mylib.BuildConfig;
import api.navegg.mylib.R;
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


public class Connection {

    private User user;
    private transient Package.Track track;
    private transient Package.MobileInfo mMogileInfo;
    private int codAccount;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private Util util;
    private ServerAPI apiServiceAccount, apiServiceMobile;
    private Gson gson = new Gson();
    private List<String> trackList = new ArrayList<>();
    private List<Package.MobileInfo> mobileInfoList = new ArrayList<>();
    private SendDataListTrack sendDataListTrack = new SendDataListTrack();


    public Connection(Context context, int codAccount) {

        this.context = context;
        util = new Util(this.context);

        apiServiceAccount = App.getClient().create(ServerAPI.class);
        apiServiceMobile = App.sendDataProto().create(ServerAPI.class);
        this.codAccount = codAccount;

        this.mSharedPreferences = context.getSharedPreferences("SDK", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
        Gson gson = new Gson();


        getListMobileAndTrack();

        String json = mSharedPreferences.getString("user", "");
        user = gson.fromJson(json, User.class);
    }

    private void getListMobileAndTrack() {

        Gson gsonMobile = new Gson();
        Type typeMobile = new TypeToken<List<Package.MobileInfo>>() {
        }.getType();
        String mMobileInfo = mSharedPreferences.getString("listAppMobileInfo", "");
        mobileInfoList = gsonMobile.fromJson(mMobileInfo, typeMobile);

        Gson gsonTrack = new Gson();
        String json = mSharedPreferences.getString("listAppTrack", "");
        Type type = new TypeToken<List<String>>() {
        }.getType();
        trackList = gsonTrack.fromJson(json, type);


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

                    // mando os dados do mobile uma vez
                    sendDataMobile(setDataMobile());
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

                    System.out.println("Mobile Enviado com sucesso");


         /*           Type type = new TypeToken<ArrayList<Package.MobileInfo>>() {}.getType();
                    listMobileInfo = gson.fromJson(mSharedPreferences.getString("listAppMobileInfo", ""), type);
                    if (listMobileInfo != null) {
                        System.out.println("Lista de mobile info" + listMobileInfo.size());
                        for (Package.MobileInfo mobileInfo : listMobileInfo) {
                            listMobileInfo.remove(track);
                            System.out.println("Mobile Info NAME " + mobileInfo);
                            String jsonText = gson.toJson(listMobileInfo);

                            //limpo shared preferences
                            editor.remove("listAppMobileInfo");
                            editor.putString("listAppMobileInfo", jsonText);
                            editor.commit();

                            System.out.println("Enviando informações do mobile " + mobileInfo.getAndroidName());

                            sendDataMobile(mobileInfo);
                        }
                    }*/
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {

                    t.printStackTrace();

                    call.cancel();
                }
            });

        } else {
            setListMobileInfoInShared(setDataMobile());
        }

    }


    public void trackMobile(String mActivity) {

        System.out.println("USER " + user);

        if (user != null) {

            sendDataTrack(setDataTrack(mActivity));

        } else {

            setListTrackInShared(setDataTrack(mActivity));

        }


    }


    // envio os dados do track para o WS
    public void sendDataTrack(Package.Track track) {


        if (util.verifyConnection()) {

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


    public Package.Track setDataTrack(String mActivity) {


        track = Package.Track.newBuilder()
                .setAcc((user != null) ? user.getCodConta() : 100000000)
                .setUserId((user != null) ? user.getmNvgId() : 100000000)
                .setNameApp(context.getString(R.string.app_name))
                .setDeviceIP(util.getMobileIP(context))
                .setTypeConnection(util.getTypeConnection())
                .addPageViews(Package.PageView.newBuilder()
                        .setActivity(mActivity)
                        .setDateTime(util.getCurrentDateTime())
                        .setTitlePage(String.valueOf(((Activity) context).getTitle()))
                        .setCallPage(""))
                .build();


        return track;

    }


    public Package.MobileInfo setDataMobile() {
        Gson gson = new Gson();
        String json = mSharedPreferences.getString("user", "");
        user = gson.fromJson(json, User.class);


        mMogileInfo = Package.MobileInfo.newBuilder()
                .setDeviceId(Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.ANDROID_ID))
                .setPlatform("Android")
                .setLongitude(util.getLong())
                .setLatitude(util.getLat())
                .setAndroidName(Build.DEVICE)
                .setAndroidBrand(Build.BRAND)
                .setAndroidModel(Build.MODEL)
                .setVersionRelease(Build.VERSION.RELEASE)
                .setManufacturer(Build.MANUFACTURER)
                .setVersionLib(String.valueOf(BuildConfig.VERSION_CODE))
                .setVersionCode(BuildConfig.VERSION_CODE)
                .setVersionOS(Build.VERSION.SDK_INT)
                .setAndroidFingerPrint(Build.FINGERPRINT)
                .setUserAgent(new WebView(context).getSettings().getUserAgentString())
                .setLinkPlayStore(util.getLinkPlayStore())
                .setTypeCategory(util.getTypeCategory())
                .setImei(util.getIMEI())
                .setSoftwareVersion(util.getSoftwareVersion())
                .setAcc(user.getCodConta())
                .setUserId(user.getmNvgId())
                .build();


        return mMogileInfo;

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

    public void setListMobileInfoInShared(Package.MobileInfo mobileInfo) {

        if (mobileInfoList == null) {
            mobileInfoList = new ArrayList<>();
        }

        mobileInfoList.add(mobileInfo);

        Gson gson = new Gson();
        String json = gson.toJson(mobileInfoList);
        editor.remove("listAppMobileInfo").commit();
        editor.putString("listAppMobileInfo", json);
        editor.commit();

    }


    public class SendDataListTrack extends AsyncTask<List<String>, Void, Void> {

        @Override
        protected Void doInBackground(List<String>... params) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            return null;
        }
    }


}
