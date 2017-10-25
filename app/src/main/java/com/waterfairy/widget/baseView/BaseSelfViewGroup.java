package com.waterfairy.widget.baseView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by water_fairy on 2017/7/26.
 * 995637517@qq.com
 */

public class BaseSelfViewGroup extends RelativeLayout {

    protected int mWidth, mHeight;
    private int left, right, top, bottom;
    private ViewDrawObserver viewDrawObserver;


    public BaseSelfViewGroup(Context context) {
        super(context);
    }

    public BaseSelfViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        viewDrawObserver = new ViewDrawObserver();
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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
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
                onGetDimen();
            }
        }
    }

    protected void onGetDimen() {

    }
}
