package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;

/**
 * Created by Nick on 2017/6/22 12:38
 */

public class FFMPEGTile extends QuickTile {

    public FFMPEGTile(@NonNull Context context) {
        super(context, null);

        this.iconRes = R.drawable.ic_stat_device_access_video;
        this.tileView = new SwitchTileView(context) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().hideAppWhenStart());
            }

            @Override
            protected void onCheckChanged(final boolean checked) {
                super.onCheckChanged(checked);
                SettingsProvider.get().setHideAppWhenStart(checked);
            }
        };
        this.titleRes = R.string.title_auto_hide;
        this.summaryRes = R.string.summary_auto_hide;
    }
}
