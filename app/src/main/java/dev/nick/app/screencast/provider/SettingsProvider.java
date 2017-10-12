package dev.nick.app.screencast.provider;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;

import java.io.File;
import java.util.Observable;

import dev.nick.app.screencast.BuildConfig;
import dev.nick.app.screencast.app.Factory;
import dev.nick.app.screencast.camera.PreviewSize;
import dev.nick.app.screencast.cast.CasterAudioSource;
import dev.nick.app.screencast.cast.Orientations;
import dev.nick.app.screencast.cast.ValidResolutions;
import dev.nick.logger.LoggerManager;

public abstract class SettingsProvider extends Observable {

    public static final int APP_VERSION_INT = BuildConfig.VERSION_CODE;
    public static final long START_DELAY_DEFAULT = 5000;

    public static final int REQUEST_CODE_FILE_PICKER = 0x102;

    private static final String STORAGE_MP4_FOLDER_NAME = "ScreenRecorder";
    private static final String STORAGE_GIF_FOLDER_NAME = "ScreenRecorder/gif";

    private static Impl sImpl;

    public static synchronized SettingsProvider get() {
        if (sImpl == null) sImpl = new Impl();
        return sImpl;
    }

    public abstract String storageRootPath();

    public abstract void setStorageRootPath(String path);

    public abstract boolean withAudio();

    public abstract void setWithAudio(boolean value);

    public abstract boolean withCamera();

    public abstract void setWithCamera(boolean value);

    public abstract int previewSize();

    public abstract void setPreviewSize(int size);

    public abstract int audioSource();

    public abstract boolean setAudioSource(int source);

    public abstract boolean audioSourceNoRemind();

    public abstract void setAudioSourceNoRemind(boolean value);

    public abstract int resolutionIndex();

    public abstract void setResolutionIndex(int index);

    public abstract float resolutionScaleFactor();

    public abstract void setResolutionScaleFactor(float factor);

    public abstract int orientation();

    public abstract void setOrientation(int orientation);

    public abstract boolean setShowTouch(boolean show);

    public abstract boolean showTouch();

    public abstract int preferredCamera();

    public abstract void setPreferredCamera(int index);

    public abstract boolean hideAppWhenStart();

    public abstract void setHideAppWhenStart(boolean hide);

    public abstract long startDelay();

    public abstract void setStartDelay(long mills);

    public abstract boolean showCD();

    public abstract void setShowCD(boolean show);

    public abstract boolean showAD();

    public abstract void setShowAD(boolean show);

    public abstract boolean clickAD();

    public abstract void setClickAD();

    public abstract boolean firstStart();

    public abstract void setFirstStart(boolean first);

    public abstract boolean soundEffect();

    public abstract void setSoundEffect(boolean use);

    public abstract boolean shakeAction();

    public abstract void setShakeAction(boolean use);

    public abstract boolean gridLayout();

    public abstract void setGridLayout(boolean use);

    public abstract int getAppVersionNum();

    public abstract boolean showFloatControl();

    public abstract void setShowFloatControl(boolean show);

    public abstract FloatControlTheme floatControlTheme();

    public abstract void setFloatControlTheme(FloatControlTheme theme);

    public abstract float floatControlAlpha();

    public abstract void setFloatControlAlpha(float alpha);

    public abstract int getFrameRate();

    public abstract void setFrameRate(int rate);

    public abstract boolean stopWhenScreenOff();

    public abstract void setStopWhenScreenOff(boolean stopWhenScreenOff);

    public abstract boolean stopOnVolume();

    public abstract void setStopOnVolume(boolean stopWhenScreenOff);

    private static class Impl extends SettingsProvider {
        // Keys in Settings.
        private static final String KEY_FIRST_START = "settings.first.start";
        private static final String KEY_WITH_AUDIO = "settings.with.audio";
        private static final String KEY_WITH_CAMERA = "settings.with.camera";
        private static final String KEY_PREVIEW_SIZE = "settings.preview.size";
        private static final String KEY_GRID_LAYOUT = "settings.view.grid";
        private static final String KEY_AUDIO_SOURCE = "settings.audio.source";
        private static final String KEY_AUDIO_SOURCE_NO_REMIND = "settings.audio.source.no.remind";
        private static final String KEY_RES_FACTOR = "settings.res.factor";
        private static final String KEY_RES_INDEX = "settings.res.index";
        private static final String KEY_ORIENTATION = "settings.orientation";
        private static final String KEY_PREFERRED_CAM = "settings.preferred.cam";
        private static final String KEY_HIDE_AUTO = "settings.hide.app.auto";
        private static final String KEY_START_DELAY = "settings.start.delay";
        private static final String KEY_SHOW_COUNTDOWN = "settings.show.countdown";
        private static final String KEY_SHOW_AD = "settings.show.ad.new";
        private static final String KEY_CLICK_AD = "settings.click.ad.new";
        private static final String KEY_SOUND_EFFECT = "settings.sound.effect";
        private static final String KEY_SHAKE_ACTION = "settings.shake.action";
        private static final String KEY_APP_VERSION = "settings.app.code";
        private static final String SHOW_TOUCHES = "show_touches";
        private static final String SHOW_FLOAT_CONTROL = "show_float_ctl";
        private static final String SHOW_ROOT_PATH = "root_path";
        private static final String KEY_FRAME_RATE = "frame_rate";
        private static final String KEY_FC_ALPHA = "fc_alpha";
        private static final String KEY_FC_THEME = "fc_theme";
        private static final String KEY_STOP_WHEN_SCREEN_OFF = "key_stop_when_screen_off";
        private static final String KEY_STOP_ON_VOLUME = "key_stop_on_volume";

        @Override
        public String storageRootPath() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getString(SHOW_ROOT_PATH, Environment.getExternalStorageDirectory().getPath()
                            + File.separator + STORAGE_MP4_FOLDER_NAME);
        }

        @Override
        public void setStorageRootPath(String path) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putString(SHOW_ROOT_PATH, path).apply();
            setChanged();
            notifyObservers();
        }

        @Override
        public boolean withAudio() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_WITH_AUDIO, false);
        }

        @Override
        public void setWithAudio(boolean value) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_WITH_AUDIO, value).apply();
        }

        @Override
        public boolean withCamera() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_WITH_CAMERA, false);
        }

        @Override
        public void setWithCamera(boolean value) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_WITH_CAMERA, value).apply();
        }

        @Override
        public int previewSize() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getInt(KEY_PREVIEW_SIZE, PreviewSize.SMALL);
        }

        @Override
        public void setPreviewSize(int size) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putInt(KEY_PREVIEW_SIZE, size).apply();
        }

        @Override
        public int audioSource() {

            if (!hasXposed()) {
                LoggerManager.getLogger(getClass()).error("No Xposed installed, r_submix is not available");
            }

            int s = PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getInt(KEY_AUDIO_SOURCE, CasterAudioSource.MIC);
            LoggerManager.getLogger(getClass()).verbose("Returning source:" + s);
            return s;
        }

        private boolean hasXposed() {
            String packageName = "de.robv.android.xposed.installer";
            try {
                Factory.get().getApplicationContext().getPackageManager().getApplicationInfo(
                        packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
                LoggerManager.getLogger(getClass()).debug("Xpoded installer deteced!");
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }

        @Override
        public boolean setAudioSource(int source) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putInt(KEY_AUDIO_SOURCE, source).apply();
            return true;
        }

        @Override
        public boolean audioSourceNoRemind() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_AUDIO_SOURCE_NO_REMIND, false);
        }

        @Override
        public void setAudioSourceNoRemind(boolean value) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_AUDIO_SOURCE_NO_REMIND, value).apply();
        }

        @Override
        public int resolutionIndex() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getInt(KEY_RES_INDEX, ValidResolutions.INDEX_MASK_AUTO);
        }

        @Override
        public void setResolutionIndex(int index) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putInt(KEY_RES_INDEX, index).apply();
        }

        @Override
        public float resolutionScaleFactor() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getFloat(KEY_RES_FACTOR, 0.5f);
        }

        @Override
        public void setResolutionScaleFactor(float factor) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putFloat(KEY_RES_FACTOR, factor).apply();
        }

        @Override
        public int orientation() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getInt(KEY_ORIENTATION, Orientations.AUTO);
        }

        @Override
        public void setOrientation(int orientation) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putInt(KEY_ORIENTATION, orientation).apply();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.M)
        public boolean setShowTouch(boolean show) {
            return false;
        }

        @Override
        public boolean showTouch() {
            try {
                return Settings.System.getInt(Factory.get().getApplicationContext().getContentResolver(), SHOW_TOUCHES) == 1;
            } catch (Settings.SettingNotFoundException | SecurityException ignored) {
            }
            return false;
        }

        @Override
        public int preferredCamera() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getInt(KEY_PREFERRED_CAM, Camera.CameraInfo.CAMERA_FACING_FRONT);
        }

        @Override
        public void setPreferredCamera(int index) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putInt(KEY_PREFERRED_CAM, index).apply();
        }

        @Override
        public boolean hideAppWhenStart() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_HIDE_AUTO, false);
        }

        @Override
        public void setHideAppWhenStart(boolean hide) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_HIDE_AUTO, hide).apply();
        }

        @Override
        public long startDelay() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getLong(KEY_START_DELAY, 0);
        }

        @Override
        public void setStartDelay(long mills) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putLong(KEY_START_DELAY, mills).apply();
        }

        @Override
        public boolean showCD() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_SHOW_COUNTDOWN, true);
        }

        @Override
        public void setShowCD(boolean show) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_SHOW_COUNTDOWN, show).apply();
        }

        @Override
        public boolean showAD() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_SHOW_AD, true);
        }

        @Override
        public void setShowAD(boolean show) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_SHOW_AD, show).apply();
        }

        @Override
        public boolean clickAD() {
            return System.currentTimeMillis()
                    - PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getLong(KEY_CLICK_AD, 0L) <= 60 * 60 * 1000 * 24;
        }

        @Override
        public void setClickAD() {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putLong(KEY_CLICK_AD, System.currentTimeMillis()).apply();
        }

        @Override
        public boolean firstStart() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_FIRST_START, true);
        }

        @Override
        public void setFirstStart(boolean first) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_FIRST_START, first).apply();
        }

        @Override
        public boolean soundEffect() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_SOUND_EFFECT, true);
        }

        @Override
        public void setSoundEffect(boolean use) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_SOUND_EFFECT, use).apply();
        }

        @Override
        public boolean shakeAction() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_SHAKE_ACTION, true);
        }

        @Override
        public void setShakeAction(boolean use) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_SHAKE_ACTION, use).apply();
        }

        @Override
        public boolean gridLayout() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_GRID_LAYOUT, false);
        }

        @Override
        public void setGridLayout(boolean use) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_GRID_LAYOUT, use).apply();
        }

        @Override
        public int getAppVersionNum() {
            int code = PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getInt(KEY_APP_VERSION, -1);
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putInt(KEY_APP_VERSION, APP_VERSION_INT).apply();
            return code;
        }

        @Override
        public boolean showFloatControl() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(SHOW_FLOAT_CONTROL, false);
        }

        @Override
        public void setShowFloatControl(boolean show) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(SHOW_FLOAT_CONTROL, show).apply();
            setChanged();
            notifyObservers();
        }

        @Override
        public FloatControlTheme floatControlTheme() {
            return FloatControlTheme.valueOf(PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getString(KEY_FC_THEME, FloatControlTheme.Default.name()));
        }

        @Override
        public void setFloatControlTheme(FloatControlTheme theme) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putString(KEY_FC_THEME, theme.name()).apply();
        }

        @Override
        public float floatControlAlpha() {
            return (PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getFloat(KEY_FC_ALPHA, 0.5f));
        }

        @Override
        public void setFloatControlAlpha(float alpha) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putFloat(KEY_FC_ALPHA, alpha).apply();
            setChanged();
            notifyObservers("float_alpha");
        }

        @Override
        public int getFrameRate() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getInt(KEY_FRAME_RATE, 30);
        }

        @Override
        public void setFrameRate(int rate) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putInt(KEY_FRAME_RATE, rate).apply();
        }

        @Override
        public boolean stopWhenScreenOff() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_STOP_WHEN_SCREEN_OFF, false);
        }

        @Override
        public void setStopWhenScreenOff(boolean stopWhenScreenOff) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_STOP_WHEN_SCREEN_OFF, stopWhenScreenOff).apply();
        }

        @Override
        public boolean stopOnVolume() {
            return PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .getBoolean(KEY_STOP_ON_VOLUME, true);
        }

        @Override
        public void setStopOnVolume(boolean stopOnVolume) {
            PreferenceManager.getDefaultSharedPreferences(Factory.get().getApplicationContext())
                    .edit().putBoolean(KEY_STOP_ON_VOLUME, stopOnVolume).apply();
        }
    }
}
