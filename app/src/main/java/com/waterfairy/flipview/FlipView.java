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
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.waterfairy.widget.baseView.BaseSelfView;
import com.waterfairy.widget.baseView.OnFloatChangeListener;

/**
 * user : water_fairy
 * email:995637517@qq.com
 * date :2017/10/22
 * des  :
 */

public class FlipView extends BaseSelfView {
    private static final String TAG = "pageView";
    private FlipAdapter adapter;//adapter
    private int viewWidth, viewHeight, leftEnd, rightStart;//宽,高,左边一半右侧,右边一半左侧
    private LruCache<Integer, Bitmap> lruCache;//缓存图片bitmap
    private int currentPos;//当前位置
    private Paint bitmapPaint = new Paint();  //bitmap bitmapPaint
    private Rect leftViewRect, rightViewRect;//画板左侧,画板右侧

    private float startX = 0;//开始x坐标
    private float radio;//旋转指数 右滑: 0->1 , 左滑: 0->-1
    private float upRadio;//滑动抬起时 radio


    public FlipView(Context context) {
        this(context, null);
    }

    public FlipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLruCache();
        bitmapPaint = new Paint();
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);
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
        setSleepTime(15);
        setTimes(15);
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
        invalidate();
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
            try {
                lruCache.put(pos, bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (adapter != null && !isDrawing) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
//                    calcMoveX(event.getX() - startX);
                    break;
                case MotionEvent.ACTION_UP:
                    calcUpX(event.getX() - startX);
//                    handleUpAction();
                    break;
            }
            return true;
        } else return false;
    }

    private int sign;

    /**
     * 处理抬手事件
     */
    private void handleUpAction(boolean right) {
        sign = right ? 1 : -1;
        setClock(new OnFloatChangeListener() {
            @Override
            public void onChange(float value) {
                radio = sign * (float) ((Math.sin((value - 0.5f) * Math.PI) + 1) / 2f);
            }

            @Override
            public void onFinish() {
                calcCurrentPos(sign == 1);
                cacheBitmapSide();
            }
        });

    }

    private void calcCurrentPos(boolean right) {
        if (right) currentPos--;
        else currentPos++;
    }
// /**
//     * 处理抬手事件
//     */
//    private void handleUpAction() {
//        upRadio = radio;
//        if (upRadio >= 1 || upRadio <= -1) {
//            //滑动到翻页结束
//            cacheBitmapSide();
//        } else if (upRadio != 0) {
//            //滑动中间位置
//            sign = 1;
//            if (upRadio < 1 && upRadio > 0.5 && currentPos > 0) {
//                //右滑翻页
//                sign = 1;
//                mulValue = 1 - upRadio;
//            } else if (upRadio > -1 && upRadio < -0.5 && currentPos < adapter.getCount() - 1) {
//                //左滑翻页
//                sign = -1;
//                mulValue = 1 + upRadio;
//            } else if (upRadio < 0.5 && upRadio > 0) {
//                //右滑不翻页
//                sign = -1;
//                mulValue = upRadio;
//            } else if (upRadio < 0 && upRadio > -0.5f) {
//                //左滑不翻页
//                sign = 1;
//                mulValue = upRadio;
//            }
//            mulValue = Math.abs(mulValue);
//            Log.i(TAG, "handleUpAction: " + mulValue + " ");
//
//            setClock(new OnFloatChangeListener() {
//                @Override
//                public void onChange(float value) {
//                    radio = upRadio + (sign * mulValue * value);
//                    Log.i(TAG, "value: " + value + "  ---- radio:" + radio + "---- mulValue:" + mulValue + " ---- sign:" + sign);
//                }
//
//                @Override
//                public void onFinish() {
//                    Log.i(TAG, "onFinish: 完成");
//                    calcCurrentPos(upRadio);
//                    cacheBitmapSide();
//                }
//            });
//        }
//    }

//    private void calcCurrentPos(float upRadio) {
//        if (this.upRadio < 1 && this.upRadio > 0.5 && currentPos > 0) {
//            //右滑翻页
//            currentPos--;
//        } else if (this.upRadio > -1 && this.upRadio < -0.5 && currentPos < adapter.getCount() - 1) {
//            //左滑翻页
//            currentPos++;
//        }
//    }

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
    private void calcMoveX(float dela) {
        if (viewHeight != 0 && ((dela > 0 && currentPos > 0) || (dela < 0 && currentPos < adapter.getCount() - 1))) {
            radio = dela / (viewHeight / 2);
            radio = radio > 1 ? 1 : radio < -1 ? -1 : radio;
            postInvalidate();
        } else {
            radio = 0;
        }
    }

    /**
     * 计算滑动距离
     *
     * @param dela
     */
    private void calcUpX(float dela) {
        if (viewHeight != 0 && ((dela > 0 && currentPos > 0) || (dela < 0 && currentPos < adapter.getCount() - 1))) {
            radio = dela / (viewHeight / 3);
            radio = radio > 1 ? 1 : radio < -1 ? -1 : radio;
            if (radio > 0.5f) {
                //右滑
                handleUpAction(true);
            } else if (radio < -0.5f) {
                //左滑
                handleUpAction(false);
            }
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
                            new RectF(leftViewRect.left, leftViewRect.top, leftViewRect.left + viewCenterX, leftViewRect.bottom), bitmapPaint);//左侧
                    //中间:
                    RectF centerRect = new RectF(leftViewRect.left + viewCenterX, leftViewRect.top, leftViewRect.right, leftViewRect.bottom);
                    canvas.drawBitmap(currentBitmap, getBitmapLeftRect(currentBitmap,
                            currentBitmap.getWidth() / 2),
                            centerRect, bitmapPaint);//中
                    //画阴影
                    canvas.drawRect(centerRect, getPaint(radio, centerRect.left, centerRect.right));
                    //右侧:
                    canvas.drawBitmap(currentBitmap,
                            getBitmapRightRect(currentBitmap, currentBitmap.getWidth() / 2),
                            new RectF(rightViewRect.left, rightViewRect.top, rightViewRect.right, rightViewRect.bottom), bitmapPaint);
                } else {
                    int bitmapCenterX = (int) (currentBitmap.getWidth() * radio);
                    //左侧:
                    canvas.drawBitmap(preBitmap,
                            getBitmapLeftRect(preBitmap, preBitmap.getWidth() / 2),
                            new RectF(leftViewRect.left, leftViewRect.top, leftViewRect.right, leftViewRect.bottom), bitmapPaint);//左侧
                    //中间:
                    RectF centerRectF = new RectF(rightViewRect.left, leftViewRect.top, leftViewRect.left + viewCenterX, rightViewRect.bottom);
                    canvas.drawBitmap(preBitmap, getBitmapRightRect(preBitmap, preBitmap.getWidth() / 2),
                            centerRectF, bitmapPaint);//中
                    //画阴影
                    canvas.drawRect(centerRectF, getPaint(radio, centerRectF.left, centerRectF.right));

                    //右侧:
                    canvas.drawBitmap(currentBitmap,
                            getBitmapRightRect(currentBitmap, bitmapCenterX),
                            new RectF(leftViewRect.left + viewCenterX, rightViewRect.top, rightViewRect.right, rightViewRect.bottom), bitmapPaint);
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
                    //画阴影
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
                    //画阴影
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
            Log.i(TAG, "cacheBitmap: -缓存大小:" + lruCache.size() / (1024 * 1024) + "M  -当前:" + pos + "  -宽:" + cacheBitmap.getWidth() + "  -高:" + cacheBitmap.getHeight());
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
//            Log.i(TAG, "calcViewSide: " + leftViewRect);
//            Log.i(TAG, "calcViewSide: " + rightViewRect);
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
