package navegg.bean;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import navegg.BuildConfig;
import navegg.main.Utils;


/**
 * Created by william on 08/06/17.
 * Reviewed by Salvachz on 20/11/17.
 */

public class User {
    @SerializedName("nvgid")
    private String userId;
    @SerializedName("accountId")
    private int accountId;
    private Context context;
    private Utils utils;
    private SharedPreferences shaPref;
    private List<PageView> trackPageViewList = new ArrayList<>();
    private List<Integer> customList = new ArrayList<>();
    private OnBoarding onBoarding;
    JSONObject segments;
    private final static String[] listSegments = {
            "gender", "age", "education", "marital",
            "income", "city", "region", "country",
            "connection", "brand", "product",
            "interest", "career", "cluster",
            "", "custom", "industry", "everybuyer" //empty one was prolook
    };


    public User(Context context, int accountId) {
        this.context = context;
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
        this.onBoarding = gsonTrack.fromJson(json, new TypeToken<List<OnBoarding>>(){}.getType());

        String jsonSegments = this.shaPref.getString("jsonSegments", "");
        try {
            this.segments = new JSONObject(jsonSegments);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void __set_user_id(String userId) {
        this.shaPref.edit().putString("user", userId).commit();
        this.userId = userId;
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

    public Boolean hasToSendDataMobileInfo(){
        return this.shaPref.getBoolean("sendDataMobileInfo", false);
    }

    public void setToSendDataMobileInfo(Boolean status){
        this.shaPref.edit().putBoolean("sendDataMobileInfo", status).commit();
    }

    public Boolean getBroadcastRunning(){

        return this.shaPref.getBoolean("broadCastRunning", false);
    }


    /* MobileInfo */
    public Package.MobileInfo getDataMobileInfo() {

        return Package.MobileInfo.newBuilder()
                .setDeviceId(Settings.Secure.getString(this.context.getContentResolver(), Settings.Secure.ANDROID_ID))
                .setPlatform("Android")
                .setLongitude(utils.getLong())
                .setLatitude(utils.getLat())
                .setAndroidName(Build.DEVICE)
                .setAndroidBrand(Build.BRAND)
                .setAndroidModel(Build.MODEL)
                .setVersionRelease(Build.VERSION.RELEASE)
                .setManufacturer(Build.MANUFACTURER)
                .setVersionLib(BuildConfig.VERSION_NAME)
                .setVersionCode(BuildConfig.VERSION_CODE)
                .setVersionOS(Build.VERSION.SDK_INT)
                .setAndroidFingerPrint(Build.FINGERPRINT)
                .setUserAgent(new WebView(context).getSettings().getUserAgentString())
                .setLinkPlayStore(utils.getLinkPlayStore())
                .setTypeCategory(utils.getTypeCategory())
                .setImei(utils.getIMEI())
                .setSoftwareVersion(utils.getSoftwareVersion())
                .setUserId(this.getUserId())
                .setAcc(this.getAccountId())
                .build();
    }

    /* Track/PageView */
    public List<PageView> getListMobileAndTrack() {

        return this.trackPageViewList;
    }

    public void makeAPageView(String activity){

        PageView pageView = new PageView();
        pageView.setActivity(activity);
        pageView.setDateTime(utils.getCurrentDateTime());
        pageView.setTitlePage(String.valueOf(((Activity) this.context).getTitle()));
        pageView.setCallPage("");

        this.trackPageViewList.add(pageView);

        // Sorting
        Collections.sort(this.trackPageViewList, new Comparator<PageView>() {
            @Override
            public int compare(PageView o1, PageView o2) {
                Long obj1 = o1.getDateTime();
                Long obj2 = o2.getDateTime();
                return obj1.compareTo(obj2);
            }
        });

        Gson gson = new Gson();
        String json = gson.toJson(this.trackPageViewList);
        this.shaPref.edit().putString("listAppPageView", json).commit();
    }

    public void cleanPageViewList() {
        this.shaPref.edit().remove("listAppPageView").commit();
        this.trackPageViewList.clear();
    }

    public List<PageView> getTrackPageViewList() {
        return this.trackPageViewList;
    }


    /* Custom */

    public void setCustom(int id_custom) {
        this.customList.add(id_custom);

        String json  = new Gson().toJson(this.customList);
        this.shaPref.edit().putString("customList", json).commit();
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

    /* OnBoarding */

    public OnBoarding getOnBoarding() {
        return this.onBoarding;
    }

    public void setOnBoarding(String key, String value) {

        this.onBoarding.addInfo(key, value);

        String json = new Gson().toJson(this.onBoarding);
        SharedPreferences.Editor editor = this.shaPref.edit();
        this.shaPref.edit().putString("onBoardingList", json).commit();
        this.__set_to_send_onBoarding(true);
    }

    public void __set_to_send_onBoarding(Boolean status){
        this.shaPref.edit().putBoolean("toSendOnBoarding", status).commit();
    }

    public Boolean hasToSendOnBoarding(){
        return this.shaPref.getBoolean("toSendOnBoarding", false);
    }

    /* Activity Name */
    public String getLastActivityName(){
        return this.shaPref.getString("lastActivityName","");
    }

    public void setLastActivityName(String activityName){
        this.shaPref.edit().putString("lastActivityName",activityName);
    }


    /* Segments */
    public void saveSegments(String segments) {
        String[] seg = segments.substring(segments.indexOf(" '") + 2, segments.indexOf("');")).split(":",-1);
        JSONObject json = new JSONObject();
        for(int i = 0; i < listSegments.length; i++){
            if(seg[i].length() > 0) {
                try {
                    json.put(listSegments[i], seg[i]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        this.shaPref.edit().putString("jsonSegments", json.toString()).commit();
    }

    public String getSegments(String segment){


        String idSegment = "";
        try {
            idSegment = (String) this.segments.get(segment.toLowerCase().trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return idSegment;
    }



}
