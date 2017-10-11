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
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.camera.CameraPreviewServiceProxy;
import dev.nick.app.screencast.camera.ThreadUtil;
import dev.nick.app.screencast.cast.IScreencaster;
import dev.nick.app.screencast.cast.ScreencastServiceProxy;
import dev.nick.app.screencast.modle.Video;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.app.screencast.provider.VideoProvider;
import dev.nick.app.screencast.tools.MediaTools;
import dev.nick.app.screencast.widget.RecordingButton;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScreenCastActivity extends TransactionSafeActivity {

    private static final int PERMISSION_CODE = 1;
    private static final int PERMISSION_CODE_CREATE = 2;
    protected Logger mLogger;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private RecordingButton mFab;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private boolean mIsCasting;
    private int mRemainingSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mLogger = LoggerManager.getLogger(getClass());

        setContentView(getContentViewId());

        initUI();
    }

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.GET_TASKS,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE})
    void initService() {
        mProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        LoggerManager.getLogger(ScreenCastActivity.class).debug("initService::mProjectionManager:" + mProjectionManager);

        ScreencastServiceProxy.watch(getApplicationContext(), new IScreencaster.ICastWatcher() {
            @Override
            public void onStartCasting() {
                LoggerManager.getLogger(ScreenCastActivity.class).debug("onStartCasting");
                refreshState(true);
            }

            @Override
            public void onStopCasting() {
                refreshState(false);
                ThreadUtil.getMainThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ScreenCastActivityPermissionsDispatcher.readVideosWithCheck(ScreenCastActivity.this);
                    }
                }, 1000);// Waiting for the Scanner.
            }

            @Override
            public void onElapsedTimeChange(String formatedTime) {

            }
        });
    }

    protected void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(getResources().getIntArray(R.array.polluted_waves));

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mFab.show();
                } else {
                    mFab.hide();
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ScreenCastActivityPermissionsDispatcher.readVideosWithCheck(ScreenCastActivity.this);
            }
        });

        mFab = (RecordingButton) findViewById(R.id.image);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsCasting) {
                    stopRecording();
                    CameraPreviewServiceProxy.hide(getApplicationContext());
                } else {
                    ScreenCastActivityPermissionsDispatcher.startRecordingWithCheck(ScreenCastActivity.this);
                    if (SettingsProvider.get().withCamera()) {
                        ScreenCastActivityPermissionsDispatcher.showCameraPreviewWithCheck(ScreenCastActivity.this);
                    }
                }
            }
        });

        if (mRemainingSeconds != 0) mFab.hide();

        setupAdapter();
        if ((SettingsProvider.get().getAppVersionNum() < SettingsProvider.APP_VERSION_INT)) {
            showPermissionDialogAndGo();
            ScreenCastActivityPermissionsDispatcher.initServiceWithCheck(ScreenCastActivity.this);
        } else {
            ScreenCastActivityPermissionsDispatcher.readVideosWithCheck(ScreenCastActivity.this);
            ScreenCastActivityPermissionsDispatcher.initServiceWithCheck(ScreenCastActivity.this);
        }
    }

    protected void showCountdownIfNeeded(String content) {
        boolean showCD = SettingsProvider.get().showCD();
        if (showCD) {
            SuperToast.cancelAllSuperToasts();
            SuperToast.create(this, content, Style.DURATION_LONG, Style.red())
                    .setText(content)
                    .setFrame(Style.FRAME_KITKAT)
                    .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_RED))
                    .setAnimations(Style.ANIMATIONS_POP).show();
        }
    }

    protected int getContentViewId() {
        return R.layout.navigator_content;
    }

    public void showPermissionDialogAndGo() {
        new MaterialStyledDialog.Builder(this)
                .setIcon(R.drawable.ic_notifications_active_black_24dp)
                .setDescription(R.string.summary_perm_require)
                .setPositiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ScreenCastActivityPermissionsDispatcher.readVideosWithCheck(ScreenCastActivity.this);
                        ScreenCastActivityPermissionsDispatcher.initServiceWithCheck(ScreenCastActivity.this);
                    }
                })
                .setNegativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();
                    }
                }).setCancelable(false).build().show();

    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void readVideos() {
        swipeRefreshLayout.setRefreshing(true);
        ThreadUtil.newThread(new Runnable() {
            @Override
            public void run() {
                final List<Video> videos = new VideoProvider(getApplicationContext()).getList();

                ThreadUtil.getMainThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.update(videos);
                        updateHint();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).run();
    }

    void updateHint() {
        boolean hasVideo = mAdapter != null && mAdapter.getItemCount() > 0;
        if (!hasVideo) {
            TextView textView = (TextView) findViewById(R.id.hint_text);
            if (!mIsCasting) {
                textView.setText(R.string.start_description);
            } else {
                textView.setText(R.string.stop_description);
            }
        }
        findViewById(R.id.hint_area).setVisibility(hasVideo ? View.GONE : View.VISIBLE);
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
            mFab.hide();
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
                            mLogger.debug("Tick");
                            showCountdownIfNeeded(String.valueOf(mRemainingSeconds));
                            mRemainingSeconds--;
                        }
                    });
                }

                @Override
                public void onFinish() {
                    mRemainingSeconds = 0;
                    SuperToast.cancelAllSuperToasts();
                    mFab.show();
                }
            }.start();
        }

        if (SettingsProvider.get().hideAppWhenStart()) finish();
    }

    private void stopRecording() {
        ScreencastServiceProxy.stop(getApplicationContext());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ScreenCastActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isCasting) {
                    mFab.setImageResource(R.drawable.stop);
                    mFab.onRecording();
                } else {
                    mFab.setImageResource(R.drawable.record);
                    mFab.onStopRecording();
                }
                updateHint();
            }
        });
    }

    protected void setupAdapter() {
        mRecyclerView.setHasFixedSize(true);
        setupLayoutManager();
        mAdapter = new Adapter();
        mRecyclerView.setAdapter(mAdapter);

    }

    protected void setupLayoutManager() {
        mRecyclerView.setLayoutManager(getLayoutManager());
    }

    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
    }


    class TwoLinesViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView description;
        ImageView thumbnail;
        View actionBtn;

        public TwoLinesViewHolder(final View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(android.R.id.title);
            description = (TextView) itemView.findViewById(android.R.id.text1);
            actionBtn = itemView.findViewById(R.id.hint);
            thumbnail = (ImageView) itemView.findViewById(R.id.avatar);
        }
    }


    private class Adapter extends RecyclerView.Adapter<TwoLinesViewHolder> {

        private final List<Video> data;

        public Adapter(List<Video> data) {
            this.data = data;
        }

        public Adapter() {
            this(new ArrayList<Video>());
        }

        public void update(List<Video> data) {
            this.data.clear();
            this.data.addAll(data);
            notifyDataSetChanged();
        }

        public void remove(int position) {
            this.data.remove(position);
            notifyItemRemoved(position);
        }

        public void add(Video video, int position) {
            this.data.add(position, video);
            notifyItemInserted(position);
        }

        @Override
        public TwoLinesViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.simple_card_item, parent, false);
            return new TwoLinesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final TwoLinesViewHolder holder, int position) {
            final Video item = data.get(position);
            holder.title.setText(item.getTitle());
            String descriptionText = item.getDuration();
            holder.description.setText(descriptionText);
            holder.actionBtn.setVisibility(View.INVISIBLE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(ScreenCastActivity.this, holder.actionBtn);
                    popupMenu.inflate(R.menu.actions);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.action_play:
                                    startActivity(MediaTools.buildOpenIntent(ScreenCastActivity.this,
                                            new File(item.getPath())));
                                    break;
                                case R.id.action_remove:
                                    ThreadUtil.getWorkThreadHandler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            new File(item.getPath()).delete();
                                            remove(holder.getAdapterPosition());
                                        }
                                    });
                                    break;
                                case R.id.action_rename:
                                    ThreadUtil.getMainThreadHandler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showRenameDialog(item.getTitle(), item.getPath());
                                        }
                                    });
                                    break;
                                case R.id.action_share:
                                    startActivity(MediaTools.buildSharedIntent(ScreenCastActivity.this,
                                            new File(item.getPath())));
                                    break;
                                case R.id.action_togif:
                                    String path = item.getPath();
                                    toGif(path, new File(path).getParent() + File.separator + getNameWithoutExtension(path) + ".gif");
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
            Glide.with(getApplicationContext()).load(item.getPath()).into(holder.thumbnail);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }


        void showRenameDialog(String hint, final String fromPath) {
            View editTextContainer = LayoutInflater.from(ScreenCastActivity.this).inflate(dev.nick.tiles.R.layout.dialog_edit_text, null, false);
            final EditText editText = (EditText) editTextContainer.findViewById(dev.nick.tiles.R.id.edit_text);
            editText.setHint(hint);
            AlertDialog alertDialog = new AlertDialog.Builder(ScreenCastActivity.this)
                    .setView(editTextContainer)
                    .setTitle(R.string.action_rename)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ThreadUtil.getWorkThreadHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    File parent = new File(fromPath).getParentFile();
                                    File to = new File(parent, editText.getText().toString() + ".mp4");
                                    new File(fromPath).renameTo(to);
                                    MediaScannerConnection.scanFile(getApplicationContext(),
                                            new String[]{to.getAbsolutePath()}, null,
                                            new MediaScannerConnection.OnScanCompletedListener() {
                                                public void onScanCompleted(String path, Uri uri) {
                                                    LoggerManager.getLogger(getClass()).info("MediaScanner scanned recording " + path);
                                                    ScreenCastActivityPermissionsDispatcher.readVideosWithCheck(ScreenCastActivity.this);
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            alertDialog.show();
        }

        public String getNameWithoutExtension(String file) {
            String fileName = new File(file).getName();
            int dotIndex = fileName.lastIndexOf('.');
            return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
        }

        void toGif(String path, final String dest) {
            String command = String.format("-y -i %s -pix_fmt rgb24 -r 10 %s", path, dest);
            LoggerManager.getLogger(getClass()).debug("Command:" + command);
            String[] commands = command.split(" ");
            final ProgressDialog p = new ProgressDialog(ScreenCastActivity.this);
            p.setTitle(R.string.action_togif);
            p.setCancelable(false);
            p.setIndeterminate(true);

            try {
                FFmpeg.getInstance(getApplicationContext()).execute(commands, new FFmpegExecuteResponseHandler() {
                    @Override
                    public void onSuccess(String message) {
                        LoggerManager.getLogger(ScreenCastActivity.class).debug(message);
                        Snackbar.make(mRecyclerView, getString(R.string.result_to_gif_ok, dest),
                                Snackbar.LENGTH_INDEFINITE)
                                .setAction(android.R.string.ok, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                })
                                .show();
                    }

                    @Override
                    public void onProgress(String message) {
                        p.setMessage(message);
                        LoggerManager.getLogger(ScreenCastActivity.class).debug(message);
                    }

                    @Override
                    public void onFailure(final String message) {
                        LoggerManager.getLogger(ScreenCastActivity.class).debug(message);
                        Snackbar.make(mRecyclerView, getString(R.string.result_to_gif_fail), Snackbar.LENGTH_INDEFINITE)
                                .setAction(android.R.string.ok, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new AlertDialog.Builder(ScreenCastActivity.this)
                                                .setMessage(message)
                                                .setTitle(R.string.result_to_gif_fail)
                                                .setCancelable(false)
                                                .setPositiveButton(android.R.string.ok, null)
                                                .create()
                                                .show();
                                    }
                                })
                                .show();
                    }

                    @Override
                    public void onStart() {
                        p.show();
                    }

                    @Override
                    public void onFinish() {
                        p.dismiss();
                    }
                });
            } catch (FFmpegCommandAlreadyRunningException e) {
                Toast.makeText(ScreenCastActivity.this, "FFmpegCommandAlreadyRunningException", Toast.LENGTH_LONG).show();
            }
        }
    }

}
