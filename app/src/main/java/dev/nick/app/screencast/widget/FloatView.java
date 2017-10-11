package dev.nick.app.screencast.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import dev.nick.app.screencast.R;
import dev.nick.app.screencast.camera.ThreadUtil;
import dev.nick.app.screencast.cast.IScreencaster;
import dev.nick.app.screencast.cast.ScreencastServiceProxy;
import dev.nick.app.screencast.content.DialogScreenCastActivity;
import dev.nick.app.screencast.provider.SettingsProvider;

public class FloatView extends FrameLayout {

    private Rect mRect = new Rect();
    private WindowManager mWm;
    private WindowManager.LayoutParams mLp = new WindowManager.LayoutParams();

    private TextView mTextView;
    private View mContainerView;
    private ImageView mImageView;

    int mTouchSlop;
    float density = getResources().getDisplayMetrics().density;

    private IScreencaster.ICastWatcher watcher = new IScreencaster.ICastWatcher() {
        @Override
        public void onStartCasting() {
            //setVisibility(VISIBLE);
            mImageView.setImageResource(R.drawable.ic_stop);
        }

        @Override
        public void onStopCasting() {
            //setVisibility(GONE);
            mImageView.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            mTextView.setText(R.string.elapse_time_zero);
        }

        @Override
        public void onElapsedTimeChange(String formatedTime) {
            mTextView.setText(formatedTime);
        }
    };

    private Observer observer = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            if (String.valueOf(arg).contains("alpha")) {
                ThreadUtil.getMainThreadHandler()
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                mContainerView.setAlpha(SettingsProvider.get().floatControlAlpha());
                            }
                        });
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (SettingsProvider.get().stopOnVolume()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public FloatView(final Context context) {
        super(context);

        View rootView = LayoutInflater.from(context).inflate(getLayoutId(), this);
        mContainerView = rootView.findViewById(R.id.container);
        mContainerView.setAlpha(SettingsProvider.get().floatControlAlpha());

        mTextView = (TextView) rootView.findViewById(R.id.text);

        OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // ScreencastServiceProxy.stop(getContext());
                Intent intent = new Intent(context, DialogScreenCastActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        };

        mImageView = (ImageView) rootView.findViewById(R.id.image);
        mImageView.setOnClickListener(clickListener);

        getWindowVisibleDisplayFrame(mRect);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mTouchSlop = mTouchSlop * mTouchSlop;


        mWm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mLp.gravity = Gravity.START | Gravity.TOP;
        mLp.format = PixelFormat.RGBA_8888;
        mLp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //noinspection WrongConstant
        mLp.type = Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        OnTouchListener touchListener = new OnTouchListener() {
            private float touchX;
            private float touchY;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchX = event.getX() + getLeft();
                        touchY = event.getY() + getTop();
                        startX = event.getRawX();
                        startY = event.getRawY();
                        isDragging = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (event.getRawX() - startX);
                        int dy = (int) (event.getRawY() - startY);
                        if ((dx * dx + dy * dy) > mTouchSlop) {
                            isDragging = true;
                            mLp.x = (int) (event.getRawX() - touchX);
                            mLp.y = (int) (event.getRawY() - touchY);
                            mWm.updateViewLayout(FloatView.this, mLp);
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        touchX = touchY = 0.0F;
                        if (isDragging) {
                            reposition();
                            isDragging = false;
                            return true;
                        }
                }
                return false;
            }
        };
        setOnTouchListener(touchListener);

        // setVisibility(GONE);

        ScreencastServiceProxy.watch(this.getContext(), watcher);
        SettingsProvider.get().addObserver(observer);
    }

    protected int getLayoutId() {
        return SettingsProvider.get().floatControlTheme().getLayoutRes();
    }

    public void attach() {
        if (getParent() == null) {
            mWm.addView(this, mLp);
        }
        mWm.updateViewLayout(this, mLp);
        getWindowVisibleDisplayFrame(mRect);
        mRect.top += dp2px(50);
        mLp.y = dp2px(150);
        mLp.x = mRect.width() - dp2px(55);
        reposition();
    }

    public void detach() {
        try {
            mWm.removeViewImmediate(this);
        } catch (Exception ignored) {

        } finally {
            ScreencastServiceProxy.unWatch(this.getContext(), watcher);
            SettingsProvider.get().deleteObserver(observer);
        }
    }

    private boolean isDragging;


    private int dp2px(int dp) {
        return (int) (dp * density);
    }

    private void reposition() {
        if (mLp.x < (mRect.width() - getWidth()) / 2) {
            mLp.x = dp2px(5);
        } else {
            mLp.x = mRect.width() - dp2px(55);
        }
        if (mLp.y < mRect.top) {
            mLp.y = mRect.top;
        }
        mWm.updateViewLayout(this, mLp);
    }
}