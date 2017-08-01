package dev.nick.app.screencast.content;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import dev.nick.app.screencast.R;

/**
 * Created by Tornaco on 2017/7/27.
 * Licensed with Apache.
 */

public class ContainerHostActivity extends TransactionSafeActivity {

    public static final String EXTRA_FRAGMENT_CLZ = "extra.fr.clz";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_container_with_appbar_template);
        setupToolbar();
        showHomeAsUp();
        placeFragment(R.id.container, onCreateFragment(), null, false);
    }

    public static Intent getIntent(Context context, Class<? extends Fragment> clz) {
        Intent i = new Intent(context, ContainerHostActivity.class);
        i.putExtra(ContainerHostActivity.EXTRA_FRAGMENT_CLZ, clz.getName());
        return i;
    }

    Fragment onCreateFragment() {
        Intent intent = getIntent();
        String clz = intent.getStringExtra(EXTRA_FRAGMENT_CLZ);

        if (PayListBrowserFragment.class.getName().equals(clz)) {
            return new PayListBrowserFragment();
        }
        return null;
    }
}
