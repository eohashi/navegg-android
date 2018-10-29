package navegg.bean;

import android.content.Context;
import android.content.SharedPreferences;
import navegg.main.Utils;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by william on 03/11/17.
 */

public class OnBoarding {

    private HashMap data;
    private int accountId;
    private SharedPreferences shaPref;
    private Utils utils;

    private long dateLastSync;

    public OnBoarding(SharedPreferences shaPref, Utils utils, int accountId, Context context) {
        this.shaPref = shaPref;
        this.utils = utils;
        this.accountId = accountId;
        this.dateLastSync = this.shaPref.getLong("dateLastSyncOnBoarding", 0);

        String json = this.shaPref.getString("onBoarding"+this.accountId,"");
        this.data = new Gson().fromJson(json, HashMap.class);

        if(this.data==null) {
            this.data = new HashMap() {};
        }
    }

    public boolean addInfo(String key, String value) {

        String _check_value = (String) this.data.get(key);

        if (_check_value != null) {
            if (_check_value.equals(value)) {
                Date dateSync = new Date(this.getDateLastSync());
                Date currentDate = Calendar.getInstance().getTime();
                try {
                     dateSync = new SimpleDateFormat("yyyy-MM-dd").parse(utils.dateToString(dateSync));
                     currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(utils.dateToString(currentDate));
                } catch (ParseException e) {}

                if (!currentDate.after(dateSync)) {
                    return false;
                }
            }
        }

        this.data.put(key,value);

        // Gson 2.3.0 problem in convert HashMap to Json
        // Fix create JSONObject native Java
        JSONObject objJson = new JSONObject(this.data);
        String jsonString = objJson.toString();

        this.shaPref.edit().putString("onBoarding"+this.accountId, jsonString).commit();

        this.__set_to_send_onBoarding(true);

        return true;
    }
    public void __set_to_send_onBoarding(Boolean status){
        this.shaPref.edit().putBoolean("toSendOnBoarding"+this.accountId, status).commit();
    }

    public Boolean hasToSendOnBoarding(){
        return this.shaPref.getBoolean("toSendOnBoarding"+this.accountId, false);
    }

    public String getInfo(String key) {
        return (String) this.data.get(key);
    }

    public HashMap<String, String> __get_hash_map(){
        return this.data;
    }

    public long getDateLastSync() {
        return this.dateLastSync;
    }

    public void setDateLastSync() {
        try {
            Long time  = Calendar.getInstance().getTime().getTime();
            this.shaPref.edit().putLong("dateLastSyncOnBoarding", time).apply();
            this.dateLastSync = time;
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}
