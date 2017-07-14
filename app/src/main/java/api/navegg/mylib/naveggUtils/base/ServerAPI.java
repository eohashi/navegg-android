package api.navegg.mylib.naveggUtils.base;

import api.navegg.mylib.naveggUtils.bean.User;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface ServerAPI {

    @GET("/usr")
    Call<User> getUser(@Query("acc") int acc, @Query("devid") String mDeviceId);

    @POST("/m")
    Call<Void> sendDataTrack(@Body  RequestBody mTrack);

    @POST("/minfo")
    Call<Void> sendDataMobile(@Body RequestBody mMobileInfo);
}
