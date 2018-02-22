package navegg.bean;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.BuildConfig;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

import navegg.connection.WebService;
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
    private List<PageView> trackPageViewList;
    private List<Integer> customList;
    private OnBoarding onBoarding;
    private JSONObject segments;
    private WebService ws;
    private final static String[] listSegments = {
            "gender", "age", "education", "marital",
            "income", "city", "region", "country",
            "connection", "brand", "product",
            "interest", "career", "cluster",
            "", "custom", "industry", "everybuyer" //empty one was prolook
    };


    public User(Context context, Integer accountId) {
        this.context = context;
        this.accountId = accountId;
        this.utils = new Utils(context);
        this.shaPref = context.getSharedPreferences("NVGSDK"+accountId, Context.MODE_PRIVATE);
        this.userId = this.shaPref.getString("user", null);
        this.ws = new WebService(this.context);
        this.loadResourcesFromSharedObject();
    }

    private void loadResourcesFromSharedObject(){

        Gson gsonTrack = new Gson();
        String json;

        json = this.shaPref.getString("listAppPageView", "");
        this.trackPageViewList = gsonTrack.fromJson(json, new TypeToken<List<PageView>>(){}.getType());
        if(this.trackPageViewList==null)
            this.trackPageViewList = new ArrayList<>();

        json = this.shaPref.getString("customList", "");
        this.customList = gsonTrack.fromJson(json, new TypeToken<List<Integer>>(){}.getType());
        if(this.customList==null)
            this.customList = new ArrayList<>();

        json = this.shaPref.getString("onBoarding", "");
        this.onBoarding = gsonTrack.fromJson(json, new TypeToken<OnBoarding>(){}.getType());
        if(this.onBoarding==null)
            this.onBoarding = new OnBoarding(this.shaPref);


    }

    /* User Id */
    public void __set_user_id(String userId) {
        this.shaPref.edit().putString("user", userId).commit();
        this.userId = userId;
    }
    public String getUserId() {
        if(this.userId==null)
            return "0";
        return this.userId;
    }

    public int getAccountId() {
        return this.accountId;
    }

    public void setBroadcastRunning(Boolean status){
        this.shaPref.edit().putBoolean("broadCastRunning", status).commit();
    }

    public Boolean hasToSendDataMobileInfo(){
        return this.shaPref.getBoolean("sendDataMobileInfo", false);
    }

    public void setToSendDataMobileInfo(Boolean status){
        this.shaPref.edit().putBoolean("sendDataMobileInfo", status).commit();
    }

    /* MobileInfo */
    public Package.MobileInfo getDataMobileInfo() {

        return Package.MobileInfo.newBuilder()
                .setDeviceId(Settings.Secure.getString(this.context.getContentResolver(), Settings.Secure.ANDROID_ID))
                .setPlatform("Android")
                .setLongitude(this.utils.getLong())
                .setLatitude(this.utils.getLat())
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
                .setLinkPlayStore(this.utils.getLinkPlayStore())
                .setTypeCategory(this.utils.getTypeCategory())
                .setImei(this.utils.getIMEI())
                .setSoftwareVersion(this.utils.getSoftwareVersion())
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
        this.shaPref.edit().putLong("dateLastSync",Calendar.getInstance().getTime().getTime()).commit();
    }

    public String  getSegments(String segment){

        String idSegment = "";
        this.segments = new JSONObject();
        String jsonSegments = this.shaPref.getString("jsonSegments", "");
        long dateLastSync = this.shaPref.getLong("dateLastSync", 0);

        if(dateLastSync != 0){

            Date dateSync = new Date(dateLastSync);
            Date currentDate = Calendar.getInstance().getTime();

            try {
                dateSync = new SimpleDateFormat("yyyy-MM-dd").parse(utils.dateToString(dateSync));
                currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(utils.dateToString(currentDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(currentDate.after(dateSync)){
                this.ws.getSegments(this);
            }
        }

        if(jsonSegments!="")
            try {
                this.segments = new JSONObject(jsonSegments);
                idSegment = (String) this.segments.get(segment.toLowerCase().trim());
            } catch (JSONException e) {
                //e.printStackTrace();
            }



        return idSegment;
    }



}
