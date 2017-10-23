package com.waterfairy.flipview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by water_fairy on 2017/6/13.
 * 995637517@qq.com
 */

public class BaseSelfViewGroup extends AppCompatImageView {
    private static final String TAG = "MenuImageView";
    protected int mWidth, mHeight;
    private ViewDrawObserver viewDrawObserver;
    private int left, right, top, bottom;
    protected boolean initOk;
//    private int currentTimes = 0;
//    private int times = 100;//绘画频率
//    private int sleepTime = 1;
//    private boolean hasDrawFinish;

    public BaseSelfViewGroup(Context context) {
        this(context, null);
    }

    public BaseSelfViewGroup(Context context, @Nullable AttributeSet attrs) {
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

    /**
     * 数据初始化后调用这个
     */
    public void onInitData() {
        viewDrawObserver.onUpdate(ViewCreateObserver.TYPE_DATA, true);
    }

    /**
     * 数据初始化后 调用super.initData();
     */
    protected void initData() {
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
            if (viewState && dataState) {
                initOk = true;
                beforeDraw();
                startDraw();
            }
        }
    }


//    private OnFloatChangeListener onFloatChangeListener;
//    protected boolean isDrawing;
//
//    protected boolean isDrawing() {
//        return isDrawing;
//    }
//
//    protected void setClock(OnFloatChangeListener onFloatChangeListener) {
//        this.onFloatChangeListener = onFloatChangeListener;
//        if (isDrawing) return;
//        isDrawing = true;
//        while (isDrawing) {
//            float ratio = currentTimes / (float) times;//绘画过的比例
//            if (onFloatChangeListener != null) {
//                onFloatChangeListener.onChange(ratio);
//                startDraw();
//            }
//            try {
//                Thread.sleep(sleepTime);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            if (currentTimes >= times) isDrawing = false;
//            currentTimes++;
//        }
//    }

    private void startDraw() {
        postInvalidate();
    }

    /**
     * 有用到长宽度时 调用
     */
    protected void beforeDraw() {

    }
}
