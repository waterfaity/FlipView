package com.waterfairy.flipview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.List;

/**
 * @author water_fairy
 * @email 995637517@qq.com
 * @date 2017/10/23
 * @Description:
 */

public class PageViewAdapter implements FlipAdapter {

    private static final String TAG = "adapter";
    private String path;
    private List<BookBean.ContentsBean> contents;

    public PageViewAdapter(String path, List<BookBean.ContentsBean> contents) {
        this.path = path;
        this.contents = contents;
    }

    @Override
    public int getCount() {
        if (contents != null) return contents.size();
        return 0;
    }

    @Override
    public Bitmap getBitmap(int position) {
        Bitmap bitmap = null;
        if (position >= 0) {
            try {
                //        http://h.xueduoduo.com.cn/data4/image/2017/09/04/134134095242682.jpeg
                BookBean.ContentsBean contentsBean = contents.get(position);
                String imageUrl = contentsBean.getImageUrl();
                String replace = imageUrl.replace("/", "-");
                String[] split = replace.split("-");
                String name = split[split.length - 1];
                String abPat = path + "/" + MD5Utils.getMD5Code(name);
                //获取bitmap
                bitmap = FlipViewUtils.getBitmap(abPat, 1200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        }
        return bitmap;
    }
}
