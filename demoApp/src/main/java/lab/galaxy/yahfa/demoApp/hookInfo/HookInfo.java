package lab.galaxy.yahfa.demoApp.hookInfo;

import lab.galaxy.yahfa.demoApp.hookInfo.hookedClass.File;

public class HookInfo {
    public static final String TAG = "HookInfo";
    public static String[] hookClassNames = {
            File.class.getName(),
    };
}
