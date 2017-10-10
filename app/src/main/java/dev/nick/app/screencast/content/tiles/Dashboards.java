package dev.nick.app.screencast.content.tiles;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.cast.CasterAudioSource;
import dev.nick.app.screencast.provider.SettingsProvider;
import dev.nick.logger.LoggerManager;
import dev.nick.tiles.tile.Category;
import dev.nick.tiles.tile.DashboardFragment;
import dev.nick.tiles.tile.QuickTile;
import dev.nick.tiles.tile.TileListener;

public class Dashboards extends DashboardFragment implements TileListener {

    private TextView mSummaryView;

    @Override
    protected void onCreateDashCategories(List<Category> categories) {
        super.onCreateDashCategories(categories);
        Category audio = new Category() {
            @Override
            public void onSummaryViewAttached(TextView view) {
                super.onSummaryViewAttached(view);
                mSummaryView = view;
            }

            @Override
            public void onNoRemindClick() {
                super.onNoRemindClick();
                SettingsProvider.get().setAudioSourceNoRemind(true);
            }
        };
        audio.titleRes = R.string.category_audio;
        audio.summaryRes = !SettingsProvider.get().audioSourceNoRemind()
                && (SettingsProvider.get().audioSource() == CasterAudioSource.DEFAULT
                || SettingsProvider.get().audioSource()
                == CasterAudioSource.R_SUBMIX) ? R.string.audio_xopsed_desc : 0;
        audio.addTile(new WithAudioTile(getContext(), this));
        audio.addTile(new AudioSourceTile(getContext(), this));

        Category camera = new Category();
        camera.titleRes = R.string.category_camera;
        camera.addTile(new WithCameraTile(getContext(), this));
        camera.addTile(new PreviewSizeDropdownTile(getContext(), this));
        camera.addTile(new SwitchCameraTile(getContext(), this));

        Category video = new Category();
        video.titleRes = R.string.category_video;
        video.addTile(new ResolutionsTile(getContext(), this));
        video.addTile(new OrientationTile(getContext(), this));
        video.addTile(new FrameRateTile(getContext()));

        Category access = new Category();
        access.titleRes = R.string.category_accessibility;
        access.addTile(new SoundEffectTile(getContext(), this));
        access.addTile(new ShakeTile(getContext(), this));
        access.addTile(new ShowTouchTile(getContext(), this));
        access.addTile(new DelayTile2(getContext(), this));
        access.addTile(new ShowCDTile(getContext(), this));
        access.addTile(new AutoHideTile(getContext(), this));
        access.addTile(new StopWhenScreenOffTile(getContext(), this));
        access.addTile(new StopOnVolumeTile(getContext(), this));

        Category floatControl = new Category();
        floatControl.titleRes = R.string.title_float_control_settings;
        floatControl.addTile(new FloatControlTile(getContext(), this));
        floatControl.addTile(new FloatControlThemeTile(getContext(), this));
        floatControl.addTile(new FloatControlAlphaTile(getContext(), this));

        Category storage = new Category();
        storage.titleRes = R.string.category_storage;
        storage.addTile(new StorageTile(getActivity(), this));

        Category others = new Category();
        others.titleRes = R.string.category_others;

        others.addTile(new DonateTile(getActivity()));
        others.addTile(new PayListTile(getActivity()));
        others.addTile(new GetProTile(getActivity()));
        others.addTile(new LicenseTile(getContext(), this));

        categories.add(audio);
        categories.add(video);
        categories.add(camera);
        categories.add(access);
        categories.add(floatControl);
        categories.add(storage);
        categories.add(others);
    }

    @Override
    public void onTileClick(@NonNull QuickTile tile) {
        LoggerManager.getLogger(getClass()).debug("OnTileClick:" + tile);
        // Nothing.
        if (!SettingsProvider.get().audioSourceNoRemind() && mSummaryView != null && tile instanceof AudioSourceTile) {
            mSummaryView.setText(R.string.audio_xopsed_desc);
            mSummaryView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}