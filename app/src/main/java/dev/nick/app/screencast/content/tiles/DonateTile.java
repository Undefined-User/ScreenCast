package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.List;

import dev.nick.app.screencast.R;
import dev.nick.tiles.tile.DropDownTileView;
import dev.nick.tiles.tile.QuickTile;

/**
 * Created by Nick@NewStand.org on 2017/4/6 18:26
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

public class DonateTile extends QuickTile {

    private boolean fromUser;

    private String[] mSources = {"WeChat", "AliPay"};
    private String[] mSourcesCN = {"微信", "支付宝"};

    public DonateTile(@NonNull Context context) {
        super(context, null);

        this.titleRes = R.string.title_donate;
        this.summaryRes = R.string.summary_donate;
        this.iconRes = R.drawable.ic_monetization_on_black_24dp;

        this.tileView = new DropDownTileView(context) {

            @Override
            protected List<String> onCreateDropDownList() {
                return Arrays.asList(mSourcesCN);
            }

            @Override
            protected void onItemSelected(int position) {
                super.onItemSelected(position);
                if (!fromUser) {
                    fromUser = true;
                    return;
                }
                switch (position) {
                    case 0:
                        showPayQRCode(R.drawable.qr_wechat_pay);
                        break;
                    case 1:
                        showPayQRCode(R.drawable.qr_ali_pay);
                        break;
                }
            }
        };
    }

    private void showPayQRCode(int res) {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(res);
        new AlertDialog.Builder(getContext())
                .setView(imageView)
                .setTitle(R.string.title_donate)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Nothing.
                    }
                })
                .create()
                .show();
    }
}
