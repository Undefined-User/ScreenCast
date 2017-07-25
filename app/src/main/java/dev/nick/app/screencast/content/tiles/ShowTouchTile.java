package dev.nick.app.screencast.content.tiles;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import com.chrisplus.rootmanager.RootManager;
import com.chrisplus.rootmanager.container.Result;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.camera.ThreadUtil;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.app.screencast.tools.SharedExecutor;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import dev.nick.tiles.tile.TileListener;

public class ShowTouchTile extends QuickTile {

    public ShowTouchTile(@NonNull Context context, TileListener listener) {
        super(context, listener);
        this.iconRes = R.drawable.ic_touch_app_black_24dp;
        this.summaryRes = R.string.warn_only_root;
        this.tileView = new SwitchTileView(context) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().showTouch());
            }

            @Override
            protected void onCheckChanged(final boolean checked) {
                super.onCheckChanged(checked);
                final String command = String.format("settings put %s %s %s",
                        "system", "show_touches", checked ? "1" : "0");
                SharedExecutor.get().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (RootManager.getInstance().obtainPermission()) {
                            Result result = RootManager.getInstance().runCommand(command);
                            if (result.getResult()) {
                                return;
                            }
                        }
                        ThreadUtil.getMainThreadHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                goToSettings();
                            }
                        });
                    }
                });
            }
        };
        this.titleRes = R.string.title_show_touch;
    }

    private void goToSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            getContext().startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
            // Nothing.
        }
    }
}
