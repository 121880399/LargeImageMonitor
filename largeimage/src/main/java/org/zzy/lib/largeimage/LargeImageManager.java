package org.zzy.lib.largeimage;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;

import org.zzy.lib.largeimage.util.ConvertUtils;
import org.zzy.lib.largeimage.util.ResHelper;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/4 7:41
 * 描    述：此类主要用于处理Hook得到的图片信息
 * 修订历史：
 * ================================================
 */
public class LargeImageManager {

    /**
     * 保存所有大图信息
     */
    private Map<String, LargeImageInfo> mLargeImageInfo = new ConcurrentHashMap<>();

    private DecimalFormat mDecimalFormat = new DecimalFormat("0.00");

    /**
     * 主线程Handler
     */
    Handler mMainHandler = new Handler(Looper.getMainLooper());

    /**
     * 用来保存超标图片信息是否被显示
     */
    private Map<String,Boolean> mAlarmInfo = new HashMap<>();

    private LargeImageManager() {
    }

    private static class Holder {
        private static LargeImageManager instance = new LargeImageManager();
    }

    public static LargeImageManager getInstance() {
        return Holder.instance;
    }


    /**
     * 得到所有大图信息
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/4 9:26
     */
    public Map<String, LargeImageInfo> getLargeImageInfo() {
        return mLargeImageInfo;
    }

    /**
     * 此方法提供给Okhttp拦截使用，只能得到请求地址和文件大小
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/4 8:05
     */
    public void saveImageInfo(String url, long fileSize) {
        if (fileSize <= 0) {
            return;
        }
        if (LargeImage.getInstance().isLargeImageOpen()) {
            //转换成kb
            double size = ConvertUtils.byte2MemorySize(fileSize, ConvertUtils.KB);
            LargeImageInfo largeImageInfo;
            if (mLargeImageInfo.containsKey(url)) {
                largeImageInfo = mLargeImageInfo.get(url);
            } else {
                largeImageInfo = new LargeImageInfo();
                largeImageInfo.setUrl(url);
                mLargeImageInfo.put(url, largeImageInfo);
            }
            largeImageInfo.setFileSize(size);
        }
    }

    /**
     * 此方法提供给四大图片框架拦截使用，此时的图片已经变成了bitmap，除非人工逆向计算，否则得不到
     * 文件的大小，如果是从网络加载，会在前面一个方法得到文件大小，加载到内存中以后，会通过这个方法
     * 补全信息。如果是加载本地的图片，就没办法得到文件大小，后期看看能不能通过逆向人工计算得到。
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/4 8:32
     */
    public void saveImageInfo(final String url, long memorySize, final int width, final int height, String framework) {
        if (memorySize <= 0) {
            return;
        }
        if(mLargeImageInfo.containsKey(url) && framework.equalsIgnoreCase("Glide")){
            LargeImageInfo largeImageInfo = mLargeImageInfo.get(url);
            if(largeImageInfo.getMemorySize()!=0.0){
                //如果已经存在url，并且已经有内存大小，说明是Glide在加载弹框图片
                //直接返回，不修改数据
                return;
            }
        }
        if (LargeImage.getInstance().isLargeImageOpen()) {
            final double size = ConvertUtils.byte2MemorySize(memorySize, ConvertUtils.KB);
            //目前采取的策略是，在网络下载图片时不论文件大小是否超标，都将保存数据
            //在这个方法进行判断，如果超标则保存，没有超标则进行删除
            if (mLargeImageInfo.containsKey(url)) {
                LargeImageInfo largeImageInfo = mLargeImageInfo.get(url);
                //文件和内存大小其中一个超标则保存
                if (largeImageInfo.getFileSize() > LargeImage.getInstance().getFileSizeThreshold() ||
                        size >= LargeImage.getInstance().getMemorySizeThreshold()) {
                    //如果文件阈值超标，则不论内存是否超标，都要进行数据存储
                    largeImageInfo.setWidth(width);
                    largeImageInfo.setHeight(height);
                    largeImageInfo.setMemorySize(size);
                    largeImageInfo.setFramework(framework);
                } else {
                    //都不超标，则删除
                    mLargeImageInfo.remove(url);
                }
            } else {
                //如果从本地加载图片，则没有文件大小数据，这时候也还没存储数据，只能看内存是否超标
                if (size >= LargeImage.getInstance().getMemorySizeThreshold()) {
                    //超过阈值,进行存储
                    LargeImageInfo largeImageInfo = new LargeImageInfo();
                    largeImageInfo.setUrl(url);
                    mLargeImageInfo.put(url, largeImageInfo);
                    largeImageInfo.setWidth(width);
                    largeImageInfo.setHeight(height);
                    largeImageInfo.setMemorySize(size);
                    largeImageInfo.setFramework(framework);
                }
            }
        }
        isShwoAlarm(url);
    }

    /**
     * 最后判断文件大小或者内存大小是否有超值
     * 有的话弹出提示框。
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/6 17:22
     */
    private void isShwoAlarm(String url) {
        final LargeImageInfo largeImageInfo = mLargeImageInfo.get(url);
        //如果文件大小和内存大小都没有超值，是不会有记录的，所以直接返回
        if (null == largeImageInfo) {
            return;
        }
        if (largeImageInfo.getFileSize() >= LargeImage.getInstance().getFileSizeThreshold() ||
                largeImageInfo.getMemorySize() >= LargeImage.getInstance().getMemorySizeThreshold()) {
            //在主线程显示弹框
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    showDialog(largeImageInfo.getUrl(), largeImageInfo.getWidth(), largeImageInfo.getHeight(), largeImageInfo.getFileSize(), largeImageInfo.getMemorySize());
                }
            });
        }
    }

    /**
     * 如果是BitmapDrawable类型，调用该方法
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/4 9:17
     */
    public Bitmap transform(String imageUrl, BitmapDrawable bitmapDrawable, String framework) {
        Bitmap sourceBitmap = ConvertUtils.drawable2Bitmap(bitmapDrawable);
        return transform(imageUrl, sourceBitmap, framework);
    }

    /**
     * 如果是Bitmap类型调用该方法
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/4 9:17
     */
    public Bitmap transform(String imageUrl, Bitmap sourceBitmap, String framework) {
        if (null == sourceBitmap) {
            return null;
        }
        if (LargeImage.getInstance().isLargeImageOpen()) {
            saveImageInfo(imageUrl, sourceBitmap.getByteCount(), sourceBitmap.getWidth(), sourceBitmap.getHeight(), framework);
        }
        return sourceBitmap;
    }

    //请求悬浮窗权限
    @TargetApi(Build.VERSION_CODES.M)
    private void getOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + LargeImage.APPLICATION.getPackageName()));
        //非Activity启动，记得加上这个属性
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        LargeImage.APPLICATION.getApplicationContext().startActivity(intent);
    }

    /**
     * 显示警告弹窗
     * fileSize,memorySize 传进来时都是KB
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/4 22:05
     */
    public void showDialog(final String url, int width, int height, double fileSize, double memorySize) {
        //判断当前URL是否已经添加进去，如果已经添加进去，则不进行添加
        if(!mAlarmInfo.containsKey(url)){
            mAlarmInfo.put(url,false);
        }
        //如果为true说明该URL已经被弹出，则不再次弹出
        if (mAlarmInfo.get(url)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(LargeImage.APPLICATION)) {
                getOverlayPermission();
                //目前如果没有权限的话，只能返回，否则会报错，这样就可能导致有些警告窗不显示
                return;
            }
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(LargeImage.APPLICATION);
        View dialogView = View.inflate(LargeImage.APPLICATION, R.layout.dialog_custom, null);
        LinearLayout llMemorySize = dialogView.findViewById(R.id.ll_memory_size);
        TextView tvMemorySize = dialogView.findViewById(R.id.tv_memory_size);
        ImageView ivMemorySize = dialogView.findViewById(R.id.iv_memory_size);
        LinearLayout llFileSize = dialogView.findViewById(R.id.ll_file_size);
        TextView tvFileSize = dialogView.findViewById(R.id.tv_file_size);
        ImageView ivFileSize = dialogView.findViewById(R.id.iv_file_size);
        TextView tvSize = dialogView.findViewById(R.id.tv_size);
        TextView tvImageUrl = dialogView.findViewById(R.id.tv_image_url);
        ImageView ivThumb = dialogView.findViewById(R.id.iv_thumb);
        //设置文件大小
        if (fileSize <= 0) {
            llFileSize.setVisibility(View.GONE);
        } else {
            llFileSize.setVisibility(View.VISIBLE);
            if (fileSize > LargeImage.getInstance().getFileSizeThreshold()) {
                ivFileSize.setVisibility(View.VISIBLE);
            } else {
                ivFileSize.setVisibility(View.GONE);
            }
            tvFileSize.setText(ResHelper.getString(R.string.large_image_file_size, mDecimalFormat.format(fileSize)));
        }
        //设置内存大小
        if (memorySize <= 0) {
            llMemorySize.setVisibility(View.GONE);
        } else {
            llMemorySize.setVisibility(View.VISIBLE);
            if (memorySize > LargeImage.getInstance().getMemorySizeThreshold()) {
                ivMemorySize.setVisibility(View.VISIBLE);
            } else {
                ivMemorySize.setVisibility(View.GONE);
            }
            double size = memorySize / 1024;
            tvMemorySize.setText(ResHelper.getString(R.string.large_image_memory_size, mDecimalFormat.format(size)));
        }
        //设置图片尺寸
        if (width <= 0 && height <= 0) {
            tvSize.setVisibility(View.GONE);
        } else {
            tvSize.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            sb.append(width).append("*").append(height);
            tvSize.setText(ResHelper.getString(R.string.large_image_size, sb.toString()));
        }
        //设置图片地址
        if (TextUtils.isEmpty(url)) {
            tvImageUrl.setVisibility(View.GONE);
        } else {
            tvImageUrl.setVisibility(View.VISIBLE);
            tvImageUrl.setText(ResHelper.getString(R.string.large_image_url, url));
            tvImageUrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        //获取剪贴板管理器
                        ClipboardManager cm = (ClipboardManager) LargeImage.APPLICATION.getSystemService(Context.CLIPBOARD_SERVICE);
                        //创建普通字符型ClipData
                        ClipData clipData = ClipData.newPlainText("Label", url);
                        cm.setPrimaryClip(clipData);
                        Toast.makeText(LargeImage.APPLICATION.getApplicationContext(), "复制成功！", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(LargeImage.APPLICATION.getApplicationContext(), "复制失败！", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });
        }
        //设置图片
        Glide.with(dialogView).load(url).into(ivThumb);
        AlertDialog alertDialog = builder.setTitle("提示").setView(dialogView).setPositiveButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAlarmInfo.put(url,false);
                dialog.dismiss();
            }
        }).setNegativeButton("不再提醒（直到下次重启）", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LargeImage.getInstance().setLargeImageOpen(false);
                dialog.dismiss();
            }
        }).create();
        //设置全局Dialog
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
        }
        alertDialog.show();
        //标识正在显示警告
        mAlarmInfo.put(url,true);
    }


}
