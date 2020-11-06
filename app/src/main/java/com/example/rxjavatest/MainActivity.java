package com.example.rxjavatest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private final static String PATH = "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=2930575924,3652732688&fm=26&gp=0.jpg";
    private ProgressDialog progressDialog;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
    }

    public void getImage(View view) {
        /****
         * Rxjava 属于U型结构
         * 最底端点就是订阅步骤subscribe,所以第一步是订阅，然后分别执行第二、第三、第四、第五步
         * 其中第二、第三步属于上游部分，第四、第五步属于下游部分
         * subscribeOn(Schedulers.io())给上游分配异步线程，用于耗时操作
         */
        //起点
        //第二步
        Observable.just(PATH)
                //第三步
                .map(new Function<String, Bitmap>() {

                    @Override
                    public Bitmap apply(String s) throws Throwable {
                        URL url = new URL(s);
                        Log.d("cheshi", "到了");
                        URLConnection urlConnection = url.openConnection();
                        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                        httpURLConnection.setConnectTimeout(1000);
                        Log.d("cheshi", "到了获取");
                        int responseCode = httpURLConnection.getResponseCode();
                        Log.d("cheshi", "responseCode");
                        if (HttpURLConnection.HTTP_OK == responseCode) {
                            Bitmap bitmap = BitmapFactory.decodeStream(httpURLConnection.getInputStream());
                            return bitmap;
                        }
                        return null;
                    }
                })
                //新需求加水印
                .map(new Function<Bitmap, Bitmap>() {
                    @Override
                    public Bitmap apply(Bitmap bitmap) throws Throwable {
                        Paint paint = new Paint();
                        paint.setColor(Color.BLUE);
                        paint.setTextSize(77);
                        return drawTextToBitmap(bitmap,"jayH",paint,300,300);
                    }
                })
                //给上游分配异步线程，用于耗时操作
                .subscribeOn(Schedulers.io())
//                .subscribeOn(AndroidSchedulers.mainThread())
                //给下游分配android主线程
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Observer<Bitmap>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                //第一步
                                progressDialog = new ProgressDialog(MainActivity.this);
                                progressDialog.setTitle("获取图片");
                                progressDialog.show();
                            }

                            @Override
                            public void onNext(@NonNull Bitmap bitmap) {
                                //第四步
                                imageView.setImageBitmap(bitmap);
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                Log.d("cheshi", "失败" + e.getMessage());
                                if (progressDialog != null) progressDialog.dismiss();
                            }

                            @Override
                            public void onComplete() {
                                //终点 全部结束
                                //第五步
                                if (progressDialog != null) progressDialog.dismiss();
                            }
                        }
                );

    }

    private final Bitmap drawTextToBitmap(Bitmap bitmap, String text, Paint paint, int paddingLeft, int paddingTop) {
        Bitmap.Config bitmapConfig=bitmap.getConfig();
        paint.setDither(true);//获取更清晰的图像采样
        paint.setFilterBitmap(true);//过滤一些
        if (bitmapConfig==null){
            bitmapConfig=Bitmap.Config.ARGB_8888;
        }
        bitmap=bitmap.copy(bitmapConfig,true);
        Canvas canvas=new Canvas(bitmap);
        canvas.drawText(text,paddingLeft,paddingTop,paint);
        return bitmap;
    }
}