package dev.nick.app.screencast.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Factory implements Application.ActivityLifecycleCallbacks {

    private static Impl sImpl;

    public synchronized static Factory get() {
        if (sImpl == null)
            sImpl = new Impl();
        return sImpl;
    }

    public abstract Context getApplicationContext();

    public abstract void onApplicationCreate(Application application);

    public abstract Activity getTopActivity();

    public abstract boolean integratedAD();

    public abstract boolean isFFMpegAvailable();

    abstract void setFFMpegAvailable();

    private static class Impl extends Factory {

        Activity mTopActivity;
        Application mApp;
        AtomicBoolean ffmpegAvailable = new AtomicBoolean(false);

        @Override
        public Context getApplicationContext() {
            return mApp;
        }

        @Override
        public void onApplicationCreate(Application application) {
            mApp = application;
        }

        @Override
        public Activity getTopActivity() {
            return mTopActivity;
        }

        @Override
        public boolean integratedAD() {
            return LocalBuildConfig.HAS_AD;
        }

        @Override
        public boolean isFFMpegAvailable() {
            return ffmpegAvailable.get();
        }

        @Override
        public void setFFMpegAvailable() {
            ffmpegAvailable.set(true);
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            mTopActivity = activity;
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native dev.nick.library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' dev.nick.library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
