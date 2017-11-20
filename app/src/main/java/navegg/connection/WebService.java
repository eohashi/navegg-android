package navegg.connection;

import android.content.Context;
import android.provider.Settings;
import android.util.Base64;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import navegg.BuildConfig;
import navegg.base.ServerAPI;
import navegg.bean.OnBoarding;
import navegg.bean.Package;
import navegg.bean.PageView;
import navegg.bean.User;
import navegg.main.Utils;
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
    private Utils utils;
    private static final HashMap ENDPOINTS= new HashMap(){{
        put("user", "usr");
        put("request", "cdn");
        put("onboarding", "cd");

    }};


    public WebService(Context context, User user) {
        this.user = user;
        this.context = context;
        utils = new Utils(this.context);
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


    // envio os dados do mobile
    public void sendDataMobileInfo(Package.MobileInfo mobileInfo) {

        if (utils.verifyConnection()) {
            ServerAPI apiService = this.getApiService(
                    "request",
                    ProtoConverterFactory.create()
            ).create(ServerAPI.class);
            RequestBody body =
                    RequestBody.create(
                            MediaType.parse("text/mobile"),
                            Base64.encodeToString(
                                    mobileInfo.toByteArray(),
                                    Base64.NO_WRAP
                            )
                    );
            Call<Void> call1 = apiService.sendDataMobileInfo(body);

            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    user.setToSendDataMobileInfo(true);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    t.printStackTrace();
                    call.cancel();
                }
            });

        } else {
            user.setToSendDataMobileInfo(false);
        }

    }

    // envio os dados do track de custom para o WS
    public void sendCustomList(final List<Integer> listCustom) {

        if (utils.verifyConnectionWifi()) {
            Call<Void> call1;
            ServerAPI apiService = this.getApiService(
                    "request",
                    ProtoConverterFactory.create()
            ).create(ServerAPI.class);
            for (final int id_custom : listCustom) {
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
    }


    // se caso user for null envio as info para WS
    public void createUserId() {
        String deviceId = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (utils.verifyConnection()) {

            ServerAPI apiService = this.getApiService(
                    "user",
                    GsonConverterFactory.create(
                            new GsonBuilder().setLenient().create()
                    )
            ).create(ServerAPI.class);
            Call<User> call1 = apiService.getUser(this.user.getAccountId(), deviceId);
            call1.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> responseUser) {
                    user.setToSendDataMobileInfo(true);
                    user.__set_user_id(responseUser.body().getUserId());
                    sendDataMobileInfo(user.getDataMobileInfo());
                    getSegments();

                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    call.cancel();
                }
            });

        }
    }





    // envio os dados do track para o WS
    public void sendDataTrack(List<PageView> pageView) {

        Package.Track trackSerialized = utils.setDataTrack(this.user, utils.setListDataPageView(pageView));

        if (utils.verifyConnectionWifi()) {

            RequestBody body =
                    RequestBody.create(
                            MediaType.parse("text/track"),
                            Base64.encodeToString(
                                    trackSerialized.toByteArray(),
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
                    user.cleanPageViewList();
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
        if (utils.verifyConnectionWifi()) {
            Call<ResponseBody> call1;
            ServerAPI apiService = this.getApiService(
                    "user",
                    GsonConverterFactory.create(
                            new GsonBuilder().setLenient().create()
                    )
            ).create(ServerAPI.class);
            call1 = apiService.getSegments(
                    this.user.getAccountId(),//accountId
                    0, //want in String
                    10, // Tag Navegg Version
                    this.user.getUserId(), // Navegg UserId
                    BuildConfig.VERSION_NAME //SDK version
            );

            call1.enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                       user.saveSegments(response.body().string());
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
    public void sendOnBoarding(final OnBoarding onBoarding) {
        if (utils.verifyConnectionWifi()) {
            Call<Void> call1;
            Map<String,String> params = onBoarding.__get_hash_map();

            ServerAPI apiService = this.getApiService(
                    "onboarding",
                    ProtoConverterFactory.create()
            ).create(ServerAPI.class);
            call1 = apiService.setOnBoarding(params, this.user.getUserId(), this.user.getAccountId());

            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    user.__set_to_send_onBoarding(false);
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

