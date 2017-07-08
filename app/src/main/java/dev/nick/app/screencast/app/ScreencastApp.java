package dev.nick.app.screencast.app;

import android.app.Application;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import dev.nick.app.screencast.BuildConfig;
import dev.nick.app.screencast.tools.SharedExecutor;
import dev.nick.logger.LoggerManager;

public class ScreencastApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(Factory.get());
        Factory.get().onApplicationCreate(this);
        LoggerManager.setDebugLevel(BuildConfig.DEBUG ? Log.VERBOSE : Log.ERROR);
        LoggerManager.setTagPrefix(ScreencastApp.class.getSimpleName());

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


                            try {
                                FFmpeg.getInstance(getApplicationContext())
                                        .execute("-help".split(" "), new FFmpegExecuteResponseHandler() {
                                            @Override
                                            public void onSuccess(String message) {
                                                LoggerManager.getLogger(ScreencastApp.class).debug(message);
                                            }

                                            @Override
                                            public void onProgress(String message) {
                                                LoggerManager.getLogger(ScreencastApp.class).debug(message);
                                            }

                                            @Override
                                            public void onFailure(String message) {
                                                LoggerManager.getLogger(ScreencastApp.class).debug(message);
                                            }

                                            @Override
                                            public void onStart() {

                                            }

                                            @Override
                                            public void onFinish() {

                                            }
                                        });
                            } catch (FFmpegCommandAlreadyRunningException ignored) {

                            }
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
