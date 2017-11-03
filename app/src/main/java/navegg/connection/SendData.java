package navegg.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import navegg.BuildConfig;
import navegg.base.App;
import navegg.base.ServerAPI;
import navegg.bean.OnBoarding;
import navegg.bean.Package;
import navegg.bean.PageView;
import navegg.bean.User;
import navegg.main.Util;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SendData {

    public User user;
    private int codAccount;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private Util util;
    private ServerAPI apiServiceAccount, apiServiceMobile, apiServiceOnBoarding;
    public List<PageView> trackPageViewList = new ArrayList<>();
    public List<Integer> customList = new ArrayList<>();
    public List<OnBoarding> onBoardingList = new ArrayList<>();

    public SendData() {
    }

    public SendData(Context context, int codAccount) {

        this.context = context;
        util = new Util(this.context);

        apiServiceAccount = App.getClient().create(ServerAPI.class);
        apiServiceMobile = App.sendDataProto().create(ServerAPI.class);
        apiServiceOnBoarding = App.sendDataOnBoarding().create(ServerAPI.class);

        this.codAccount = codAccount;

        this.mSharedPreferences = context.getSharedPreferences("SDK", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();

        getListMobileAndTrack();
        getListIdCustom();
        getListOnBoarding();

        Gson gson = new Gson();
        String json = mSharedPreferences.getString("user", "");
        user = gson.fromJson(json, User.class);

    }

    public void getListMobileAndTrack() {

        Gson gsonTrack = new Gson();
        String json = mSharedPreferences.getString("listAppPageView", "");
        Type type = new TypeToken<List<PageView>>() {
        }.getType();
        trackPageViewList = gsonTrack.fromJson(json, type);

    }

    public void getListIdCustom() {

        Gson gsonTrack = new Gson();
        String json = mSharedPreferences.getString("customList", "");
        Type type = new TypeToken<List<Integer>>() {
        }.getType();
        customList = gsonTrack.fromJson(json, type);


    }

    public void getListOnBoarding() {

        Gson gsonTrack = new Gson();
        String json = mSharedPreferences.getString("onBoardingList", "");
        Type type = new TypeToken<List<OnBoarding>>() {
        }.getType();
        onBoardingList = gsonTrack.fromJson(json, type);


    }


    public void trackMobile(String mActivity) {


        if (user != null) {

            if (!mSharedPreferences.getBoolean("sendDataMobile", true)) {
                sendDataMobile(util.setDataMobile(util.setDataMobileInfo(user)));
            }

            setListTrackInShared(util.setDataPageView(mActivity));
            getListMobileAndTrack();

            // Sorting
            Collections.sort(trackPageViewList, new Comparator<PageView>() {
                @Override
                public int compare(PageView o1, PageView o2) {
                    Long obj1 = o1.getDateTime();
                    Long obj2 = o2.getDateTime();
                    return obj1.compareTo(obj2);
                }
            });

            if (trackPageViewList != null) {
                sendDataTrack(trackPageViewList);
            }

        } else {
            setListTrackInShared(util.setDataPageView(mActivity));
        }

    }


    public void setCustomInMobile(int id_custom) {


        if (user != null) {

            if (!mSharedPreferences.getBoolean("sendDataMobile", true)) {
                sendDataMobile(util.setDataMobile(util.setDataMobileInfo(user)));
            }

            // insiro na lista de custom o id_custom
            // se caso falhe a conexão na hora de enviar os dados.
            setInListCustom(id_custom);
            getListIdCustom();


            if (customList != null) {
                sendIdCustom(customList, id_custom);
            }

        } else {
            setInListCustom(id_custom);
        }

    }

    public void setOnBoardingMobile(String params,String onBoarding) {

        OnBoarding onBoard = new OnBoarding();
        onBoard.setMethod(params);
        onBoard.setSha1(onBoarding);

        if (user != null) {

            if (!mSharedPreferences.getBoolean("sendDataMobile", true)) {
                sendDataMobile(util.setDataMobile(util.setDataMobileInfo(user)));
            }


            // insiro na lista de onBoard
            // se caso falhe a conexão na hora de enviar os dados.
            setInListOnBoarding(onBoard);
            getListOnBoarding();


            if (onBoardingList != null) {
                sendOnBoardingMobile(onBoardingList, onBoard);
            }

        } else {
            setInListOnBoarding(onBoard);
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
                    getSegments();
                    Gson gson = new Gson();
                    String mUserObject = gson.toJson(user);
                    editor.putString("user", mUserObject);
                    editor.commit();
                    sendDataMobile(util.setDataMobile(util.setDataMobileInfo(user)));

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
    private void sendDataMobile(Package.MobileInfo mobileInfo) {

        if (util.verifyConnection()) {

            RequestBody body =
                    RequestBody.create(MediaType.parse("text/mobile"), Base64.encodeToString(mobileInfo.toByteArray(), Base64.NO_WRAP));
            Call<Void> call1 = apiServiceAccount.sendDataMobile(body);

            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    editor.putBoolean("sendDataMobile", true);
                    editor.commit();

                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    t.printStackTrace();
                    call.cancel();
                }
            });

        } else {
            editor.putBoolean("sendDataMobile", false);
            editor.commit();
        }

    }


    // envio os dados do track para o WS
    public void sendDataTrack(List<PageView> pageView) {

        Package.Track trackMob = null;
        trackMob = util.setDataTrack(util.setDataBeanTrack(user, pageView), util.setListDataPageView(pageView));
        trackMob.toBuilder()
                .setAcc(user.getCodConta())
                .setUserId(user.getmNvgId()).build();

        if (util.verifyConnectionWifi()) {

            RequestBody body =
                    RequestBody.create(MediaType.parse("text/track"), Base64.encodeToString(trackMob.toByteArray(), Base64.NO_WRAP));
            Call<Void> call1 = apiServiceMobile.sendDataTrack(body);


            call1.enqueue(new Callback<Void>() {

                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    editor.remove("listAppPageView").commit();
                    trackPageViewList.clear();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    t.printStackTrace();
                    call.cancel();
                }
            });

        }

    }


    // envio os dados do track para o WS
    public void sendIdCustom(final List<Integer> listCustom, final int id_custom) {

        if (util.verifyConnectionWifi()) {
            Call<Void> call1 = null;

            call1 = apiServiceMobile.sendCustomId(user.getCodConta(),id_custom, user.getmNvgId());

            call1.enqueue(new Callback<Void>() {

                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    listCustom.remove(new Integer(id_custom));
                    if(listCustom.size() > 0) {
                        for(int id : listCustom){
                            sendIdCustom(listCustom, id);
                            break;
                         }
                    }else{
                        editor.remove("customList").commit();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    t.printStackTrace();
                    call.cancel();
                }
            });
        }
    }

    // retornando os segmentos do WS
    public void getSegments() {
        if (util.verifyConnectionWifi()) {
            Call<ResponseBody> call1 = null;
            call1 = apiServiceAccount.getSegments(user.getCodConta(),0,10,666, BuildConfig.VERSION_NAME);

            call1.enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                       util.saveSegments(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                    call.cancel();
                }
            });
        }

    }


    // Onboarding
    public void sendOnBoardingMobile(final List<OnBoarding> listOnBoarding, final OnBoarding boarding) {
        if (util.verifyConnectionWifi()) {
            Call<Void> call1 = null;
            Map<String,String> params = new HashMap<>();
            params.put(boarding.getMethod(),boarding.getSha1());
            call1 = apiServiceOnBoarding.setOnBoarding(params,user.getmNvgId(),user.getCodConta());

            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    onBoardingList.remove(boarding);
                    if(listOnBoarding.size() > 0) {
                        for(OnBoarding boarding : listOnBoarding){
                            sendOnBoardingMobile(listOnBoarding, boarding);
                            break;
                        }
                    }else{
                        editor.remove("onBoardingList").commit();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    t.printStackTrace();
                    call.cancel();
                }
            });
        }

    }



    public void setListTrackInShared(PageView pageViewData) {

        if (trackPageViewList == null) {
            trackPageViewList = new ArrayList<>();
        }

        trackPageViewList.add(pageViewData);

        Gson gson = new Gson();
        String json = gson.toJson(trackPageViewList);
        editor.remove("listAppPageView").commit();
        editor.putString("listAppPageView", json);
        editor.commit();

    }

    public void setInListCustom(int id_custom) {

        if (customList == null) {
            customList = new ArrayList<>();
        }

        customList.add(id_custom);

        Gson gson = new Gson();
        String json = gson.toJson(customList);
        editor.remove("customList").commit();
        editor.putString("customList", json);
        editor.commit();

    }

    public void setInListOnBoarding(OnBoarding onBoarding) {

        if (onBoardingList == null) {
            onBoardingList = new ArrayList<>();
        }

        onBoardingList.add(onBoarding);
        Gson gson = new Gson();
        String json = gson.toJson(onBoardingList);
        editor.remove("onBoardingList").commit();
        editor.putString("onBoardingList", json);
        editor.commit();

    }

}

