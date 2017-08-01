package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.content.ContainerHostActivity;
import dev.nick.app.screencast.content.PayListBrowserFragment;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;

/**
 * Created by Tornaco on 2017/7/29.
 * Licensed with Apache.
 */

public class PayListTile extends QuickTile {

    public PayListTile(@NonNull final Context context) {
        super(context, null);

        this.titleRes = R.string.title_pay_list;
        this.iconRes = R.drawable.ic_playlist_add_check_black_24dp;

        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);

                context.startActivity(ContainerHostActivity.getIntent(context, PayListBrowserFragment.class));
            }
        };
    }
}
