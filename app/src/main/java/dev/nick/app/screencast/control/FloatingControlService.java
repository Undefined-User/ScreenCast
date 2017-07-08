package dev.nick.app.screencast.control;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.app.screencast.widget.FloatView;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;
import ezy.assist.compat.SettingsCompat;

/**
 * Created by Nick on 2017/6/28 14:43
 */

public class FloatingControlService extends Service implements FloatingController {

    private FloatView floatView;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Stub();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        floatView = new FloatView(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (SettingsCompat.canDrawOverlays(this)) {
            show();
        } else {
            SettingsCompat.manageDrawOverlays(this);
            SettingsProvider.get().setShowFloatControl(false);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hide();
    }

    @Override
    public void show() {
        floatView.attach();
        LoggerManager.getLogger(getClass()).debug("Show float control");
    }

    @Override
    public void hide() {
        floatView.detach();
    }

    @Override
    public boolean isShowing() {
        return floatView.isAttachedToWindow();
    }

    private class Stub extends Binder implements FloatingController {

        @Override
        public void show() {

        }

        @Override
        public void hide() {

        }

        @Override
        public boolean isShowing() {
            return false;
        }
    }
}
