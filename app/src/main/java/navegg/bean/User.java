package navegg.bean;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by william on 08/06/17.
 */

public class User {

    private String userId;
    private int accountId;
    private SharedPreferences shaPref;
    private List<PageView> trackPageViewList = new ArrayList<>();
    private List<Integer> customList = new ArrayList<>();
    private List<OnBoarding> onBoardingList = new ArrayList<>();


    public User(Context context, int accountId) {
        this.accountId = accountId;
        this.shaPref = context.getSharedPreferences("NVGSDK"+accountId, Context.MODE_PRIVATE);
        this.userId = this.shaPref.getString("user", null);
        this.loadResourcesFromSharedObject();
    }

    private void loadResourcesFromSharedObject(){

        Gson gsonTrack = new Gson();
        String json;

        json = this.shaPref.getString("listAppPageView", "");
        this.trackPageViewList = gsonTrack.fromJson(json, new TypeToken<List<PageView>>(){}.getType());

        json = this.shaPref.getString("customList", "");
        this.customList = gsonTrack.fromJson(json, new TypeToken<List<Integer>>(){}.getType());

        json = this.shaPref.getString("onBoardingList", "");
        this.onBoardingList = gsonTrack.fromJson(json, new TypeToken<List<OnBoarding>>(){}.getType());

    }

    public String getUserId() { return this.userId; }

    public int getAccountId() {
        return this.accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getId (){ return this.userId;}

    public void setBroadcastRunning(Boolean status){
        this.shaPref.edit().putBoolean("broadCastRunning", status).commit();
    }

    public Boolean getBroadcastRunning(){
        this.shaPref.getBoolean("broadCastRunning", false);
    }

    public void setListTrackInShared(PageView pageViewData) {
        this.trackPageViewList.add(pageViewData);

        Gson gson = new Gson();
        String json = gson.toJson(this.trackPageViewList);
        this.shaPref.edit().putString("listAppPageView", json).commit();
    }

    public void setInListCustom(int id_custom) {
        this.customList.add(id_custom);

        String json  = new Gson().toJson(this.customList);
        this.shaPref.edit().putString("customList", json).commit();
    }

    public void setInListOnBoarding(OnBoarding onBoarding) {

        this.onBoardingList.add(onBoarding);
        String json = new Gson().toJson(this.onBoardingList);
        SharedPreferences.Editor editor = this.shaPref.edit();
        this.shaPref.edit().putString("onBoardingList", json).commit();

    }

    public List<PageView> getListMobileAndTrack() {
        return this.trackPageViewList;
    }

    public List<Integer> getListIdCustom() {
        return this.customList;
    }

    public List<OnBoarding> getListOnBoarding() {
        return this.onBoardingList;
    }

    public List<PageView> getTrackPageViewList() {
        return this.trackPageViewList;
    }

    public List<Integer> getCustomList() {
        return this.customList;
    }

    public void removeCustomId(int id_custom){
        this.customList.remove(new Integer(id_custom));
        if(this.customList.size() > 0) {
            String json  = new Gson().toJson(this.customList);
            this.shaPref.edit().putString("customList", json).commit();
        }else
            this.shaPref.edit().remove("customList").commit();

    }
}
