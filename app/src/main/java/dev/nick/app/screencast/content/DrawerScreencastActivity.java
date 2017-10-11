package dev.nick.app.screencast.content;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.nononsenseapps.filepicker.Utils;
import com.xiaomi.ad.AdListener;
import com.xiaomi.ad.NativeAdInfoIndex;
import com.xiaomi.ad.NativeAdListener;
import com.xiaomi.ad.adView.BannerAd;
import com.xiaomi.ad.adView.CustomNewsFeedAd;
import com.xiaomi.ad.common.pojo.AdError;
import com.xiaomi.ad.common.pojo.AdEvent;
import com.xiaomi.ad.internal.CustomNewsFeedJson;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dev.nick.app.screencast.BuildConfig;
import dev.nick.app.screencast.R;
import dev.nick.app.screencast.app.ScreencastApp;
import dev.nick.app.screencast.content.tiles.Dashboards;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.logger.LoggerManager;

public class DrawerScreencastActivity extends ScreenCastActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private boolean mGriding;

    private BannerAd mBannerAd;

    public static final String TAG = "AD-DrawerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initUI() {
        super.initUI();
        placeFragment(R.id.container, new Dashboards(), null, false);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (SettingsProvider.get().showAD() && BuildConfig.DEBUG || !SettingsProvider.get().clickAD()) {
            initAd(this, (ViewGroup) findViewById(R.id.adview_container));
        }
    }

    private void initAd(Context context, final ViewGroup container) {
//        mBannerAd = new BannerAd(context, container, new BannerAd.BannerListener() {
//            @Override
//            public void onAdEvent(AdEvent adEvent) {
//                if (adEvent.mType == AdEvent.TYPE_CLICK) {
//                    Log.i(TAG, "ad has been clicked!");
//                    SettingsProvider.get().setClickAD();
//                } else if (adEvent.mType == AdEvent.TYPE_SKIP) {
//                    Log.i(TAG, "x button has been clicked!");
//                } else if (adEvent.mType == AdEvent.TYPE_VIEW) {
//                    Log.i(TAG, "ad has been showed!");
//                }
//            }
//        });
//        try {
//            mBannerAd.show(ScreencastApp.AD_BANNER);
//        } catch (Exception ignored) {
//        }

        final CustomNewsFeedAd customNewsFeedAd = new CustomNewsFeedAd(this);
        try {
            customNewsFeedAd.requestAd(ScreencastApp.AD_FEED_SMALL, 1, new NativeAdListener() {
                @Override
                public void onNativeInfoFail(AdError adError) {
                    Log.e(TAG, "onNativeInfoFail e : " + adError);
                }

                @Override
                public void onNativeInfoSuccess(List<NativeAdInfoIndex> list) {
                    final NativeAdInfoIndex adInfoResponse = list.get(0);
                    customNewsFeedAd.buildViewAsync(adInfoResponse, requestSmallPicAd(true), new AdListener() {
                        @Override
                        public void onAdError(AdError adError) {
                            Log.e(TAG, "error : remove all views");
                            container.removeAllViews();
                        }

                        @Override
                        public void onAdEvent(AdEvent adEvent) {
                            // 目前考虑了下述情况：
                            // 1.用户点击信息流广告（整个的范围内）
                            // 2.用户点击dislike按钮(目前dislike按钮暂时不启用)
                            // 3.信息流广告展示
                            // 4.下载类广告中的下载按钮被点击
                            if (adEvent.mType == AdEvent.TYPE_CLICK) {
                                Log.i(TAG, "ad has been clicked!");
                                SettingsProvider.get().setClickAD();
                            } else if (adEvent.mType == AdEvent.TYPE_SKIP) {
                                Log.i(TAG, "x button has been clicked!");
                            } else if (adEvent.mType == AdEvent.TYPE_VIEW) {
                                Log.i(TAG, "ad has been showed!");
                            } else if (adEvent.mType == AdEvent.TYPE_APP_START_DOWNLOAD) {
                                Log.i(TAG, "install button has been clicked");
                                SettingsProvider.get().setClickAD();
                            }
                        }

                        @Override
                        public void onAdLoaded() {

                        }

                        @Override
                        public void onViewCreated(View view) {
                            Log.e(TAG, "onViewCreated " + view);
                            container.removeAllViews();
                            container.addView(view);
                        }
                    });
                }
            });
        } catch (Throwable ignored) {

        }

    }

    private JSONObject requestSmallPicAd(boolean isInstallApp) {
        try {
            //这个例子中我们使用2套布局来分别处理下载类样式和默认样式
            CustomNewsFeedJson.Builder builder = new CustomNewsFeedJson.Builder(ScreencastApp.AD_FEED_TYPE_SMALL_PIC);
            ArrayList<Integer> idList = new ArrayList<Integer>();
            CustomNewsFeedJson customNewsFeedJson = null;
            if (isInstallApp) {
                idList.add(R.id.icon);
                customNewsFeedJson = builder.setTitleId(R.id.title).setPopularizeId(R.id.popularize)
                        .setLayoutId(R.layout.custom_sample_small_app_ad_layout).setIsInstallApp(isInstallApp)
                        .setSumaryId(R.id.summary).setSmallImageIds(idList).setInstallId(R.id.install).build();
            } else {
                idList.add(R.id.small_pic);
                customNewsFeedJson = builder.setTitleId(R.id.title).setPopularizeId(R.id.popularize)
                        .setLayoutId(R.layout.custom_sample_small_ad_layout).setIsInstallApp(isInstallApp)
                        .setSumaryId(R.id.summary).setSmallImageIds(idList).build();
            }
            return customNewsFeedJson.toJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected int getContentViewId() {
        return R.layout.activity_drawer_screencast;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mGriding) {
            getMenuInflater().inflate(R.menu.layouts_linear, menu);
        } else {
            getMenuInflater().inflate(R.menu.layouts_grid, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_switch_layout_to_linear: //fall
            case R.id.action_switch_layout_to_grid:
                boolean useGrid = SettingsProvider.get().gridLayout();
                SettingsProvider.get().setGridLayout(!useGrid);
                setupLayoutManager();
                invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        mGriding = SettingsProvider.get().gridLayout();
        if (mGriding)
            return new GridLayoutManager(getApplicationContext(), 2, LinearLayoutManager.VERTICAL, false);
        return new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SettingsProvider.REQUEST_CODE_FILE_PICKER && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            File file = Utils.getFileForUri(files.get(0));
            // Do something with the result...
            onStorageDirPick(file);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onStorageDirPick(File dir) {
        LoggerManager.getLogger(getClass())
                .debug("onStorageDirPick:" + dir);
        SettingsProvider.get().setStorageRootPath(dir.getPath());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // mBannerAd.recycle();
    }
}
