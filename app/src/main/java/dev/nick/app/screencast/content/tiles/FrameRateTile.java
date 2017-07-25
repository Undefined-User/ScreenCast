package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.EditTextTileView;
import dev.nick.tiles.tile.QuickTile;

/**
 * Created by Tornaco on 2017/7/21.
 * Licensed with Apache.
 */

public class FrameRateTile extends QuickTile {
    public FrameRateTile(@NonNull final Context context) {
        super(context, null);

        this.titleRes = R.string.title_frame_rate;
        this.iconRes = R.drawable.ic_filter_frames_black_24dp;
        this.summary = String.valueOf(SettingsProvider.get().getFrameRate());

        this.tileView = new EditTextTileView(context) {
            @Override
            protected int getInputType() {
                return InputType.TYPE_CLASS_NUMBER;
            }

            @Override
            protected CharSequence getHint() {
                return String.valueOf(SettingsProvider.get().getFrameRate());
            }

            @Override
            protected CharSequence getDialogTitle() {
                return context.getString(R.string.title_frame_rate);
            }

            @Override
            protected void onPositiveButtonClick() {
                super.onPositiveButtonClick();
                String text = getEditText().getText().toString().trim();
                try {
                    int rate = Integer.parseInt(text);

                    if (rate > 99) {
                        Toast.makeText(context, "<=99 ~.~", Toast.LENGTH_LONG).show();
                        return;
                    }

                    SettingsProvider.get().setFrameRate(rate);
                } catch (Throwable e) {
                    Toast.makeText(context, Log.getStackTraceString(e), Toast.LENGTH_LONG).show();
                }

                getSummaryTextView().setText(String.valueOf(SettingsProvider.get().getFrameRate()));
            }
        };
    }
}
