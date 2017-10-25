package com.waterfairy.widget.baseView;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by water_fairy on 2017/6/13.
 * 995637517@qq.com
 */

public class BaseSelfView extends AppCompatImageView {
    private static final String TAG = "MenuImageView";
    protected int mWidth, mHeight;
    private ViewDrawObserver viewDrawObserver;
    private int left, right, top, bottom;
    private int currentTimes = 0;
    private int times = 100;//绘画频率
    private int sleepTime = 1;
    private boolean hasDrawFinish;
    protected boolean canDraw;
    private boolean needDraw = true;

    public BaseSelfView(Context context) {
        this(context, null);
    }

    public BaseSelfView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        viewDrawObserver = new ViewDrawObserver();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        left = l;
        right = r;
        top = t;
        bottom = b;
        int width = r - l;
        int height = b - t;
        if (width != 0 && height != 0) {
            boolean change = false;
            if (width != this.mWidth) {
                this.mWidth = width;
                change = true;
            }
            if (height != this.mHeight) {
                this.mHeight = height;
                change = true;
            }
            if (change) {
                viewDrawObserver.onUpdate(ViewCreateObserver.TYPE_VIEW, true);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canDraw) {
            drawOne(canvas);
        }
    }

    protected void drawOne(Canvas canvas) {

    }

    /**
     * 数据初始化后调用这个
     */
    public void onInitData() {
        viewDrawObserver.onUpdate(ViewCreateObserver.TYPE_DATA, true);
    }

    /**
     * 数据初始化后 调用super.initData();
     */
    protected void onInitDataOk() {
        onInitData();
    }

    /**
     * 数据初始化后 调用super.initData();
     */
    protected void onInitDataOk(boolean needDraw) {
        this.needDraw = needDraw;
        onInitData();
    }

    private class ViewDrawObserver implements ViewCreateObserver {
        private boolean viewState;
        private boolean dataState;

        @Override
        public void onUpdate(int type, boolean state) {
            if (type == TYPE_VIEW) {
                viewState = state;
            } else if (type == TYPE_DATA) {
                dataState = state;
            }
            canDraw = false;
            if (viewState && dataState) {
                canDraw = true;
                beforeDraw();
                startDraw();
            }
        }
    }


    private OnFloatChangeListener onFloatChangeListener;
    protected boolean isDrawing;

    protected boolean isDrawing() {
        return isDrawing;
    }

    private Handler handler;//延时时钟

    protected void setClock(final OnFloatChangeListener onFloatChangeListener) {
        this.onFloatChangeListener = onFloatChangeListener;
        if (isDrawing) return;
        Log.i(TAG, "setClock: " + isDrawing);
        isDrawing = true;
        currentTimes = 0;
        if (handler == null) {
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == 0) {
                        if (onFloatChangeListener != null && isDrawing) {
                            float ratio = currentTimes / (float) times;//绘画过的比例
                            startDraw();
                            onFloatChangeListener.onChange(ratio);
                            if (ratio >= 1) {
                                isDrawing = false;
                                removeMessages(0);
                                sendEmptyMessageDelayed(1, 100);
                            } else if (ratio < 1) {
                                currentTimes++;
                                sendEmptyMessageDelayed(0, sleepTime);
                            }
                        }
                    } else if (msg.what == 1) {
                        onFloatChangeListener.onFinish();
                    }
                }
            };
        } else {
            handler.removeMessages(0);
        }
        handler.sendEmptyMessage(0);
    }

    private void startDraw() {
        if (needDraw)
            invalidate();
    }

    protected void beforeDraw() {

    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }
}
