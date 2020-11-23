package navegg.main;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.IntRange;
import androidx.core.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import navegg.BuildConfig;
import navegg.R;
import navegg.bean.Package;
import navegg.bean.PageView;
import navegg.bean.User;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;


/**
 * Created by william on 23/05/17.
 */

public class Utils {

    private Context context;
    LocationPosition util;
    String lastActivityName = null;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;

    public Utils(Context context) {
        this.context = context;
        util = new LocationPosition(context);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            List<ActivityManager.AppTask> taskInfo = null;
        }else{
            List<ActivityManager.RunningTaskInfo> taskInfo = null;
        }
        this.mSharedPreferences = context.getSharedPreferences("NVGSDK", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();

    }


    public static String getMobileIP(Context context) {
        String ipAddress = "No Connection";

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
                //ex.printStackTrace();
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
            return "No GPS";
        }
    }

    public String getLong() {
        if (util.canGetLocation()) {
            return String.valueOf(util.getLongitude());
        } else {
            return "No GPS";
        }
    }

    public String getTypeConnection() {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        try {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return "WIFI";
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return "LTE";
            } else return "No Connection";
        }catch (Exception e){
            return "No Connection";
        }
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

            return "IMEI without permission";

        } else {
            TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String IMEI = mTelephonyManager.getDeviceId();

            return "";
        }
    }


    public String getSoftwareVersion() {


        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_DENIED) {

            return "Software without permission";

        } else {
            TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String mSoftwareVersion = mTelephonyManager.getDeviceSoftwareVersion();
            if(mSoftwareVersion == null ){
                mSoftwareVersion = "";
            }
            return mSoftwareVersion;
        }
    }

    public long getCurrentDateTime() {
        long millis = System.currentTimeMillis();
        return millis;

    }


    /* Pega pagina origem que chamou pagina atual
    * (Somente para activity)*/
    @TargetApi(Build.VERSION_CODES.M)
    public String getActivityName() {


        String activityName = "";
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> tasks = null;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                tasks = am.getAppTasks();

                for (ActivityManager.AppTask task : tasks) {
                    ComponentName topActivity = task.getTaskInfo().topActivity;
                    if (topActivity == null) continue;
                    activityName = topActivity.getClassName();
                }
            } else {
                List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(2);
                activityName = taskInfo.get(0).topActivity.getClassName();
            }
        }catch (Exception e){}

        return activityName;
    }

    /* Pega pagina origem que chamou pagina atual
        Ainda em Teste
       (Funciona somente para Fragment)
     */
/*    @SuppressWarnings("RestrictedApi")
    public Fragment getVisibleFragment() {
        String fragClassName = null;
        List<Fragment> fragments = ((FragmentActivity) context).getSupportFragmentManager().getFragments();
        if(fragments == null) return null;
        for (Fragment fragment : fragments) {
            if (fragment != null) {
                fragClassName = fragment.getClass().getName();
                return fragment;
            }
        }
        return null;


    }*/


    public boolean verifyConnection() {
        boolean connected = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        connected = true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        connected = true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        connected = true;
                    }
                }
            }
        }
        return connected;
        /*boolean connected;
        ConnectivityManager conectivtyManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        int activeNetwork = getConnectionType(context.getSystemService(Context.CONNECTIVITY_SERVICE));
        if (activeNetwork != null) {
            if (conectivtyManager.getActiveNetworkInfo() != null
                    && conectivtyManager.getActiveNetworkInfo().isAvailable()
                    && conectivtyManager.getActiveNetworkInfo().isConnected()) {
                connected = true;
            } else {
                connected = false;
            }
        } else {
            connected = false;
        }


        return connected;*/
    }

    public boolean verifyConnectionWifi() {
        boolean connected;
        ConnectivityManager conectivtyManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conectivtyManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
         /*   if (conectivtyManager.getActiveNetworkInfo() != null
                    && conectivtyManager.getActiveNetworkInfo().isAvailable()
                    && conectivtyManager.getActiveNetworkInfo().isConnected())*/
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE || activeNetwork.getType() == ConnectivityManager.TYPE_ETHERNET || activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                connected = true;
            } else {
                connected = false;
            }
        } else {
            connected = false;
        }

        return connected;
    }





    public Package.Track setDataTrack(User user, List<Package.PageView> listPageView) {


        Package.Track packageTrack = Package.Track.newBuilder()
                .setAcc(user.getAccountId())
                .setUserId(user.getUserId())
                .setNameApp(context.getString(R.string.app_name))
                .setDeviceIP(getMobileIP(context))
                .setTypeConnection(getTypeConnection())
                .addAllPageViews(listPageView)
                .build();


        return packageTrack;

    }



    public List<Package.PageView> setListDataPageView(List<PageView> pageView) {

        List<Package.PageView> listPackagePageView = new ArrayList<>();
        for(PageView pageViews : pageView) {

            Package.PageView packagePageView = Package.PageView.newBuilder()
                    .setActivity(pageViews.getActivity())
                    .setDateTime(pageViews.getDateTime())
                    .setTitlePage(pageViews.getTitlePage())
                    .setCallPage("").build();

            listPackagePageView.add(packagePageView);
        }

        return listPackagePageView;

    }


    public String dateToString(Date date){
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public Date stringToDate(String date){
        Date dateFormated = null;
        try {
            dateFormated = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            //e.printStackTrace();
        }

        return dateFormated;
    }

}
