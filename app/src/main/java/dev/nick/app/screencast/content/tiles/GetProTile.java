package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;

import dev.nick.app.screencast.R;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;

/**
 * Created by Tornaco on 2017/7/29.
 * Licensed with Apache.
 */

public class GetProTile extends QuickTile {

    public GetProTile(@NonNull final Context context) {
        super(context, null);

        this.titleRes = R.string.title_get_pro;
        this.summaryRes = R.string.summary_get_pro;
        this.iconRes = R.drawable.ic_get_app_black_24dp;

        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);

                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri CONTENT_URI_BROWSERS = Uri.parse("https://coolapk.com/apk/dev.tornaco.torscreenrec");
                intent.setData(CONTENT_URI_BROWSERS);
                context.startActivity(intent);
            }
        };
    }
}
