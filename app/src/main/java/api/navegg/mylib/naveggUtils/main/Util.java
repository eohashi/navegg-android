package api.navegg.mylib.naveggUtils.main;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.webkit.WebView;

import com.google.gson.Gson;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.TimeZone;

import api.navegg.mylib.BuildConfig;
import api.navegg.mylib.R;
import api.navegg.mylib.naveggUtils.bean.Package;
import api.navegg.mylib.naveggUtils.bean.User;

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
        } else return "Not SendData";
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

    public long getCurrentDateTime() {
        long millis = 0;
        //long millis = System.currentTimeMillis();
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));

//Local time zone
        SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");

        try {
            millis = dateFormatLocal.parse( dateFormatGmt.format(Calendar.getInstance().getTime().getTime())).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return millis;

    }


    /* Pega pagina origem que chamou pagina atual
    * (Somente para activity)*/
    public String getCallPage() {

        lastActivityName = mSharedPreferences.getString("lastActivityName", "");
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(2);

        if (lastActivityName == "") {

            editor.putString("lastActivityName", taskInfo.get(0).topActivity.getClassName());
            editor.commit();

            lastActivityName = taskInfo.get(0).topActivity.getClassName();

        }

        Log.d("topActivity", "CURRENT Activity ::"
                + taskInfo.get(0).topActivity.getClassName());

        String activityName = taskInfo.get(0).topActivity.getClassName();

        if (!lastActivityName.equalsIgnoreCase(activityName)) {
            System.out.println("LASTPACKAGENAME " + lastActivityName);
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


    public boolean verifyConnection() {
        boolean connected;
        ConnectivityManager conectivtyManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conectivtyManager.getActiveNetworkInfo();
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

        System.out.println("TYPE SendData " + connected);

        return connected;
    }


    public boolean verifyConnectionWifi() {
        boolean connected;
        ConnectivityManager conectivtyManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conectivtyManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
         /*   if (conectivtyManager.getActiveNetworkInfo() != null
                    && conectivtyManager.getActiveNetworkInfo().isAvailable()
                    && conectivtyManager.getActiveNetworkInfo().isConnected())*/
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_ETHERNET) {
                connected = true;
            } else {
                connected = false;
            }
        } else {
            connected = false;
        }

        System.out.println("TYPE SendData " + connected);

        return connected;
    }


    public Package.MobileInfo setDataMobile(User user) {
        Gson gson = new Gson();
        String json = mSharedPreferences.getString("user", "");
        user = gson.fromJson(json, User.class);


        Package.MobileInfo mMobileInfo = Package.MobileInfo.newBuilder()
                .setDeviceId(Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.ANDROID_ID))
                .setPlatform("Android")
                .setLongitude(getLong())
                .setLatitude(getLat())
                .setAndroidName(Build.DEVICE)
                .setAndroidBrand(Build.BRAND)
                .setAndroidModel(Build.MODEL)
                .setVersionRelease(Build.VERSION.RELEASE)
                .setManufacturer(Build.MANUFACTURER)
                .setVersionLib(String.valueOf(BuildConfig.VERSION_CODE))
                .setVersionCode(BuildConfig.VERSION_CODE)
                .setVersionOS(Build.VERSION.SDK_INT)
                .setAndroidFingerPrint(Build.FINGERPRINT)
                .setUserAgent(new WebView(context).getSettings().getUserAgentString())
                .setLinkPlayStore(getLinkPlayStore())
                .setTypeCategory(getTypeCategory())
                .setImei(getIMEI())
                .setSoftwareVersion(getSoftwareVersion())
                .setAcc(user.getCodConta())
                .setUserId(user.getmNvgId())
                .build();


        return mMobileInfo;

    }


    public Package.Track setDataTrack(String mActivity, User user) {


        Package.Track track = Package.Track.newBuilder()
                .setAcc((user != null) ? user.getCodConta() : 100000000)
                .setUserId((user != null) ? user.getmNvgId() : 100000000)
                .setNameApp(context.getString(R.string.app_name))
                .setDeviceIP(getMobileIP(context))
                .setTypeConnection(getTypeConnection())
                .addPageViews(Package.PageView.newBuilder()
                        .setActivity(mActivity)
                        .setDateTime(getCurrentDateTime())
                        .setTitlePage(String.valueOf(((Activity) context).getTitle()))
                        .setCallPage(""))
                .build();


        return track;

    }



}
