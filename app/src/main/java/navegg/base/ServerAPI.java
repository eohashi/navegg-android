package navegg.base;

import navegg.bean.User;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface ServerAPI {

    @GET("/usr")
    Call<User> getUser(@Query("acc") int acc, @Query("devid") String mDeviceId);

    @POST("/sdkreq")
    Call<Void> sendDataTrack(@Body  RequestBody mTrack);

    @POST("/sdkinfo")
    Call<Void> sendDataMobile(@Body RequestBody mMobileInfo);

    @POST("/usr")
    Call<Void> sendCustomId(@Query("acc") int acc, @Query("cus") int id_custom, @Query("id") long id_user);
}
