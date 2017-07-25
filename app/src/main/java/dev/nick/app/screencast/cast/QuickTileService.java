package dev.nick.app.screencast.cast;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

import dev.nick.app.screencast.content.DialogScreenCastActivity;

/**
 * Created by Tornaco on 2017/7/17.
 * Licensed with Apache.
 */
@TargetApi(Build.VERSION_CODES.N)
public class QuickTileService extends TileService {

    @Override
    public void onClick() {
        super.onClick();
        Intent intent = new Intent(this, DialogScreenCastActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
