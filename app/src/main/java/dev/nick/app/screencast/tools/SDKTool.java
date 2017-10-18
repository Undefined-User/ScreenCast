package dev.nick.app.screencast.tools;

import android.os.Build;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public class SDKTool {
    public static boolean isOOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }
}
