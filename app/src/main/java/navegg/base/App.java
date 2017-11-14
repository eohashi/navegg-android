package navegg.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.protobuf.ProtoConverterFactory;


public class App {
    private static final HashMap ENDPOINTS= new HashMap(){{
        put("user", "usr");
        put("request", "cdn");
        put("onboarding", "cd");

    }};


    private static String getEndpoint(String endpoint) {
        return "http://"+ENDPOINTS.get(endpoint)+".navdmp.com";
    }


    private static Retrofit.Builder getRetrofitBuilder(){
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        return new Retrofit.Builder().client(httpClient.build());
    }


    public static Retrofit getRetrofit(String endpoint, Converter.Factory fctr){

        Retrofit.Builder retrofitBuilder = getRetrofitBuilder();
        return retrofitBuilder
                .baseUrl(getEndpoint(endpoint))
                .addConverterFactory(fctr)
                .build();
    }

}
