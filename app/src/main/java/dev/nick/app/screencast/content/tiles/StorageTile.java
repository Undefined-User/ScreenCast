package dev.nick.app.screencast.content.tiles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.Observable;
import java.util.Observer;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.QuickTileView;
import dev.nick.tiles.tile.TileListener;

class StorageTile extends QuickTile {


    StorageTile(@NonNull final Context context, TileListener listener) {
        super(context, listener);
        this.titleRes = R.string.title_storage;
        this.summary = context.getString(R.string.summary_storage,
                SettingsProvider.get().storageRootPath());
        this.iconRes = R.drawable.ic_folder_open_black_24dp;
        this.tileView = new QuickTileView(context, this) {


            @Override
            protected void onBindActionView(RelativeLayout container) {
                super.onBindActionView(container);
                SettingsProvider.get().addObserver(new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        getSummaryTextView().setText(context.getString(R.string.summary_storage,
                                SettingsProvider.get().storageRootPath()));
                    }
                });
            }

            @Override
            public void onClick(View v) {
                super.onClick(v);
                pickSingleDir((Activity) context, SettingsProvider.REQUEST_CODE_FILE_PICKER);
            }
        };
    }

    public static void pickSingleDir(Activity activity, int code) {
        // This always works
        Intent i = new Intent(activity, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        activity.startActivityForResult(i, code);
    }
}
