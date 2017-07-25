package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import dev.nick.app.screencast.R;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import dev.nick.tiles.tile.TileListener;

class LicenseTile extends QuickTile {

    LicenseTile(@NonNull Context context, TileListener listener) {
        super(context, listener);
        this.titleRes = R.string.title_app_license;
        this.iconRes = R.drawable.ic_help_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                showInfo();
            }
        };
    }

    private void showInfo() {

    }
}
