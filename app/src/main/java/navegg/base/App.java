package navegg.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.protobuf.ProtoConverterFactory;


public class App {

    private static Retrofit retrofit, retrofitProto, retrofitCustom= null;
    public static final String ENDPOINT = "http://usr.navdmp.com";

    //public static final String ENDPOINT2 = "http://192.168.1.113";
    public static final String ENDPOINT2 = "http://cdn.navdmp.com";



    public static Retrofit getClient() {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(logging);

        retrofit = new Retrofit.Builder()
                .baseUrl(ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();


        return retrofit;
    }


    public static Retrofit sendDataProto() {


        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
// add your other interceptors …

// add logging as last interceptor
        httpClient.addInterceptor(logging);  // <-- this is the important line!


        retrofitProto = new Retrofit.Builder()
                .baseUrl(ENDPOINT2)
                .addConverterFactory(ProtoConverterFactory.create())
                .client(httpClient.build())
                .build();


        return retrofitProto;
    }




}
