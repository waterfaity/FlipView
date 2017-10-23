package com.waterfairy.flipview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.v4.util.LruCache;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * user : water_fairy
 * email:995637517@qq.com
 * date :2017/10/22
 * des  :
 */

public class PageView2 extends BaseSelfViewGroup {
    private static final String TAG = "pageView";
    private FlipAdapter adapter;
    private int viewWidth, viewHeight, leftEnd, rightStart;
    private LruCache<Integer, Bitmap> lruCache;
    private int currentPos;
    private Paint paint = new Paint();
    private Shader leftDeepShader, rightDeepShader;
//    private SparseArray<Rect> mLeftViewRectArr, mRightViewRectArr;

    private Rect leftViewRect, rightViewRect;

    public PageView2(Context context) {
        this(context, null);
    }

    public PageView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLruCache();
        paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setDither(true);
    }

    /**
     * 缓存处理
     */
    private void initLruCache() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        int cacheSize = (int) (maxMemory / 8);
        lruCache = new LruCache<Integer, Bitmap>(cacheSize) {
            //必须重写此方法，来测量Bitmap的大小
            @Override
            protected int sizeOf(Integer key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    @Override
    protected void beforeDraw() {
        viewHeight = mHeight;
        viewWidth = mWidth;
        leftEnd = viewWidth / 2;
        rightStart = viewWidth - leftEnd;

        if (lruCache == null) {
            initLruCache();
        } else {
            lruCache.evictAll();
        }


        currentPos = 0;
        calcViewSide(cacheBitmap(0));
        cacheBitmap(1);

        shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);

    }

    /**
     * 获取bitmap
     *
     * @param pos
     * @return
     */
    private Bitmap getBitmap(int pos) {
        if (lruCache == null) return null;
        Bitmap bitmap = lruCache.get(pos);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = adapter.getBitmap(pos);
            lruCache.put(pos, bitmap);
        }
        return bitmap;
    }


    private float startX = 0;
    private float radio;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (adapter != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    calcX(event.getX() - startX);
                    break;
                case MotionEvent.ACTION_UP:
                    if (radio > 0.5 && currentPos > 0) {
                        currentPos--;
                    } else if (radio < -0.5 & currentPos < adapter.getCount() - 1) {
                        currentPos++;
                    }
                    radio = 0;
                    invalidate();
                    cacheBitmapSide();
                    break;
            }
            return true;
        } else return false;
    }

    /**
     * 缓存当前位置左右俩个页面
     */
    private void cacheBitmapSide() {
        if (currentPos > 0) {
            cacheBitmap(currentPos - 1);
        }
        if (currentPos < adapter.getCount() - 1) {
            cacheBitmap(currentPos + 1);
        }
    }

    /**
     * 计算滑动距离
     *
     * @param dela
     */
    private void calcX(float dela) {
        if (viewHeight != 0 && ((dela > 0 && currentPos > 0) || (dela < 0 && currentPos < adapter.getCount() - 1))) {
            radio = dela / (viewHeight / 2);
            radio = radio > 1 ? 1 : radio < -1 ? -1 : radio;
            invalidate();
        } else {
            radio = 0;
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewHeight != 0 && adapter != null) {
            if (radio > 0) {
                //右滑 -> 当前右侧不动 pre左侧改变
                Bitmap currentBitmap = getBitmap(currentPos);
                Bitmap preBitmap = getBitmap(currentPos - 1);
                int viewCenterX = (int) ((rightViewRect.right - leftViewRect.left) * radio);
                if (Math.abs(radio) < 0.5f) {
                    int bitmapCenterX = (int) (preBitmap.getWidth() * radio);
                    //左侧:
                    canvas.drawBitmap(preBitmap,
                            getBitmapLeftRect(preBitmap, bitmapCenterX),
                            new RectF(leftViewRect.left, leftViewRect.top, leftViewRect.left + viewCenterX, leftViewRect.bottom), paint);//左侧
                    //中间:
                    RectF centerRect = new RectF(leftViewRect.left + viewCenterX, leftViewRect.top, leftViewRect.right, leftViewRect.bottom);
                    canvas.drawBitmap(currentBitmap, getBitmapLeftRect(currentBitmap,
                            currentBitmap.getWidth() / 2),
                            centerRect, paint);//中
                    canvas.drawRect(centerRect, getPaint(radio, centerRect.left, centerRect.right));
                    //右侧:
                    canvas.drawBitmap(currentBitmap,
                            getBitmapRightRect(currentBitmap, currentBitmap.getWidth() / 2),
                            new RectF(rightViewRect.left, rightViewRect.top, rightViewRect.right, rightViewRect.bottom), paint);
                } else {
                    int bitmapCenterX = (int) (currentBitmap.getWidth() * radio);
                    //左侧:
                    canvas.drawBitmap(preBitmap,
                            getBitmapLeftRect(preBitmap, preBitmap.getWidth() / 2),
                            new RectF(leftViewRect.left, leftViewRect.top, leftViewRect.right, leftViewRect.bottom), paint);//左侧
                    //中间:
                    RectF centerRectF = new RectF(rightViewRect.left, leftViewRect.top, leftViewRect.left + viewCenterX, rightViewRect.bottom);
                    canvas.drawBitmap(preBitmap, getBitmapRightRect(preBitmap, preBitmap.getWidth() / 2),
                            centerRectF, paint);//中
                    canvas.drawRect(centerRectF, getPaint(radio, centerRectF.left, centerRectF.right));

                    //右侧:
                    canvas.drawBitmap(currentBitmap,
                            getBitmapRightRect(currentBitmap, bitmapCenterX),
                            new RectF(leftViewRect.left + viewCenterX, rightViewRect.top, rightViewRect.right, rightViewRect.bottom), paint);
                }
            } else if (radio < 0) {
                float radioTemp = -radio;
                //左滑 <- 左侧不动 右侧改变
                Bitmap currentBitmap = getBitmap(currentPos);
                Bitmap nextBitmap = getBitmap(currentPos + 1);
                int viewCenterX = (int) ((rightViewRect.right - leftViewRect.left) * (1 - radioTemp));

                if (Math.abs(radioTemp) < 0.5f) {
                    int bitmapCenterX = (int) (nextBitmap.getWidth() * (1 - radioTemp));
                    //左侧:
                    canvas.drawBitmap(currentBitmap,
                            getBitmapLeftRect(currentBitmap, currentBitmap.getWidth() / 2),
                            new RectF(leftViewRect.left, leftViewRect.top, leftViewRect.right, leftViewRect.bottom), null);//左侧
                    //中间:
                    RectF centerRectF = new RectF(rightViewRect.left, rightViewRect.top, leftViewRect.left + viewCenterX, rightViewRect.bottom);
                    canvas.drawBitmap(currentBitmap,
                            getBitmapRightRect(currentBitmap, currentBitmap.getWidth() - currentBitmap.getWidth() / 2),
                            centerRectF, null);//中
                    canvas.drawRect(centerRectF, getPaint(radio, centerRectF.left, centerRectF.right));
                    //右侧:
                    canvas.drawBitmap(nextBitmap,
                            getBitmapRightRect(nextBitmap, bitmapCenterX),
                            new RectF(leftViewRect.left + viewCenterX, rightViewRect.top, rightViewRect.right, rightViewRect.bottom), null);
                } else {
                    int bitmapCenterX = (int) (currentBitmap.getWidth() * (1 - radioTemp));
                    //左侧:
                    canvas.drawBitmap(currentBitmap,
                            getBitmapLeftRect(currentBitmap, bitmapCenterX),
                            new RectF(leftViewRect.left, leftViewRect.top, leftViewRect.left + viewCenterX, leftViewRect.bottom), null);//左侧
                    //中间:
                    RectF centerRectF = new RectF(leftViewRect.left + viewCenterX, leftViewRect.top, leftViewRect.right, leftViewRect.bottom);
                    canvas.drawBitmap(nextBitmap,
                            getBitmapLeftRect(nextBitmap, nextBitmap.getWidth() - nextBitmap.getWidth() / 2),
                            centerRectF, null);//中
                    canvas.drawRect(centerRectF, getPaint(radio, centerRectF.left, centerRectF.right));
                    //右侧:
                    canvas.drawBitmap(nextBitmap,
                            getBitmapRightRect(nextBitmap, nextBitmap.getWidth() - nextBitmap.getWidth() / 2),
                            new RectF(rightViewRect.left, rightViewRect.top, rightViewRect.right, rightViewRect.bottom), null);
                }

            } else if (radio == 0) {
                Bitmap bitmap = getBitmap(currentPos);
                canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                        new Rect(leftViewRect.left, leftViewRect.top, rightViewRect.right, rightViewRect.bottom), null);//静止
            }
        }
    }

    private Paint shadowPaint = new Paint();

    private Paint getPaint(float radio, float left, float right) {
        float radioTemp = Math.abs(radio);
        if (radioTemp < 0.5) {
            if (radio > 0) {
                //左侧深
                shadowPaint.setShader(new LinearGradient(left, 0, right, 0, argb(radioTemp, 0, 0, 0), Color.TRANSPARENT, Shader.TileMode.MIRROR));
            } else {
                //右侧深
                shadowPaint.setShader(new LinearGradient(left, 0, right, 0, Color.TRANSPARENT, argb(radioTemp, 0, 0, 0), Shader.TileMode.MIRROR));
            }
        } else {
            if (radio > 0) {
                //右侧深
                shadowPaint.setShader(new LinearGradient(left, 0, right, 0, Color.TRANSPARENT, argb(1 - radioTemp, 0, 0, 0), Shader.TileMode.MIRROR));
            } else {
                //左侧深
                shadowPaint.setShader(new LinearGradient(left, 0, right, 0, argb(1 - radioTemp, 0, 0, 0), Color.TRANSPARENT, Shader.TileMode.MIRROR));
            }
        }
        return shadowPaint;
    }

    public static int argb(float alpha, float red, float green, float blue) {
        return ((int) (alpha * 255.0f + 0.5f) << 24) |
                ((int) (red * 255.0f + 0.5f) << 16) |
                ((int) (green * 255.0f + 0.5f) << 8) |
                (int) (blue * 255.0f + 0.5f);
    }

    public void setAdapter(FlipAdapter adapter) {
        this.adapter = adapter;
        onInitData();
    }

    /**
     * 缓存指定位置的bitmap
     *
     * @param pos
     * @return
     */
    private Bitmap cacheBitmap(int pos) {
        Bitmap cacheBitmap = lruCache.get(pos);
        if (cacheBitmap == null || cacheBitmap.isRecycled()) {
            cacheBitmap = adapter.getBitmap(pos);
            lruCache.put(pos, cacheBitmap);
            Log.i(TAG, "cacheBitmap: -缓存数量:" + lruCache.size() + "  -当前:" + pos + "  -宽:" + cacheBitmap.getWidth() + "  -高:" + cacheBitmap.getHeight());
        }
        return cacheBitmap;
    }

    /**
     * 计算绘制到view的边缘
     *
     * @param cacheBitmap
     */
    private void calcViewSide(Bitmap cacheBitmap) {

        leftViewRect = new Rect();
        rightViewRect = new Rect();
        if (cacheBitmap != null && !cacheBitmap.isRecycled()) {
            int bitmapWidth = cacheBitmap.getWidth();
            int bitmapHeight = cacheBitmap.getHeight();

            if (bitmapWidth / (float) bitmapHeight > viewWidth / (float) viewHeight) {
                //图片宽度 max
                float viewHeightTemp = bitmapHeight / (float) bitmapWidth * viewWidth;
                int top = (int) ((viewHeight - viewHeightTemp) / 2);
                int bottom = viewHeight - top;
                leftViewRect = new Rect(0, top, leftEnd, bottom);
                rightViewRect = new Rect(rightStart, top, viewWidth, bottom);
            } else {
                //图片高度 max
                float viewWidthTemp = bitmapWidth / (float) bitmapHeight * viewHeight;
                int left = (int) ((viewWidth - viewWidthTemp) / 2);
                int right = viewWidth - left;
                leftViewRect = new Rect(left, 0, leftEnd, viewHeight);
                rightViewRect = new Rect(rightStart, 0, right, viewHeight);
            }
            Log.i(TAG, "calcViewSide: " + leftViewRect);
            Log.i(TAG, "calcViewSide: " + rightViewRect);
        }
    }

    /**
     * 获取bitmap 左半边
     *
     * @param bitmap
     * @param rightX
     * @return
     */
    private Rect getBitmapLeftRect(Bitmap bitmap, int rightX) {
        if (bitmap == null || bitmap.isRecycled()) {
            return new Rect(0, 0, 0, 0);
        } else {
            return new Rect(0, 0, rightX, bitmap.getHeight());
        }

    }

    /**
     * 获取bitmap 右半边
     *
     * @param bitmap
     * @param leftX
     * @return
     */
    private Rect getBitmapRightRect(Bitmap bitmap, int leftX) {
        if (bitmap == null || bitmap.isRecycled()) {
            return new Rect(0, 0, 0, 0);
        } else {
            int width = bitmap.getWidth();
            return new Rect(leftX, 0, width, bitmap.getHeight());
        }
    }
}
