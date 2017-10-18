package dev.nick.app.screencast.widget;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public class OneSecondToast extends Handler {

    public void show(Context context, String content) {
        final Toast toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
        toast.show();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 1000);
    }
}
