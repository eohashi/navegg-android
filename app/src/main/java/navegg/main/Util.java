package navegg.main;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.support.v4.BuildConfig;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.webkit.WebView;

import com.google.gson.Gson;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import navegg.R;
import navegg.bean.MobileInfo;
import navegg.bean.Package;
import navegg.bean.PageView;
import navegg.bean.Track;
import navegg.bean.User;

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
    List<Package.PageView> listPackagePageView = new ArrayList<>();


    public Util(Context context) {
        this.context = context;
        util = new LocationPosition(context);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            List<ActivityManager.AppTask> taskInfo = null;
        }else{
            List<ActivityManager.RunningTaskInfo> taskInfo = null;
        }
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
                == PackageManager.PERMISSION_DENIED) {

            return "Software Sem permissão";

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
    public String getCallPage() {

        lastActivityName = mSharedPreferences.getString("lastActivityName", "");
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        String activityName = "";

        List<ActivityManager.AppTask> tasks = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            tasks = am.getAppTasks();



            for (ActivityManager.AppTask task : tasks) {
                if (lastActivityName == "") {

                    editor.putString("lastActivityName", task.getTaskInfo().topActivity.getClassName().toString());
                    editor.commit();

                    lastActivityName = task.getTaskInfo().topActivity.getClassName().toString();;

                }
                activityName = task.getTaskInfo().topActivity.getClassName().toString();
            }
        }else {

            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(2);
            if (lastActivityName == "") {

                editor.putString("lastActivityName", taskInfo.get(0).topActivity.getClassName());
                editor.commit();

                lastActivityName = taskInfo.get(0).topActivity.getClassName();

            }

            activityName = taskInfo.get(0).topActivity.getClassName();


        }



        if (!lastActivityName.equalsIgnoreCase(activityName)) {
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


    public Package.MobileInfo setDataMobile(MobileInfo mobileInfo) {


        Package.MobileInfo mMobileInfo = Package.MobileInfo.newBuilder()
                .setDeviceId(mobileInfo.getDeviceId())
                .setPlatform(mobileInfo.getPlatform())
                .setLongitude(mobileInfo.getLongitude())
                .setLatitude(mobileInfo.getLatitude())
                .setAndroidName(mobileInfo.getAndroidName())
                .setAndroidBrand(mobileInfo.getAndroidBrand())
                .setAndroidModel(mobileInfo.getAndroidModel())
                .setVersionRelease(mobileInfo.getVersionRelease())
                .setManufacturer(mobileInfo.getManufacturer())
                .setVersionLib(mobileInfo.getVersionLib())
                .setVersionCode(mobileInfo.getVersionCode())
                .setVersionOS(mobileInfo.getVersionOS())
                .setAndroidFingerPrint(mobileInfo.getAndroidFingerPrint())
                .setUserAgent(mobileInfo.getUserAgent())
                .setLinkPlayStore(mobileInfo.getLinkPlayStore())
                .setTypeCategory(mobileInfo.getTypeCategory())
                .setImei(mobileInfo.getImei())
                .setSoftwareVersion(mobileInfo.getSoftwareVersion())
                .setAcc(mobileInfo.getAcc())
                .setUserId(mobileInfo.getUserId())
                .build();

        return mMobileInfo;

    }

    public MobileInfo setDataMobileInfo(User user){
        Gson gson = new Gson();
        String json = mSharedPreferences.getString("user", "");
        user = gson.fromJson(json, User.class);

        MobileInfo mobileInfo =
                new MobileInfo();
        mobileInfo.setDeviceId(Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID));
        mobileInfo.setPlatform("Android");
        mobileInfo.setLongitude(getLong());
        mobileInfo.setLatitude(getLat());
        mobileInfo.setAndroidName(Build.DEVICE);
        mobileInfo.setAndroidBrand(Build.BRAND);
        mobileInfo.setAndroidModel(Build.MODEL);
        mobileInfo.setVersionRelease(Build.VERSION.RELEASE);
        mobileInfo.setManufacturer(Build.MANUFACTURER);
        mobileInfo.setVersionLib(BuildConfig.VERSION_NAME);
        mobileInfo.setVersionCode(BuildConfig.VERSION_CODE);
        mobileInfo.setVersionOS(Build.VERSION.SDK_INT);
        mobileInfo.setAndroidFingerPrint(Build.FINGERPRINT);
        mobileInfo.setUserAgent(new WebView(context).getSettings().getUserAgentString());
        mobileInfo.setLinkPlayStore(getLinkPlayStore());
        mobileInfo.setTypeCategory(getTypeCategory());
        mobileInfo.setImei(getIMEI());
        mobileInfo.setSoftwareVersion(getSoftwareVersion());
        mobileInfo.setAcc(user.getCodConta());
        mobileInfo.setUserId(user.getmNvgId());


        return mobileInfo;
    }


    public Package.Track setDataTrack(Track track, List<Package.PageView> listPageView) {


        Package.Track packageTrack = Package.Track.newBuilder()
                .setAcc(track.getAcc())
                .setUserId(track.getUserId())
                .setNameApp(track.getNameApp())
                .setDeviceIP(track.getDeviceIP())
                .setTypeConnection(track.getTypeConnection())
                .addAllPageViews(listPageView)
                .build();


        return packageTrack;

    }




    public Track setDataBeanTrack(User user, List<PageView> pageView){

        Track track = new Track();
        track.setAcc((user != null) ? user.getCodConta() : 100000000);
        track.setUserId((user != null) ? user.getmNvgId() : 100000000);
        track.setNameApp(context.getString(R.string.app_name));
        track.setDeviceIP(getMobileIP(context));
        track.setTypeConnection(getTypeConnection());
        track.setPageViews(pageView);

        return track;
    }


    public List<Package.PageView> setListDataPageView(List<PageView> pageView) {


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


    public PageView setDataPageView(String mActivity){

        PageView pageView = new PageView();
        pageView.setActivity(mActivity);
        pageView.setDateTime(getCurrentDateTime());
        pageView.setTitlePage(String.valueOf(((Activity) context).getTitle()));
        pageView.setCallPage("");

        return pageView;
    }






}
