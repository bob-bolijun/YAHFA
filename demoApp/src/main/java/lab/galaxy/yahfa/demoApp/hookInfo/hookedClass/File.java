package lab.galaxy.yahfa.demoApp.hookInfo.hookedClass;

import android.util.Log;

import lab.galaxy.yahfa.annotation.HookClass;
import lab.galaxy.yahfa.annotation.HookMethod;

import static lab.galaxy.yahfa.demoApp.hookInfo.HookInfo.TAG;

@HookClass(name = "java.io.File")
public class File {
    @HookMethod(name="<init>", signature = "(Ljava/lang/String;)V")
    public static void constor(Object thiz, String path) {
        Log.w(TAG, "new File() ");
        constor_org(thiz, path);
    }

    public static void constor_org(Object thiz, String path) {
        Log.w(TAG, "new File() should not be here");
    }

    @HookMethod(signature = "()Z")
    public static boolean exists(java.io.File thiz) {
        Log.w(TAG, "in File.exists(): " + thiz.getAbsolutePath());
        //return exists_org(thiz);
        return true;
    }

    public static boolean exists_org(java.io.File thiz) {
        Log.w(TAG, "File.exists() should not be here");
        return false;
    }

    @HookMethod(signature = "()J")
    public static long length(java.io.File thiz) {
        Log.w(TAG, "in File.exists(): " + thiz.getAbsolutePath());
        //return length_org(thiz);
        return 100;
    }

    public static long length_org(java.io.File thiz) {
        Log.w(TAG, "File.exists() should not be here");
        return 100;
    }

    @HookMethod(signature = "()[Ljava/io/File;")
    public static java.io.File[] listRoots() {
        Log.w(TAG, "File.listRoots");
        //listRoots_org();
        return new java.io.File[]{new java.io.File("aaaa"), new java.io.File("bbbb")};
    }

    public static java.io.File[] listRoots_org() {
        Log.w(TAG, "File.listRoots should not be here");
        return null;
    }

}
