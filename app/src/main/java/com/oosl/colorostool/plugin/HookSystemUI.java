package com.oosl.colorostool.plugin;

import android.app.Application;
import android.content.Context;

import com.oosl.colorostool.util.ColorToolPrefs;
import com.oosl.colorostool.util.CosApkName;
import com.oosl.colorostool.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class HookSystemUI extends HookBase {

    private static final String redTextClock;
    private static final String redHorizontalDualClock;

    static {
        if (CosApkName.isCos12()) {
            redTextClock = "com.oplusos.systemui.keyguard.clock.RedTextClock";
            redHorizontalDualClock = "com.oplusos.systemui.keyguard.clock.RedHorizontalDualClock";
        } else {
            redTextClock = "com.coloros.systemui.keyguard.clock.RedTextClock";
            redHorizontalDualClock = "com.coloros.systemui.keyguard.clock.RedHorizontalDualClock";
        }
    }

    @Override
    public void hook() {
        super.hook();
        if (ColorToolPrefs.getPrefs("lock_red_one", false)) {
            hookRedOne();
        }
    }

    private void hookRedOne() {
        String tag = "SystemUI";
        Log.d(tag, "Hook SystemUI success!");
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Class<?> clazz, clazz1;
                ClassLoader cl = ((Context) param.args[0]).getClassLoader();
                try {
                    clazz = cl.loadClass(redTextClock);
                    //clazz1 = cl.loadClass(redHorizontalDualClock);
                    Log.d(tag, "Hook Class success!");

                    // the read one in lock screen
                    XposedHelpers.setStaticObjectField(clazz, "NUMBER_ONE", "");
                    //XposedHelpers.setObjectField(clazz1,"NUMBER_ONE","");
                    Log.d(tag, "Hook RedClock success!");

                } catch (Exception e) {
                    Log.error(tag,e);
                }
            }
        });
    }

    @Override
    public void hookLog() {
        super.hookLog();
        // disable SystemUI Log if unnecessary
        if (enableLog) return;
        String tag = "SystemUILog";
        Log.d(tag, "Hook SystemUILog success!");
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Class<?> clazz;
                ClassLoader cl = ((Context) param.args[0]).getClassLoader();
                try {
                    clazz = cl.loadClass("com.oplusos.systemui.common.util.LogUtil");
                    Log.d(tag, "Hook Class success!");

                    // hook SystemUI_LogUtil
                    XposedHelpers.findAndHookMethod(clazz, "isTagEnable", String.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Log.d(tag, "br log tag is " + param.getResult());
                            param.setResult(true);
                            Log.d(tag, "af log tag is " + param.getResult());
                        }
                    });
                    XposedHelpers.findAndHookMethod(clazz, "updateLevel", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Log.d(tag, "af updateLevel is " + XposedHelpers.getStaticBooleanField(clazz, "sNormal"));
                            XposedHelpers.setStaticBooleanField(clazz, "sNormal", true);
                            Log.d(tag, "af updateLevel is " + XposedHelpers.getStaticBooleanField(clazz, "sNormal"));
                        }
                    });
                } catch (Exception e) {
                    Log.error(tag, e);
                }
            }
        });
    }
}
