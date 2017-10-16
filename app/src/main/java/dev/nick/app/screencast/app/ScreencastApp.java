package dev.nick.app.screencast.app;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.nick.app.screencast.BuildConfig;
import dev.nick.app.screencast.cast.IScreencaster;
import dev.nick.app.screencast.cast.ScreencastServiceProxy;
import dev.nick.app.screencast.content.DialogScreenCastActivity;
import dev.nick.app.screencast.tools.Collections;
import dev.nick.app.screencast.tools.Consumer;
import dev.nick.app.screencast.tools.SharedExecutor;
import dev.nick.logger.LoggerManager;

public class ScreencastApp extends Application {

    public static final String AD_APP_ID = "2882303761517617098";
    public static final String AD_FEED_SMALL = "44590c6cde68340ae9e6842192a03d55";
    public static final String AD_BANNER = "20676c78b42d76cd9ee03f97818e7342";
    public static final int AD_FEED_TYPE_SMALL_PIC = 1;


    private AtomicBoolean mIsCasting = new AtomicBoolean(false);

    public boolean isCasting() {
        return mIsCasting.get();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(Factory.get());
        Factory.get().onApplicationCreate(this);
        LoggerManager.setDebugLevel(BuildConfig.DEBUG ? Log.VERBOSE : Log.ERROR);
        LoggerManager.setTagPrefix(ScreencastApp.class.getSimpleName());

        try {
            LoggerManager.getLogger(ScreencastApp.class).info("checking assets");
            Collections.consumeRemaining(getAssets().list("test"), new Consumer<String>() {
                @Override
                public void accept(@NonNull String s) {
                    LoggerManager.getLogger(ScreencastApp.class).info("passets:" + s);
                }
            });
        } catch (IOException e) {
            LoggerManager.getLogger(ScreencastApp.class).error("passets err" + e.getLocalizedMessage());
        }
        // For AD end.

        ScreencastServiceProxy.watch(getApplicationContext(), new IScreencaster.ICastWatcher() {
            @Override
            public void onStartCasting() {
                LoggerManager.getLogger(DialogScreenCastActivity.class).debug("onStartCasting");
                mIsCasting.set(true);
            }

            @Override
            public void onStopCasting() {
                LoggerManager.getLogger(DialogScreenCastActivity.class).debug("onStopCasting");
                mIsCasting.set(false);
            }

            @Override
            public void onElapsedTimeChange(String formatedTime) {

            }
        });

        SharedExecutor.get().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FFmpeg.getInstance(getApplicationContext()).loadBinary(new FFmpegLoadBinaryResponseHandler() {
                        @Override
                        public void onFailure() {
                            LoggerManager.getLogger(ScreencastApp.class).debug("FFMpeg loading onFailure");
                        }

                        @Override
                        public void onSuccess() {
                            Factory.get().setFFMpegAvailable();
                            LoggerManager.getLogger(ScreencastApp.class).debug("FFMpeg loading onSuccess");
                        }

                        @Override
                        public void onStart() {
                            LoggerManager.getLogger(ScreencastApp.class).debug("FFMpeg loading onStart");
                        }

                        @Override
                        public void onFinish() {
                            LoggerManager.getLogger(ScreencastApp.class).debug("FFMpeg loading onFinish");
                        }
                    });
                } catch (FFmpegNotSupportedException e) {
                    LoggerManager.getLogger(ScreencastApp.class).error("Fail load FFMPEG:" + e.getLocalizedMessage());
                }
            }
        });
    }
}
