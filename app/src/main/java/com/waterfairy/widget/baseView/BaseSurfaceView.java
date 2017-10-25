package com.waterfairy.widget.baseView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

//import android.util.Log;

/**
 * Created by water_fairy on 2017/5/25.
 * 995637517@qq.com
 */

public abstract class BaseSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "BaseSurfaceView";
    protected SurfaceHolder mSurfaceHolder;
    protected int mWidth, mHeight;
    private int currentTimes = 0;
    private int times;//绘画频率
    private int sleepTime = 1;
    protected ViewDrawObserver viewDrawObserver;
    protected int mBgColor;
    private boolean hasDrawFinish;//绘制完成
    protected boolean isDrawing;//绘画中
    private OnCanvasChangeListener onFloatChangeListener;
    protected boolean hasCreate;//surfaceHolder 创建
    protected boolean hasInitData;//数据初始化
    private boolean hasWidthInit;
    private static final int TIME_NO = -1;
    private static final int TIME_MAX = 100;
    protected int mState;
    public static final int TYPE_NO = 1;
    public static final int TYPE_DRAWING = 2;
    public static final int TYPE_DRAW_FINISHED = 3;
    public static final int TYPE_DESTROY = 4;


    public BaseSurfaceView(Context context) {
        this(context, null);
    }

    public BaseSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBgColor = Color.WHITE;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);//透明
        setZOrderOnTop(true);
        viewDrawObserver = new ViewDrawObserver();
        mSurfaceHolder.addCallback(this);
    }

    private int widthMeasureSpec, heightMeasureSpec;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (hasWidthInit) {
            super.onMeasure(this.widthMeasureSpec, this.heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        if (hasWidthInit) return;
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.widthMeasureSpec = widthMeasureSpec;
        this.heightMeasureSpec = heightMeasureSpec;
//        Log.i(TAG, "drawWidth:  " + mWidth + "--" + mHeight);

    }

    protected boolean isDrawing() {
        return isDrawing;
    }

    protected void setClockNo() {
        setClock(null, TIME_NO);
    }

    protected void setClock(OnCanvasChangeListener onFloatChangeListener) {
        setClock(onFloatChangeListener, TIME_MAX);
    }

    protected void setClock(OnCanvasChangeListener onFloatChangeListener, int time) {
        if (time == 0 || time < -1) return;
        this.onFloatChangeListener = onFloatChangeListener;
        times = time;
        if (isDrawing) return;
        isDrawing = true;
        currentTimes = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isDrawing) {
                    float ratio = 0;
                    if (currentTimes < 0) {
                        currentTimes = 0;
                    }
                    ratio = currentTimes / (float) times;//绘画过的比例
                    Canvas canvas = null;
                    try {
                        canvas = mSurfaceHolder.lockCanvas();
                        if (TIME_NO == times) {
                            drawOne(canvas);
                        } else {
                            if (BaseSurfaceView.this.onFloatChangeListener != null)
                                BaseSurfaceView.this.onFloatChangeListener.onChange(canvas, ratio);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            mSurfaceHolder.unlockCanvasAndPost(canvas);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (currentTimes >= times) isDrawing = false;
                    currentTimes++;
                }
                hasDrawFinish = true;
                hasWidthInit = true;
            }
        }).start();

    }

    protected void drawOne(Canvas canvas) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        onCreateOk();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasCreate = false;
//        Log.i(TAG, "surfaceDestroyed: ");
        isDrawing = false;
        viewDrawObserver.onUpdate(ViewCreateObserver.TYPE_VIEW, false);
    }


    private class ViewDrawObserver implements ViewCreateObserver {
        private boolean viewState;
        private boolean dataState;

        @Override
        public void onUpdate(int type, boolean state) {
            if (type == TYPE_VIEW) {
                hasCreate = state;
                viewState = state;
            } else if (type == TYPE_DATA) {
                currentTimes = -1;//防止0的时候刚好加1   没有0的情况
                hasInitData = state;
                hasDrawFinish = false;
                dataState = state;
            }
            if (viewState && dataState) {
                if (!hasDrawFinish) {
                    beforeDraw();
                    startDraw();
                } else if (type == TYPE_VIEW) {
                    drawFinishView();
                }
            }
        }
    }

    private void drawFinishView() {
        isDrawing = true;
        Canvas canvas = null;
        try {
            canvas = mSurfaceHolder.lockCanvas();
            drawFinishView(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (isDrawing && hasCreate) {
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            isDrawing = false;
        }
    }

    public float getTextLen(String content, float textSize) {
        if (TextUtils.isEmpty(content) || textSize <= 0) return 0;
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        return paint.measureText(content);
    }

    protected abstract void beforeDraw();

    protected abstract void startDraw();

    protected abstract void drawFinishView(Canvas canvas);

    protected void onInitDataOk() {
        viewDrawObserver.onUpdate(ViewCreateObserver.TYPE_DATA, true);
    }

    private void onCreateOk() {
        viewDrawObserver.onUpdate(ViewCreateObserver.TYPE_VIEW, true);
    }

}
