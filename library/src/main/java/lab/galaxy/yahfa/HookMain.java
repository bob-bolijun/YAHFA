package lab.galaxy.yahfa;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import lab.galaxy.yahfa.annotation.HookClass;
import lab.galaxy.yahfa.annotation.HookMethod;

/**
 * Created by liuruikai756 on 28/03/2017.
 */

public class HookMain {
    private static final String TAG = "YAHFA";
    private static List<Class<?>> hookInfoClasses = new LinkedList<>();

    static {
        System.loadLibrary("yahfa");
        init(android.os.Build.VERSION.SDK_INT);
        HookMethodResolver.init();
    }

    public static void doHookDefault(Class<?> hookInfoClass) {
        try {
            String[] hookItemNames = (String[]) hookInfoClass.getField("hookClassNames").get(null);
            for (String hookItemName : hookItemNames) {
                doHookItemDefault(hookItemName);
            }
            hookInfoClasses.add(hookInfoClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void doHookItemDefault(String hookItemName) {
        try {
            Log.i(TAG, "Start hooking with item " + hookItemName);
            Class<?> hookItem = Class.forName(hookItemName);

            HookClass hookClass = hookItem.getAnnotation(HookClass.class);
            if(hookClass == null){
                Log.i(TAG, "Not add HookClass Anotation on " + hookItemName);
                return;
            }

            String className = hookClass.name();
            if (className == null || className.equals("")) {
                Log.w(TAG, "No target class. Skipping...");
                return;
            }

            Method[] methods = hookItem.getDeclaredMethods();
            for(Method method : methods){
                HookMethod hookMethod = method.getAnnotation(HookMethod.class);
                if(hookMethod == null) {
                    continue;
                }
                String methodName = hookMethod.name();
                if(methodName == null || methodName.isEmpty()) {
                    methodName = method.getName();
                }
                String methodSig = hookMethod.signature();

                Class<?> clazz = Class.forName(className);
                if (Modifier.isAbstract(clazz.getModifiers())) {
                    Log.w(TAG, "HookMethod may fail for abstract class: " + className);
                }

                Method backup = null;
                String hookedMethodName = method.getName();
                for (Method methodBackup : hookItem.getDeclaredMethods()) {
                    if (methodBackup.getName().equals(hookedMethodName + "_org")) {
                        backup = methodBackup;
                        break;
                    }
                }
                findAndBackupAndHook(clazz, methodName, methodSig, method, backup);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doHookDefault(Class<?> hookInfoClass, ClassLoader originClassLoader) {
        try {
             String[] hookItemNames = (String[]) hookInfoClass.getField("hookItemNames").get(null);
            for (String hookItemName : hookItemNames) {
                doHookItemDefault(originClassLoader, hookItemName, originClassLoader);
            }
            hookInfoClasses.add(hookInfoClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doHookDefault(ClassLoader patchClassLoader, ClassLoader originClassLoader) {
        try {
            Class<?> hookInfoClass = Class.forName("lab.galaxy.yahfa.HookInfo", true, patchClassLoader);
            String[] hookItemNames = (String[]) hookInfoClass.getField("hookItemNames").get(null);
            for (String hookItemName : hookItemNames) {
                doHookItemDefault(patchClassLoader, hookItemName, originClassLoader);
            }
            hookInfoClasses.add(hookInfoClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void doHookItemDefault(ClassLoader patchClassLoader, String hookItemName, ClassLoader originClassLoader) {
        try {
            Log.i(TAG, "Start hooking with item " + hookItemName);
            Class<?> hookItem = Class.forName(hookItemName, true, patchClassLoader);

            String className = (String) hookItem.getField("name").get(null);
            String methodName = (String) hookItem.getField("name").get(null);
            String methodSig = (String) hookItem.getField("methodSig").get(null);

            if (className == null || className.equals("")) {
                Log.w(TAG, "No target class. Skipping...");
                return;
            }
            Class<?> clazz = Class.forName(className, true, originClassLoader);
            if (Modifier.isAbstract(clazz.getModifiers())) {
                Log.w(TAG, "HookMethod may fail for abstract class: " + className);
            }

            Method hook = null;
            Method backup = null;
            for (Method method : hookItem.getDeclaredMethods()) {
                if (method.getName().equals("hook") && Modifier.isStatic(method.getModifiers())) {
                    hook = method;
                } else if (method.getName().equals("backup") && Modifier.isStatic(method.getModifiers())) {
                    backup = method;
                }
            }
            if (hook == null) {
                Log.e(TAG, "Cannot find hook for " + methodName);
                return;
            }
            findAndBackupAndHook(clazz, methodName, methodSig, hook, backup);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void findAndHook(Class targetClass, String methodName, String methodSig, Method hook) {
        hook(findMethod(targetClass, methodName, methodSig), hook);
    }

    public static void findAndBackupAndHook(Class targetClass, String methodName, String methodSig,
                                            Method hook, Method backup) {
        backupAndHook(findMethod(targetClass, methodName, methodSig), hook, backup);
    }

    public static void hook(Object target, Method hook) {
        backupAndHook(target, hook, null);
    }

    public static void backupAndHook(Object target, Method hook, Method backup) {
        if (target == null) {
            throw new IllegalArgumentException("null target method");
        }
        if (hook == null) {
            throw new IllegalArgumentException("null hook method");
        }

        if (!Modifier.isStatic(hook.getModifiers())) {
            throw new IllegalArgumentException("HookMethod must be a static method: " + hook);
        }
        checkCompatibleMethods(target, hook, "Original", "HookMethod");
        if (backup != null) {
            if (!Modifier.isStatic(backup.getModifiers())) {
                throw new IllegalArgumentException("Backup must be a static method: " + backup);
            }
            // backup is just a placeholder and the constraint could be less strict
            checkCompatibleMethods(target, backup, "Original", "Backup");
        }
        if (backup != null) {
            HookMethodResolver.resolveMethod(hook, backup);
        }
        if (!backupAndHookNative(target, hook, backup)) {
            throw new RuntimeException("Failed to hook " + target + " with " + hook);
        }
    }

    private static Object findMethod(Class cls, String methodName, String methodSig) {
        if (cls == null) {
            throw new IllegalArgumentException("null class");
        }
        if (methodName == null) {
            throw new IllegalArgumentException("null method name");
        }
        if (methodSig == null) {
            throw new IllegalArgumentException("null method signature");
        }
        return findMethodNative(cls, methodName, methodSig);
    }

    private static void checkCompatibleMethods(Object original, Method replacement, String originalName, String replacementName) {
        ArrayList<Class<?>> originalParams;
        if (original instanceof Method) {
            originalParams = new ArrayList<>(Arrays.asList(((Method) original).getParameterTypes()));
        } else if (original instanceof Constructor) {
            originalParams = new ArrayList<>(Arrays.asList(((Constructor<?>) original).getParameterTypes()));
        } else {
            throw new IllegalArgumentException("Type of target method is wrong");
        }

        ArrayList<Class<?>> replacementParams = new ArrayList<>(Arrays.asList(replacement.getParameterTypes()));

        if (original instanceof Method
                && !Modifier.isStatic(((Method) original).getModifiers())) {
            originalParams.add(0, ((Method) original).getDeclaringClass());
        } else if (original instanceof Constructor) {
            originalParams.add(0, ((Constructor<?>) original).getDeclaringClass());
        }


        if (!Modifier.isStatic(replacement.getModifiers())) {
            replacementParams.add(0, replacement.getDeclaringClass());
        }

        if (original instanceof Method
                && !replacement.getReturnType().isAssignableFrom(((Method) original).getReturnType())) {
            throw new IllegalArgumentException("Incompatible return types. " + originalName + ": " + ((Method) original).getReturnType() + ", " + replacementName + ": " + replacement.getReturnType());
        } else if (original instanceof Constructor) {
            if (replacement.getReturnType().equals(Void.class)) {
                throw new IllegalArgumentException("Incompatible return types. " + "<init>" + ": " + "V" + ", " + replacementName + ": " + replacement.getReturnType());
            }
        }

        if (originalParams.size() != replacementParams.size()) {
            throw new IllegalArgumentException("Number of arguments don't match. " + originalName + ": " + originalParams.size() + ", " + replacementName + ": " + replacementParams.size());
        }

        for (int i = 0; i < originalParams.size(); i++) {
            if (!replacementParams.get(i).isAssignableFrom(originalParams.get(i))) {
                throw new IllegalArgumentException("Incompatible argument #" + i + ": " + originalName + ": " + originalParams.get(i) + ", " + replacementName + ": " + replacementParams.get(i));
            }
        }
    }

    private static native boolean backupAndHookNative(Object target, Method hook, Method backup);

    public static native void ensureMethodCached(Method hook, Method backup);

    // JNI.ToReflectedMethod() could return either Method or Constructor
    public static native Object findMethodNative(Class targetClass, String methodName, String methodSig);

    private static native void init(int sdkVersion);
}
