
package info.daylemk.viewinplay;

import android.content.res.XModuleResources;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author DayLemK Liu
 *  @todo 1. an option to toggle Store and Browser </br>
 *        2. an option to switch 'Remove from list' to 'Force Stop'
 */
public class XposedInit implements IXposedHookLoadPackage, IXposedHookZygoteInit,
        IXposedHookInitPackageResources {
    private static final String TAG = "DayL";

    public static XModuleResources sModRes;

    static String KEY_DIRECTLY_SHOW_IN_PLAY;
    static String KEY_SHOW_IN_APP_INFO;
    static String KEY_SHOW_IN_RECENT_PANEL;
    static String KEY_SHOW_IN_NOTIFICATION;
    static String KEY_TWO_FINGER_IN_RECENT_PANEL;
    static String KEY_COMPAT_XHALO;
    static String KEY_COMPAT_FLOATING;
    // add for debug
    static String KEY_DEBUG_LOGS;
    // add for browser-store switch
    static String KEY_USE_BROWSER;
    
    static boolean directlyShowInPlay = false;
    // add for browser-store switch
    static boolean useBrowser = false;
    static boolean debuggable = false;
    // the compat for the PA floating mode
    static boolean bool_compat_floating = false;

    private static List<String> notStockApp;
    private static List<String> stockAndroidApp;
    private static String MODULE_PATH = null;
    private static XSharedPreferences mPref;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        mPref = new XSharedPreferences(Common.THIS_PACKAGE_NAME, Common.PREFERENCE_MAIN_FILE);
        MODULE_PATH = startupParam.modulePath;
        sModRes = XModuleResources.createInstance(MODULE_PATH, null);
        RecentTaskHook.initZygote(sModRes);
        AppInfoHook.initZygote(sModRes);

        KEY_DIRECTLY_SHOW_IN_PLAY = sModRes.getString(R.string.key_directly_show_in_play);
        KEY_SHOW_IN_RECENT_PANEL = sModRes.getString(R.string.key_show_in_recent_panel);
        KEY_SHOW_IN_APP_INFO = sModRes.getString(R.string.key_show_in_app_info);
        KEY_SHOW_IN_NOTIFICATION = sModRes.getString(R.string.key_show_in_notification);
        KEY_TWO_FINGER_IN_RECENT_PANEL = sModRes.getString(R.string.key_two_finger_in_recent_panel);
        KEY_COMPAT_XHALO = sModRes.getString(R.string.key_compat_xhalo);
        KEY_COMPAT_FLOATING = sModRes.getString(R.string.key_compat_floating);
        KEY_DEBUG_LOGS = sModRes.getString(R.string.key_debug_logs);
        // add for browser-store switch 
        KEY_USE_BROWSER = sModRes.getString(R.string.key_use_browser);

        notStockApp = Arrays.asList(sModRes.getStringArray(R.array.not_stock_app));
        stockAndroidApp = Arrays.asList(sModRes.getStringArray(R.array.stock_android_app));

        XposedBridge.log(TAG + "[]init done");
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam)
            throws Throwable {
        loadPref(lpparam);

        RecentTaskHook.handleLoadPackage(lpparam, mPref);
        AppInfoHook.handleLoadPackage(lpparam, mPref);
        // this status bar should call after RecentTaskHook
        StatusBarHook.handleLoadPackage(lpparam, mPref);
    }

    private void loadPref(final LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.android.systemui")
                // FIXED the directly view in play not effect in the app info
                // screen
                && !lpparam.packageName.equals("com.android.settings"))
            return;

        mPref.reload();
        directlyShowInPlay = mPref.getBoolean(KEY_DIRECTLY_SHOW_IN_PLAY,
                Common.DEFAULT_DIRECTLY_SHOW_IN_PLAY);
        // add for browser-store switch
        useBrowser = mPref.getBoolean(KEY_USE_BROWSER, Common.DEFAULT_USE_BROWSER);
        // debug
        debuggable = mPref.getBoolean(KEY_DEBUG_LOGS, Common.DEFAULT_DEBUG_LOGS);
        
        Common.debugLog(TAG + "[]lpparam.packageName:" + lpparam.packageName);
        Common.debugLog(TAG + "[]the directly is " + directlyShowInPlay);
        
        // if we are in the settings process, return, no need to get floating mode
        if (lpparam.packageName.equals("com.android.settings")) {
            return;
        }
        // PA floating mode
        bool_compat_floating = mPref.getBoolean(XposedInit.KEY_COMPAT_FLOATING,
                Common.DEFAULT_COMPAT_FLOATING);
    }

    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
        AppInfoHook.handleInitPackageResources(resparam);
    }

    public static boolean isStockAndroidApp(String pkgName) {
        if (stockAndroidApp.contains(pkgName))
            return true;
        return false;
    }

    public static boolean isNotStockApp(String pkgName) {
        if (notStockApp.contains(pkgName))
            return true;
        return false;
    }
}
