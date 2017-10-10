package dev.nick.app.screencast.content.tiles;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.ad.adView.BannerAd;
import com.xiaomi.ad.common.pojo.AdEvent;

import dev.nick.app.screencast.R;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.TileView;

/**
 * Created by Tornaco on 2017/7/28.
 * Licensed with Apache.
 */

public class AdTile extends QuickTile {

    public static final String TAG = "AD-AdTile";

    private BannerAd mBannerAd;

    private static final String BANNER_POS_ID = "20676c78b42d76cd9ee03f97818e7342";

    public AdTile(final Context context) {
        super(context);
        this.tileView = new TileView(context) {
            @Override
            protected void onViewInflated(View view) {
                ViewGroup container = (ViewGroup) view.findViewById(R.id.container);
                initAd(context, container);
            }

            @Override
            public void setDividerVisibility(boolean visible) {
                // Hooked.
            }

            @Override
            protected int getLayoutId() {
                return R.layout.banner_container;
            }
        };
    }

    private void initAd(Context context, ViewGroup container) {
        mBannerAd = new BannerAd(context, container, new BannerAd.BannerListener() {
            @Override
            public void onAdEvent(AdEvent adEvent) {
                if (adEvent.mType == AdEvent.TYPE_CLICK) {
                    Log.i(TAG, "ad has been clicked!");
                } else if (adEvent.mType == AdEvent.TYPE_SKIP) {
                    Log.i(TAG, "x button has been clicked!");
                } else if (adEvent.mType == AdEvent.TYPE_VIEW) {
                    Log.i(TAG, "ad has been showed!");
                }
            }
        });
        try {
            mBannerAd.show(BANNER_POS_ID);
        } catch (Exception ignored) {
        }
    }

    public void recycle() {
        mBannerAd.recycle();
    }
}
