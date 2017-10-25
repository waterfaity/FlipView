package com.waterfairy.flipview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;

/**
 * @author water_fairy
 * @email 995637517@qq.com
 * @date 2017/10/25
 * @Description:
 */

public class FlipViewUtils {

    private static final String TAG = "FlipViewUtils";

    public static Bitmap getBitmap(String imgFile, int maxWidth) {
        if (new File(imgFile).exists()) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                long l = System.currentTimeMillis();
                BitmapFactory.decodeFile(imgFile, options);
                long l1 = System.currentTimeMillis();
                int outWidth = options.outWidth;
                options.inJustDecodeBounds = false;
                int inSampleSize = outWidth / maxWidth;
                if (outWidth % maxWidth != 0) {
                    inSampleSize++;
                }
                if (inSampleSize == 0) inSampleSize = 1;
                options.inSampleSize = inSampleSize;
                options.inDither = false;    /*不进行图片抖动处理*/
                options.inPreferredConfig = null;  /*设置让解码器以最佳方式解码*/
                options.inPurgeable = true;  /* 下面两个字段需要组合使用 */
                options.inInputShareable = true;
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile, options);
                long l2 = System.currentTimeMillis();
                if (bitmap != null) {
                    Log.i(TAG, "获取图像宽时间:" + (l1 - l) + " * 获取bitmap时间:" + (l2 - l1)
                            + " * 原图片宽:" + outWidth + " * 处理后宽:"
                            + bitmap.getWidth() + " * 最大宽:" + maxWidth
                            + " * 缩放倍数:" + inSampleSize);
                    return bitmap;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
    }
}
