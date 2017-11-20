package navegg.bean;

import java.util.HashMap;

/**
 * Created by william on 03/11/17.
 */

public class OnBoarding {

    private final HashMap data = new HashMap();

    public OnBoarding() {
    }

    public void addInfo(String key, String value) {

        this.data.put(key,value);
    }

    public String getInfo(String key) {
        return (String) this.data.get(key);
    }

    public HashMap<String, String> __get_hash_map(){
        return this.data;
    }

}
