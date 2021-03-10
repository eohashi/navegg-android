package navegg.main;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Set;
import java.util.TreeSet;

import navegg.bean.User;
import navegg.broadcast.VerifyStateConnection;
import navegg.connection.WebService;

public class NaveggAPI extends MultiDexApplication {

    private Context context;
    protected WebService webService;
    private User user;

    public NaveggAPI(Context context, final int accountId) {

        this.user = new User(context, accountId);
        this.context = context;
        this.webService = new WebService(context);
        Utils utils = new Utils(context);

        this.user.setLastActivityName(utils.getActivityName());

        if(this.user.getUserId().equals("0")) {
            this.webService.createUserId(this.user);
        }

        this.registerReceiverAndAccountSdk(accountId);
    }

    private void registerReceiverAndAccountSdk(Integer accountId){
        SharedPreferences shaPref = context.getSharedPreferences("NVGSDKS", Context.MODE_PRIVATE);
        boolean broadCast = shaPref.getBoolean("broadCastRunning", false);
        if(!broadCast) {
            shaPref.edit().putBoolean("broadCastRunning", true).commit();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            VerifyStateConnection verifySC = new VerifyStateConnection();
            context.registerReceiver(new VerifyStateConnection(), intentFilter);

        }

        Gson gson = new Gson();
        String json = shaPref.getString("accounts","");
        Set<Integer> accounts = gson.fromJson(json, new TypeToken<TreeSet<Integer>>(){}.getType());

        if(accounts==null)
            accounts = new TreeSet<>();
        if(!accounts.contains(accountId)){
            accounts.add(accountId);
            json = gson.toJson(accounts);
            shaPref.edit().putString("accounts", json).commit();
        }
    }

    public String getSegments(String segment) {
        return this.user.getSegments(segment);
    }

    public void setOnBoarding(String key, String value) {
        if (this.user.setOnBoarding(key, value)) {
            this.webService.sendOnBoarding(this.user, this.user.getOnBoarding());
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(context);
    }
}

