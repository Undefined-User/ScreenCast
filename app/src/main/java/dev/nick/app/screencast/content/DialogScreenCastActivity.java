/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nick.app.screencast.content;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.app.ScreencastApp;
import dev.nick.app.screencast.camera.CameraPreviewServiceProxy;
import dev.nick.app.screencast.camera.ThreadUtil;
import dev.nick.app.screencast.cast.IScreencaster;
import dev.nick.app.screencast.cast.ScreencastServiceProxy;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.app.screencast.widget.OneSecondToast;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class DialogScreenCastActivity extends TransactionSafeActivity {

    private static final int PERMISSION_CODE = 1;
    private static final int PERMISSION_CODE_CREATE = 2;

    protected Logger mLogger;

    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;

    private boolean mIsCasting;
    private int mRemainingSeconds;

    private OneSecondToast oneSecondToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mLogger = LoggerManager.getLogger(getClass());

        setContentView(getContentViewId());

        initService();

        onFabClick();
    }

    private void initService() {
        mProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        ScreencastServiceProxy.watch(getApplicationContext(), new IScreencaster.ICastWatcher() {
            @Override
            public void onStartCasting() {
                LoggerManager.getLogger(DialogScreenCastActivity.class).debug("onStartCasting");
                refreshState(true);
            }

            @Override
            public void onStopCasting() {
                LoggerManager.getLogger(DialogScreenCastActivity.class).debug("onStopCasting");
                refreshState(false);
            }

            @Override
            public void onElapsedTimeChange(String formatedTime) {

            }
        });

        mIsCasting = ((ScreencastApp) getApplication()).isCasting();
        oneSecondToast = new OneSecondToast();
    }

    private void onFabClick() {
        if (mIsCasting) {
            stopRecording();
            CameraPreviewServiceProxy.hide(getApplicationContext());
            finish();
        } else {
            // Delay to wait for the UI.
            ThreadUtil.getMainThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    DialogScreenCastActivityPermissionsDispatcher.startRecordingWithCheck(DialogScreenCastActivity.this);
                    if (SettingsProvider.get().withCamera()) {
                        DialogScreenCastActivityPermissionsDispatcher.showCameraPreviewWithCheck(DialogScreenCastActivity.this);
                    }
                }
            }, 1000);
        }
    }

    protected void showCountdownIfNeeded(String content) {
        boolean showCD = SettingsProvider.get().showCD();
        if (showCD) {
            oneSecondToast.show(this, content);
        }
    }

    protected int getContentViewId() {
        return R.layout.dialog_cast;
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void showCameraPreview() {
        CameraPreviewServiceProxy.show(getApplicationContext(), SettingsProvider.get().previewSize());
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void startRecording() {
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(),
                    PERMISSION_CODE);
            return;
        }

        ScreencastServiceProxy.start(getApplicationContext(), mMediaProjection, SettingsProvider.get().withAudio());

        if (SettingsProvider.get().startDelay() > 0) {
            long delay = SettingsProvider.get().startDelay();
            mRemainingSeconds = (int) (delay / 1000);
            showCountdownIfNeeded(String.valueOf(mRemainingSeconds));
            mRemainingSeconds--;
            mLogger.debug(mRemainingSeconds);
            new CountDownTimer(delay, 1000) {

                @Override
                public void onTick(long l) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLogger.debug("Tick:" + mRemainingSeconds);
                            showCountdownIfNeeded(String.valueOf(mRemainingSeconds == 0 ? "GO" : mRemainingSeconds));
                            mRemainingSeconds--;
                        }
                    });
                }

                @Override
                public void onFinish() {
                    mRemainingSeconds = 0;
                }
            }.start();
        }

        finish();
    }

    private void stopRecording() {
        ScreencastServiceProxy.stop(getApplicationContext());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DialogScreenCastActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PERMISSION_CODE && requestCode != PERMISSION_CODE_CREATE) {
            return;
        }
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == PERMISSION_CODE) {
            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            mMediaProjection.unregisterCallback(projectionCallback);
            mMediaProjection.registerCallback(projectionCallback, null);
            ScreencastServiceProxy.setProjection(this, mMediaProjection);

            startRecording();
        } else {
            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            mMediaProjection.unregisterCallback(projectionCallback);
            mMediaProjection.registerCallback(projectionCallback, null);

            ScreencastServiceProxy.setProjection(this, mMediaProjection);
        }
    }

    private MediaProjection.Callback projectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            super.onStop();
            onProjectionStop();
        }
    };

    private void onProjectionStop() {
        mMediaProjection = null;
        ScreencastServiceProxy.stop(getApplicationContext());
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onNoStoragePermission() {
        finish();
    }

    @OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
    void onNoAudioPermission() {
        SettingsProvider.get().setWithAudio(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshState(final boolean isCasting) {
        mIsCasting = isCasting;
    }

}
