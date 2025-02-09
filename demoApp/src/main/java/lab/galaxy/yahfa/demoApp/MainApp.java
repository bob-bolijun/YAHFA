package lab.galaxy.yahfa.demoApp;

import android.app.Application;
import android.content.Context;

import dalvik.system.DexClassLoader;
import lab.galaxy.yahfa.HookMain;
import lab.galaxy.yahfa.demoApp.hookInfo.HookInfo;

/**
 * Created by liuruikai756 on 30/03/2017.
 */

public class MainApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        try {
        /*
        Build and put the demoPlugin apk in sdcard before running the demoApp
         */
            //ClassLoader classLoader = getClassLoader();

            //DexClassLoader dexClassLoader = new DexClassLoader("/sdcard/demoPlugin-debug.apk",
           //         getCodeCacheDir().getAbsolutePath(), null, classLoader);
            //HookMain.doHookDefault(dexClassLoader, classLoader);

            //HookMain.doHookDefault(HookInfo.class, getClassLoader());
            HookMain.doHookDefault(HookInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
