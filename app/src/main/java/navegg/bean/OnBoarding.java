package navegg.bean;

import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Created by william on 03/11/17.
 */

public class OnBoarding {

    private HashMap data;
    private  SharedPreferences shaPref;

    public OnBoarding(SharedPreferences shaPref) {
        this.shaPref = shaPref;
        String json = this.shaPref.getString("onBoarding","");
        this.data = new Gson().fromJson(json, HashMap.class);
        if(this.data==null)
            this.data = new HashMap(){};
    }

    public void addInfo(String key, String value) {

        this.data.put(key,value);
        String jsonString = new Gson().toJson(this.data);
        this.shaPref.edit().putString("onBoarding", jsonString).commit();
        this.__set_to_send_onBoarding(true);
    }
    public void __set_to_send_onBoarding(Boolean status){
        this.shaPref.edit().putBoolean("toSendOnBoarding", status).commit();
    }

    public Boolean hasToSendOnBoarding(){
        return this.shaPref.getBoolean("toSendOnBoarding", false);
    }

    public String getInfo(String key) {
        return (String) this.data.get(key);
    }

    public HashMap<String, String> __get_hash_map(){
        return this.data;
    }

}
