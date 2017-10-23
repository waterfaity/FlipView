package com.waterfairy.flipview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "main";
    private PageView2 pageView;
    private FlipView flipView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pageView = (PageView2) findViewById(R.id.page_view);
//        flipView = (FlipView) findViewById(R.id.flip_view);

    }


    public void load(View view) {
        String path = ((EditText) findViewById(R.id.edit)).getText().toString();
        String config = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(new File(path + "/" + MD5Utils.getMD5Code("temp.txt")))));
            config = bufferedReader.readLine();

            if (!TextUtils.isEmpty(config)) {
                Gson gson = new Gson();
                BookBean bean = gson.fromJson(config, BookBean.class);
                List<BookBean.ContentsBean> contents = bean.getContents();
                PageViewAdapter pageViewAdapter = new PageViewAdapter(path, contents);
                pageView.setAdapter(pageViewAdapter);
//                flipView.setAdapter(pageViewAdapter);
                Log.i(TAG, "load:");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
