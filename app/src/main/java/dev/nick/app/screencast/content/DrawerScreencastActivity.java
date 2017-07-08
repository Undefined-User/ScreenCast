package dev.nick.app.screencast.content;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.nononsenseapps.filepicker.Utils;

import java.io.File;
import java.util.List;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.content.tiles.Dashboards;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.logger.LoggerManager;

public class DrawerScreencastActivity extends ScreenCastActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private boolean mGriding;

    @Override
    protected void initUI() {
        super.initUI();
        placeFragment(R.id.container, new Dashboards(), null, false);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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

    @SuppressWarnings("StatementWithEmptyBody")
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

}
