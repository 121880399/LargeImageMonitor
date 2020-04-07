package org.zzy.lib.largeimage.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import org.zzy.lib.largeimage.LargeImage;
import org.zzy.lib.largeimage.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/7 16:52
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ImageUtil {

    /**
     * 从输入流中得到Bitmap
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/7 17:05
     */
    private static  Bitmap decodeBitmapFromStream(String filePath, int targetWidth, int targetHeight) {
        Log.d(LargeImage.TAG, "decodeBitmapFromStream Thread id:" + Thread.currentThread().getId());
        BitmapFactory.Options options = new BitmapFactory.Options();
        //只获取Bitmap原始宽高，不分配内存
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        //计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 只要bitmap的宽或者高 大于ImageView的宽高
     * 那么就计算inSampleSize的值
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/7 17:14
     */
    private static  int calculateInSampleSize(BitmapFactory.Options options, int targetWidth, int targetHeight) {
        if (targetHeight == 0 || targetWidth == 0) {
            return 1;
        }
        Log.d(LargeImage.TAG, "calculateInSampleSize thread id:" + Thread.currentThread().getId());
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (width > targetWidth || height > targetHeight) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= targetHeight &&
                    (halfWidth / inSampleSize) >= targetWidth) {
                Log.d(LargeImage.TAG, "calculateInSampleSize thread id:" + Thread.currentThread().getId() + " inSampleSize:" + inSampleSize + " H");
                inSampleSize *= 2;
            }

        }
        return inSampleSize;
    }

    /**
     * 如果使用四大图片框架，则会有循环引用的问题，所以只能利用OkHttp自己实现图片下载
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/7 17:17
     */
    public static  void downloadImage(final String url, final ImageView targetView) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                targetView.setImageResource(R.drawable.ic_failed);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String root = LargeImage.APPLICATION.getFilesDir().getAbsolutePath() + "/largeimage";
                InputStream is = null;
                FileOutputStream fileOutputStream = null;
                File dir = new File(root);
                if(!dir.exists()){
                    dir.mkdirs();
                }
                String imageUrl = url;
                int index = imageUrl.lastIndexOf("/");
                String fileName = imageUrl.substring(index,imageUrl.length());
                File file = new File(dir,fileName);
                try {
                    Log.d(LargeImage.TAG, "onResponse thread id:" + Thread.currentThread().getId());
                    is = response.body().byteStream();
                    fileOutputStream = new FileOutputStream(file);
                    byte [] bytes = new byte[2048];
                    int len = -1;
                    while((len=is.read(bytes)) != -1){
                        fileOutputStream.write(bytes,0,len);
                    }
                    fileOutputStream.flush();
                    final Bitmap bitmap = decodeBitmapFromStream(file.getAbsolutePath(), targetView.getWidth(), targetView.getHeight());
                    targetView.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(LargeImage.TAG, "onResponse thread id:" + Thread.currentThread().getId());
                            targetView.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (is != null) {
                        is.close();
                    }
                    if(fileOutputStream != null){
                        fileOutputStream.close();
                    }
                }
            }
        });
    }
}
