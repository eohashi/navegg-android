package navegg.connection;

import android.content.Context;
import android.provider.Settings;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import navegg.BuildConfig;
import navegg.base.ServerAPI;
import navegg.bean.OnBoarding;
import navegg.bean.Package;
import navegg.bean.PageView;
import navegg.bean.User;
import navegg.main.Util;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.protobuf.ProtoConverterFactory;


public class WebService {

    public User user;
    private Context context;
    private Util util;
    private static final HashMap ENDPOINTS= new HashMap(){{
        put("user", "usr");
        put("request", "cdn");
        put("onboarding", "cd");

    }};


    public WebService(Context context, User user) {
        this.user = user;
        this.context = context;
        util = new Util(this.context);
    }


    private static String getEndpoint(String endpoint) {
        return "http://"+ENDPOINTS.get(endpoint)+".navdmp.com";
    }


    private static Retrofit.Builder getRetrofitBuilder(){
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        return new Retrofit.Builder().client(httpClient.build());
    }


    private static Retrofit getApiService(String endpoint, Converter.Factory fctr){

        Retrofit.Builder retrofitBuilder = getRetrofitBuilder();
        return retrofitBuilder
                .baseUrl(getEndpoint(endpoint))
                .addConverterFactory(fctr)
                .build();
    }




    public void trackMobile(String mActivity) {


        if (!mSharedPreferences.getBoolean("sendDataMobile", true)) {
                sendDataMobile(util.getDataMobile(this.user));
            }

            setListTrackInShared(util.setDataPageView(mActivity));

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



    }


    public void setCustomInMobile(int id_custom) {


        if (this.user != null) {

            if (!mSharedPreferences.getBoolean("sendDataMobile", true)) {
                sendDataMobile(util.getDataMobile(this.user));
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

        if (this.user != null) {

            if (!mSharedPreferences.getBoolean("sendDataMobile", true)) {
                sendDataMobile(util.getDataMobile(this.user));
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

            apiServiceAccount = App.getApiService("user", GsonConverterFactory.create(new GsonBuilder().setLenient().create())).create(ServerAPI.class);
            Call<User> call1 = apiServiceAccount.getUser(codAccount, deviceId);
            call1.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    user = response.body();
                    user.setAccountId(codAccount);
                    getSegments();
                    Gson gson = new Gson();
                    String mUserObject = gson.toJson(user);
                    editor.putString("user", mUserObject);
                    editor.commit();
                    sendDataMobile(util.getDataMobile(user));

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

        Package.Track trackMob = util.setDataTrack(this.user, util.setListDataPageView(pageView));
        /*trackMob.toBuilder()
                .setAcc(this.user.getAccountId())
                .setUserId(this.user.getUserId()).build();*/

        if (util.verifyConnectionWifi()) {

            RequestBody body =
                    RequestBody.create(
                            MediaType.parse("text/track"),
                            Base64.encodeToString(
                                    trackMob.toByteArray(),
                                    Base64.NO_WRAP
                            )
                    );
            ServerAPI apiService = this.getApiService(
                    "request",
                    ProtoConverterFactory.create()
            ).create(ServerAPI.class);
            Call<Void> call1 = apiService.sendDataTrack(body);


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


    // envio os dados do track de custom para o WS
    public void sendIdCustom(final List<Integer> listCustom, final int id_custom) {

        if (util.verifyConnectionWifi()) {
            Call<Void> call1 = null;
            ServerAPI apiService = this.getApiService (
                    "request",
                    ProtoConverterFactory.create()
            ).create(ServerAPI.class);

            call1 = apiService.sendCustomId(
                    this.user.getAccountId(),
                    id_custom,
                    this.user.getUserId()
            );

            call1.enqueue(new Callback<Void>() {

                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    user.removeCustomId(id_custom);
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
            call1 = apiServiceAccount.getSegments(this.user.getAccountId(),0,10,666, BuildConfig.VERSION_NAME);

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
            apiServiceOnBoarding = App.getApiService("onboarding", ProtoConverterFactory.create()).create(ServerAPI.class);
            call1 = apiServiceOnBoarding.setOnBoarding(params, this.user.getUserId(), this.user.getAccountId());

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





}

