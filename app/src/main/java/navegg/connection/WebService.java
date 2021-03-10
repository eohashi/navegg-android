package navegg.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
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

    private Context context;
    private Utils utils;
    private JSONObject jsonObject = new JSONObject();

    private static List<String> defineParams = new ArrayList<String>(Arrays.asList("prtusride","prtusridc","prtusridr","prtusridf","prtusridt"));

    private static final HashMap ENDPOINTS= new HashMap(){{
        put("app", "app");
        put("request", "cdn");
        put("onboarding", "cd");
    }};


    public WebService(Context context) {
        this.context = context;
        utils = new Utils(this.context);
    }


    private static String getEndpoint(String endpoint) {
        return "https://"+ENDPOINTS.get(endpoint)+".navdmp.com";
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
    public void sendDataMobileInfo(final User user, Package.MobileInfo mobileInfo) {
        if(user.getUserId().equals("0"))return;
        if (utils.verifyConnection()) {
            ServerAPI apiService = WebService.getApiService(
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
            Call<ResponseBody> call1 = apiService.sendDataMobileInfo(body);

            call1.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    try {
                        String response_body = response.body().string();
                        JSONObject jsonResponse = new JSONObject(response_body);
                        Boolean status = jsonResponse.getBoolean("status");
                        if (status) {
                            user.setToSendDataMobileInfo(true);
                        } else {
                            user.setToSendDataMobileInfo(false);
                        }
                    } catch (IOException e) {
                        //e.printStackTrace();
                        user.setToSendDataMobileInfo(false);

                    } catch (JSONException e) {
                        //e.printStackTrace();
                        user.setToSendDataMobileInfo(false);

                    } catch (Exception e){
                        user.setToSendDataMobileInfo(false);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody>  call, Throwable t) {
                    //t.printStackTrace();
                    user.setToSendDataMobileInfo(false);
                    call.cancel();
                }
            });

        } else {
            user.setToSendDataMobileInfo(false);
        }

    }

    // envio os dados do track de custom para o WS
    public void sendCustomList(final User user, final List<Integer> listCustom) {
        if(user.getUserId().equals("0"))return;
        if (utils.verifyConnectionWifi()) {
            Call<Void> call1;
            ServerAPI apiService = WebService.getApiService(
                    "request",
                    ProtoConverterFactory.create()
            ).create(ServerAPI.class);
            for (final int id_custom : listCustom) {
                call1 = apiService.sendCustomId(
                        user.getAccountId(),
                        id_custom,
                        user.getUserId()
                );
                call1.enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        user.removeCustomId(id_custom);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        //t.printStackTrace();
                        call.cancel();
                    }
                });
            }
        }
    }


    // se caso user for null envio as info para WS
    public void createUserId(final User user) {
        String userId = user.getUserId();
        if(userId!=null && !userId.equals("0"))
            return;

        String advertId = user.getAdvertId();

        if (utils.verifyConnection() && advertId!=null) {

            ServerAPI apiService = WebService.getApiService(
                    "app",
                    GsonConverterFactory.create(
                            new GsonBuilder().setLenient().create()
                    )
            ).create(ServerAPI.class);
            Call<ResponseBody> call1 = apiService.getUser(user.getAccountId(), advertId);
            call1.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String response_body = response.body().string();
                        JSONObject jsonResponse = new JSONObject(response_body);
                        user.setToSendDataMobileInfo(true);
                        user.__set_user_id(jsonResponse.getString("nvgid"));
                        sendDataMobileInfo(user, user.getDataMobileInfo());
                        getSegments(user);
                    } catch (IOException e) {
                        //e.printStackTrace();
                    } catch (JSONException e) {
                        //e.printStackTrace();
                    } catch (Exception e){

                    }


                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    call.cancel();
                }
            });

        }
    }


    // envio os dados do track para o WS
    public void sendDataTrack(final User user, List<PageView> pageView) {
        if(user.getUserId().equals("0")) return;
        Package.Track trackSerialized = utils.setDataTrack(user, utils.setListDataPageView(pageView));

        if (utils.verifyConnectionWifi()) {

            RequestBody body =
                    RequestBody.create(
                            MediaType.parse("text/track"),
                            Base64.encodeToString(
                                    trackSerialized.toByteArray(),
                                    Base64.NO_WRAP
                            )
                    );
            ServerAPI apiService = WebService.getApiService(
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
                    //t.printStackTrace();
                    call.cancel();
                }
            });
        }
    }

    // retornando os segmentos do WS
    public void getSegments(final User user) {
        if(user.getUserId().equals("0")) return;
        if (utils.verifyConnectionWifi()) {
            Call<ResponseBody> call1;
            ServerAPI apiService = WebService.getApiService(
                    "app",
                    GsonConverterFactory.create(
                            new GsonBuilder().setLenient().create()
                    )
            ).create(ServerAPI.class);
            call1 = apiService.getSegments(
                    user.getAccountId(),//accountId
                    0, //want in String
                    11, // Tag Navegg Version
                    user.getUserId(), // Navegg UserId
                    BuildConfig.VERSION_NAME, //SDK version
                    1, // want Custom
                    user.getOnBoarding().__get_hash_map()
            );

            call1.enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        user.saveSegments(response.body().string());
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    //t.printStackTrace();
                    call.cancel();
                }
            });
        }

    }


    // Onboarding
    public void sendOnBoarding(final User user, final OnBoarding onBoarding) {
        if(user.getUserId().equals("0")) return;

        if (utils.verifyConnectionWifi()) {
            Call<Void> call1;
            Map<String,String> params = onBoarding.__get_hash_map();
            for(String par : params.keySet()){
                if(!defineParams.contains(par) && !par.equalsIgnoreCase("DATA")){
                    try {
                        jsonObject.put(par, params.get(par));
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
            }

            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                String defParams = it.next();
                if(params.containsKey(defParams) && (defParams != "DATA")){
                    params.remove(defParams);
                }
            }

            if(jsonObject.length() > 0)
                params.put("DATA", jsonObject.toString());


            ServerAPI apiService = WebService.getApiService(
                    "onboarding",
                    ProtoConverterFactory.create()
            ).create(ServerAPI.class);
            call1 = apiService.setOnBoarding(params, user.getUserId(), user.getAccountId());

            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    user.getOnBoarding().__set_to_send_onBoarding(false);
                    user.getOnBoarding().setDateLastSync();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    //t.printStackTrace();
                    call.cancel();
                }
            });
        }
    }


}

