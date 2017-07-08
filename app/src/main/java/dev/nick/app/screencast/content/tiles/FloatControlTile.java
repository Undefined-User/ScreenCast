package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.control.FloatingControllerServiceProxy;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.SwitchTileView;
import dev.nick.tiles.tile.TileListener;

public class FloatControlTile extends QuickTile {

    public FloatControlTile(@NonNull final Context context, TileListener listener) {
        super(context, listener);

        this.titleRes = R.string.title_float_window;
        this.iconRes = R.drawable.ic_settings_white_24dp;

        this.tileView = new SwitchTileView(context) {
            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setChecked(SettingsProvider.get().showFloatControl());

                if (isChecked()) {
                    new FloatingControllerServiceProxy(context).start(context);
                } else {
                    new FloatingControllerServiceProxy(context).stop(context);
                }
            }

            @Override
            protected void onCheckChanged(boolean checked) {
                super.onCheckChanged(checked);
                SettingsProvider.get().setShowFloatControl(checked);

                if (checked) {
                    new FloatingControllerServiceProxy(context).start(context);
                } else {
                    new FloatingControllerServiceProxy(context).stop(context);
                }
            }
        };
    }
}
