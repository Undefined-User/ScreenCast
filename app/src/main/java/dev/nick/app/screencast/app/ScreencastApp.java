package dev.nick.app.screencast.app;

import android.app.Application;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.util.concurrent.atomic.AtomicBoolean;

import dev.nick.app.screencast.BuildConfig;
import dev.nick.app.screencast.cast.IScreencaster;
import dev.nick.app.screencast.cast.ScreencastServiceProxy;
import dev.nick.app.screencast.content.DialogScreenCastActivity;
import dev.nick.app.screencast.tools.SharedExecutor;
import dev.nick.logger.LoggerManager;

public class ScreencastApp extends Application {

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
