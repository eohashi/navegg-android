package navegg.bean;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import navegg.BuildConfig;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

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
    private String advertId;
    private Context context;
    private Utils utils;
    private SharedPreferences shaPref;
    private List<PageView> trackPageViewList;
    private List<Integer> customList;
    private OnBoarding onBoarding;
    private JSONObject segments;
    private WebService ws;
    private List<Integer> listCustomPermanent = new ArrayList<>();
    private String jsonSegments;

    public User(Context context, Integer accountId) {
        this.context = context;
        this.accountId = accountId;
        this.utils = new Utils(context);
        this.shaPref = context.getSharedPreferences("NVGSDK"+accountId, Context.MODE_PRIVATE);
        this.userId = this.shaPref.getString("user"+accountId, null);
        this.onBoarding = new OnBoarding(this.shaPref, this.utils, this.accountId, this.context);
        this.ws = new WebService(this.context);
        this.loadAdvertId(this.context);
        this.loadResourcesFromSharedObject();

        if (!this.getUserId().equals("0")) {
            if (!this.hasToSendDataMobileInfo()) {
                this.ws.sendDataMobileInfo(this, this.getDataMobileInfo());
            }
        }
    }

    public String getAdvertId(){
        return this.advertId;
    }
    private void loadAdvertId(final Context context) {
        if(this.advertId!=null) return;

        this.advertId = this.shaPref.getString("advertId", null);
        if(this.advertId!=null) return;

        final User thisUser = this;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                    thisUser.advertId = adInfo != null ? adInfo.getId() : null;
                    if (thisUser.advertId != null)
                        thisUser.shaPref.edit().putString("advertId", thisUser.advertId).apply();
                    // Use the advertising id
                } catch (Exception e){
                    Log.e("Error","Error Exception: " + e);
                } catch (Throwable t) {
                    // not avalible google play
                    Log.e("Error","Error getting advertising ID. Google Play Services are not available: " + t);
                }
            }
        });
    }

    private void loadResourcesFromSharedObject(){

        Gson gsonTrack = new Gson();
        String json;

        json = this.shaPref.getString("listAppPageView", "");
        this.trackPageViewList = gsonTrack.fromJson(json, new TypeToken<List<PageView>>(){}.getType());
        if(this.trackPageViewList==null)
            this.trackPageViewList = new ArrayList<>();

        json = this.shaPref.getString("customList"+this.accountId, "");
        this.customList = gsonTrack.fromJson(json, new TypeToken<List<Integer>>(){}.getType());
        if(this.customList==null)
            this.customList = new ArrayList<>();

        json = this.shaPref.getString("customListAux"+this.accountId, "");
        this.listCustomPermanent = gsonTrack.fromJson(json, new TypeToken<List<Integer>>(){}.getType());
        if(this.listCustomPermanent==null)
            this.listCustomPermanent = new ArrayList<>();
    }

    /* User Id */
    public void __set_user_id(String userId) {
        this.shaPref.edit().putString("user"+this.accountId, userId).commit();
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
        return this.shaPref.getBoolean("sentMobileInfo", false);
    }

    public void setToSendDataMobileInfo(Boolean status){
        this.shaPref.edit().putBoolean("sentMobileInfo", status).commit();
    }

    /* MobileInfo */
    public Package.MobileInfo getDataMobileInfo() {

        return Package.MobileInfo.newBuilder()
                .setDeviceId(this.getAdvertId())
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
                .setUserAgent("Android")
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
        this.shaPref.edit().remove("listAppPageView").apply();
        this.trackPageViewList.clear();
    }

    public List<PageView> getTrackPageViewList() {
        return this.trackPageViewList;
    }


    /* Custom */

    public void setCustom(int id_custom) {
        this.customList.add(id_custom);

        setPermanentCustom(id_custom);


        String json  = new Gson().toJson(this.customList);
        this.shaPref.edit().putString("customList"+this.accountId, json).commit();
    }

    private void setPermanentCustom(int id_custom) {

        if(!listCustomPermanent.contains(id_custom)) {
            listCustomPermanent.add(id_custom);
            String json = new Gson().toJson(this.listCustomPermanent);
            this.shaPref.edit().putString("customListAux"+this.accountId, json).commit();
        }

    }

    public List<Integer> getCustomList() {
        return this.customList;
    }

    public void removeCustomId(int id_custom){
        this.customList.remove(Integer.valueOf(id_custom));
        if(this.customList.size() > 0) {
            String json  = new Gson().toJson(this.customList);
            this.shaPref.edit().putString("customList"+this.accountId, json).commit();
        }else
            this.shaPref.edit().remove("customList"+this.accountId).commit();

    }

    /* OnBoarding */
    public OnBoarding getOnBoarding() {
        return this.onBoarding;
    }

    public boolean setOnBoarding(String key, String value) {

        return this.onBoarding.addInfo(key, value);
    }


    /* Activity Name */
    public String getLastActivityName(){
        return this.shaPref.getString("lastActivityName","");
    }

    public void setLastActivityName(String activityName){
        this.shaPref.edit().putString("lastActivityName",activityName).apply();
    }


    /* Segments */
    public void saveSegments(String segments) {
        try {
            JSONObject json = new JSONObject(segments);
            this.shaPref.edit().putString("jsonSegments"+this.accountId, json.toString()).apply();
            this.shaPref.edit().putLong("dateLastSync", Calendar.getInstance().getTime().getTime()).apply();
        } catch (Exception e) {
            //e.printStackTrace();
        }


    }

    public String  getSegments(String segment){
        String idSegment = "";
        try {
            this.segments = new JSONObject();
            jsonSegments = this.shaPref.getString("jsonSegments" + this.accountId, "");
            long dateLastSync = this.shaPref.getLong("dateLastSync", 0);
            long dateLastSyncOnBoard = this.getOnBoarding().getDateLastSync();

            if (dateLastSync != 0) {

                Date dateSync = new Date(dateLastSync);
                Date currentDate = Calendar.getInstance().getTime();

                try {
                    dateSync = new SimpleDateFormat("yyyy-MM-dd").parse(utils.dateToString(dateSync));
                    currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(utils.dateToString(currentDate));
                } catch (ParseException e) {
                    //e.printStackTrace();
                }
                if (currentDate.after(dateSync)) {
                    this.ws.getSegments(this);
                } else {
                    if (dateLastSync < dateLastSyncOnBoard) {
                        this.ws.getSegments(this);
                    }
                }
            } else {
                this.ws.getSegments(this);
            }


            if (jsonSegments != null && !jsonSegments.equals(""))
                try {
                    this.segments = new JSONObject(jsonSegments);
                    joinCustomSegments();
                    idSegment = (String) this.segments.get(segment.toLowerCase().trim());
                } catch (JSONException e) {
                    //e.printStackTrace();
                }

        }catch (Exception e){
            //Log.d("navegg","Error on get segment: "+segment);
        }

        return idSegment;
    }

    private void joinCustomSegments() {
        try {
            List<Integer> newCustomList = this.listCustomPermanent;
            String customSegment = (String) this.segments.getString("custom");
            if (!customSegment.equals("")) {
                for (String field : customSegment.split("-")) {
                    Integer customId = Integer.parseInt(field);
                    if (!newCustomList.contains(customId)) {
                        newCustomList.add(customId);
                    }
                }
            }
            this.segments.put("custom", TextUtils.join("-", newCustomList));
        } catch (Exception e) {
            //e.printStackTrace();
        }


    }


}
