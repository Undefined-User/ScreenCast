package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.DropDownTileView;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.TileListener;

class DelayTile2 extends QuickTile {

    private int[] mSources = null;

    DelayTile2(@NonNull Context context, final TileListener listener) {
        super(context, listener);

        this.iconRes = R.drawable.ic_access_time_black_24dp;

        this.mSources = getContext().getResources().getIntArray(R.array.start_delay);

        this.tileView = new DropDownTileView(context) {

            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                setSelectedItem(indexOfSource(SettingsProvider.get().startDelay()), false);
            }

            @Override
            protected List<String> onCreateDropDownList() {
                List<String> strings = new ArrayList<>(mSources.length);
                for (int s : mSources) {
                    strings.add(String.valueOf(s));
                }
                return strings;
            }

            @Override
            protected void onItemSelected(int position) {
                super.onItemSelected(position);
                SettingsProvider.get().setStartDelay(mSources[position]);
                getSummaryTextView().setText(String.valueOf(mSources[position]));
            }
        };
        this.titleRes = R.string.title_start_delay;
        this.summary = String.valueOf(SettingsProvider.get().startDelay());
    }

    private int indexOfSource(long value) {
        for (int i = 0; i < mSources.length; i++) {
            if (value == mSources[i]) return i;
        }
        return 0;
    }
}
