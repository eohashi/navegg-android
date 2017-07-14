package api.navegg.mylib.naveggUtils.main;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;


/**
 * Created by william on 23/05/17.
 */

public class Util {

    private Context context;
    LocationPosition util;
    String lastActivityName = null;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;

    public Util(Context context) {
        this.context = context;
        util = new LocationPosition(context);

        this.mSharedPreferences = context.getSharedPreferences("SDK", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();

    }


    public static String getMobileIP(Context context) {
        String ipAddress = null;

        if (isConnectedMobile(context)) {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface
                        .getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            ipAddress = inetAddress.getHostAddress().toString();
                        }
                    }
                }
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
        } else if (isConnectedWifi(context)) {
            WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
            ipAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        }
        return ipAddress;
    }

    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    private static boolean isConnectedMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }


    private static boolean isConnectedWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }


    public String getLat() {
        if (util.canGetLocation()) {
            return String.valueOf(util.getLatitude());
        } else {
            return "SEM GPS";
        }
    }

    public String getLong() {
        if (util.canGetLocation()) {
            return String.valueOf(util.getLongitude());
        } else {
            return "SEM GPS";
        }
    }

    public String getTypeConnection() {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();

        if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
            return "WIFI";
        } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
            return "LTE";
        } else return "Not Connection";
    }

    public String getTypeCategory() {
        if (isTablet(context)) {
            return "TABLET";
        } else {
            return "PHONE";
        }
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }


    public String getLinkPlayStore() {

        final String appPackageName = context.getPackageName();
        return "http://play.google.com/store/apps/details?id=" + appPackageName;

    }


    public String getIMEI() {

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            return "IMEI Sem permissão";

        } else {
            TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String IMEI = mTelephonyManager.getDeviceId();

            return IMEI;
        }
    }


    public String getSoftwareVersion() {

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {


            return "Software Sem permissão";


        } else {
            TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String mSoftwareVersion = mTelephonyManager.getDeviceSoftwareVersion();

            return mSoftwareVersion;

        }
    }

    public long getCurrentDateTime(){
        long millis = System.currentTimeMillis();
        return millis;

    }


    /* Pega pagina origem que chamou pagina atual
    * (Somente para activity)*/
    public String getCallPage() {

        lastActivityName = mSharedPreferences.getString("lastActivityName", "");
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);

        List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(2);

        if(lastActivityName == "" ){

            editor.putString("lastActivityName", taskInfo.get(0).topActivity.getClassName());
            editor.commit();

            lastActivityName =  taskInfo.get(0).topActivity.getClassName();

        }

        Log.d("topActivity", "CURRENT Activity ::"
                + taskInfo.get(0).topActivity.getClassName());

        String activityName = taskInfo.get(0).topActivity.getClassName();

        if(!lastActivityName.equalsIgnoreCase(activityName)){
            System.out.println("LASTPACKAGENAME "+ lastActivityName);
            return lastActivityName;
        }

        lastActivityName = activityName;

        return "";
    }

    /* Pega pagina origem que chamou pagina atual
        Ainda em Teste
       (Funciona somente para Fragment)
     */
    @SuppressWarnings("RestrictedApi")
    public Fragment getVisibleFragment() {
        String fragClassName = null;
        List<Fragment> fragments = ((FragmentActivity) context).getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null) {
                fragClassName = fragment.getClass().getName();
                return fragment;
            }
        }
        return null;


    }


    public  boolean verifyConnection() {
        boolean connected;
        ConnectivityManager conectivtyManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conectivtyManager.getActiveNetworkInfo();
        if(activeNetwork != null) {
         /*   if (conectivtyManager.getActiveNetworkInfo() != null
                    && conectivtyManager.getActiveNetworkInfo().isAvailable()
                    && conectivtyManager.getActiveNetworkInfo().isConnected())*/
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_ETHERNET ){
                connected = true;
            } else {
                connected = false;
            }
        }else{
            connected = false;
        }

        System.out.println("TYPE Connection "+ connected);

        return connected;
    }


}
