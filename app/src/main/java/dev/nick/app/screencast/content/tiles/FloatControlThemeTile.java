package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import java.util.Arrays;
import java.util.List;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.provider.FloatControlTheme;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.DropDownTileView;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.TileListener;

class FloatControlThemeTile extends QuickTile {

    private String[] mSources = null;

    FloatControlThemeTile(@NonNull Context context, final TileListener listener) {
        super(context, listener);

        this.iconRes = R.drawable.ic_color_lens_black_24dp;

        this.mSources = new String[FloatControlTheme.values().length];
        for (int i = 0; i < mSources.length; i++) {
            mSources[i] = context.getString(FloatControlTheme.values()[i].getStringRes());
        }

        this.tileView = new DropDownTileView(context) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setSelectedItem(SettingsProvider.get().floatControlTheme().ordinal(), false);
            }

            @Override
            protected List<String> onCreateDropDownList() {
                return Arrays.asList(mSources);
            }

            @Override
            protected void onItemSelected(int position) {
                super.onItemSelected(position);
                SettingsProvider.get().setFloatControlTheme(FloatControlTheme.from(position));
                getSummaryTextView().setText(mSources[SettingsProvider.get().floatControlTheme().ordinal()]);
            }
        };
        this.titleRes = R.string.float_theme;
        this.summary = mSources[SettingsProvider.get().floatControlTheme().ordinal()];
    }
}
