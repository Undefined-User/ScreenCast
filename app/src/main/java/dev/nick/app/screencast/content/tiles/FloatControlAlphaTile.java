package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;

import java.util.Observable;
import java.util.Observer;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import dev.nick.tiles.tile.TileListener;

class FloatControlAlphaTile extends QuickTile {

    FloatControlAlphaTile(@NonNull Context context, TileListener listener) {
        super(context, listener);
        this.titleRes = R.string.float_alpha;
        this.iconRes = R.drawable.ic_gradient_black_24dp;
        this.tileView = new QuickTileView(context, this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                showAlphaSeeker();
            }
        };
        this.summary = String.valueOf(100 * SettingsProvider.get().floatControlAlpha());
        SettingsProvider.get().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                if (String.valueOf(arg).contains("alpha")) {
                    getTileView().getSummaryTextView().setText(String.valueOf(100 * SettingsProvider.get().floatControlAlpha()));
                }
            }
        });
    }

    private void showAlphaSeeker() {
        final SeekBar seekBar = new SeekBar(getContext());
        float alpha = SettingsProvider.get().floatControlAlpha();
        seekBar.setMax(100);
        seekBar.setProgress((int) (alpha * 100));
        new AlertDialog.Builder(getContext())
                .setView(seekBar)
                .setTitle(R.string.float_alpha)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int current = seekBar.getProgress();
                        float cf = (float) current;
                        float newAlpha = cf / (float) 100;
                        SettingsProvider.get().setFloatControlAlpha(newAlpha);
                    }
                })
                .create()
                .show();
    }
}
