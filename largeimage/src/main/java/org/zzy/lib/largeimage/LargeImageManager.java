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

import com.tencent.mmkv.MMKV;

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

    private DecimalFormat mDecimalFormat = new DecimalFormat("0.00");

    /**
     * 主线程Handler
     */
    Handler mMainHandler = new Handler(Looper.getMainLooper());

    /**
     * 自定义MMKV来存储大图信息
     */
    private MMKV mmkv;

    /**
     * 用来保存超标图片信息是否被显示
     */
    private Map<String, Boolean> mAlarmInfo = new HashMap<>();

    /**
     * 缓存超标的Bitmap，用于弹窗显示
     */
    private Map<String,Bitmap> mBitmapCache = new ConcurrentHashMap<>();

    /**
     * 缓存大图信息，用于列表显示
     */
    private Map<String,LargeImageInfo> mInfoCache = new ConcurrentHashMap<>();

    /**
     * 是否开启弹窗
     */
    private boolean openDialog = false;

    private LargeImageManager() {
        mmkv = MMKV.mmkvWithID("LargeImage");
    }

    private static class Holder {
        private static LargeImageManager instance = new LargeImageManager();
    }

    public static LargeImageManager getInstance() {
        return Holder.instance;
    }

    /**
     * @return 返回所有超标图片信息
     */
    public MMKV getMmkv() {
        return mmkv;
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
            if (mmkv.containsKey(url)) {
                largeImageInfo = mmkv.decodeParcelable(url, LargeImageInfo.class);
            } else {
                largeImageInfo = new LargeImageInfo();
                largeImageInfo.setUrl(url);
            }
            largeImageInfo.setFileSize(size);
            mmkv.encode(url, largeImageInfo);
        }
    }

    /**
     * 此方法提供给四大图片框架拦截使用，此时的图片已经变成了bitmap，除非人工逆向计算，否则得不到
     * 文件的大小，如果是从网络加载，会在前面一个方法得到文件大小，加载到内存中以后，会通过这个方法
     * 补全信息。如果是加载本地的图片，就没办法得到文件大小，后期看看能不能通过逆向人工计算得到。
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/4 8:32
     */
    public void saveImageInfo(final String url, long memorySize, final int width, final int height, String framework,
                              int targetWidth, int targetHeight, Bitmap bitmap) {
        if (memorySize <= 0) {
            return;
        }
        if (LargeImage.getInstance().isLargeImageOpen()) {
            final double size = ConvertUtils.byte2MemorySize(memorySize, ConvertUtils.KB);
            //目前采取的策略是，在网络下载图片时不论文件大小是否超标，都将保存数据
            //在这个方法进行判断，如果超标则保存，没有超标则进行删除
            if (mmkv.containsKey(url)) {
                LargeImageInfo largeImageInfo = mmkv.decodeParcelable(url, LargeImageInfo.class);
                //文件和内存大小其中一个超标则保存
                if (largeImageInfo.getFileSize() > LargeImage.getInstance().getFileSizeThreshold() ||
                        size >= LargeImage.getInstance().getMemorySizeThreshold()) {
                    largeImageInfo.setWidth(width);
                    largeImageInfo.setHeight(height);
                    largeImageInfo.setMemorySize(size);
                    largeImageInfo.setFramework(framework);
                    largeImageInfo.setTargetWidth(targetWidth);
                    largeImageInfo.setTargetHeight(targetHeight);
                    mBitmapCache.put(url,bitmap);
                    mInfoCache.put(url,largeImageInfo);
                    mmkv.encode(url, largeImageInfo);
                } else {
                    //都不超标，则删除
                    mBitmapCache.remove(url);
                    mInfoCache.remove(url);
                    mmkv.remove(url);
                }
            } else {
                //如果从本地加载图片，则没有文件大小数据，第一次加载时也还没存储数据，只能看内存是否超标
                if (size >= LargeImage.getInstance().getMemorySizeThreshold()) {
                    //超过阈值,进行存储
                    LargeImageInfo largeImageInfo = new LargeImageInfo();
                    largeImageInfo.setUrl(url);
                    largeImageInfo.setWidth(width);
                    largeImageInfo.setHeight(height);
                    largeImageInfo.setMemorySize(size);
                    largeImageInfo.setFramework(framework);
                    largeImageInfo.setTargetWidth(targetWidth);
                    largeImageInfo.setTargetHeight(targetHeight);
                    mBitmapCache.put(url,bitmap);
                    mInfoCache.put(url,largeImageInfo);
                    mmkv.encode(url, largeImageInfo);
                }
            }
        }
        if(openDialog) {
            //开启弹窗模式，才显示
            isShwoAlarm(url);
        }
    }

    /**
     * 最后判断文件大小或者内存大小是否有超值
     * 有的话弹出提示框。
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/6 17:22
     */
    private void isShwoAlarm(String url) {
        final LargeImageInfo largeImageInfo = mmkv.decodeParcelable(url, LargeImageInfo.class);
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
                    showDialog(largeImageInfo.getUrl(), largeImageInfo.getWidth(), largeImageInfo.getHeight(),
                            largeImageInfo.getFileSize(), largeImageInfo.getMemorySize(), largeImageInfo.getTargetWidth(),
                            largeImageInfo.getTargetHeight());
                }
            });
        }
    }

    /**
     * 如果是BitmapDrawable类型，调用该方法
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/4 9:17
     */
    public Bitmap transform(String imageUrl, BitmapDrawable bitmapDrawable, String framework, int targetWidth, int targetHeight) {
        Bitmap sourceBitmap = ConvertUtils.drawable2Bitmap(bitmapDrawable);
        return transform(imageUrl, sourceBitmap, framework, targetWidth, targetHeight);
    }

    /**
     * 如果是Bitmap类型调用该方法
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/4 9:17
     */
    public Bitmap transform(String imageUrl, Bitmap sourceBitmap, String framework, int targetWidth, int targetHeight) {
        if (null == sourceBitmap) {
            return null;
        }
        if (LargeImage.getInstance().isLargeImageOpen()) {
            saveImageInfo(imageUrl, sourceBitmap.getByteCount(), sourceBitmap.getWidth(), sourceBitmap.getHeight(), framework,
                    targetWidth, targetHeight, sourceBitmap);
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
    public void showDialog(final String url, int width, int height, double fileSize, double memorySize, int targetWidth,
                           int targetHeigh) {
        //判断当前URL是否已经添加进去，如果已经添加进去，则不进行添加
        if (!mAlarmInfo.containsKey(url)) {
            mAlarmInfo.put(url, false);
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
        TextView tvViewSize = dialogView.findViewById(R.id.tv_view_size);
        TextView tvImageUrl = dialogView.findViewById(R.id.tv_image_url);
        ImageView ivThumb = dialogView.findViewById(R.id.iv_thumb);
        //设置文件大小
        setFileSize(fileSize, llFileSize, tvFileSize, ivFileSize);
        //设置内存大小
        setMemorySize(memorySize, llMemorySize, tvMemorySize, ivMemorySize);
        //设置图片尺寸
        setImageSize(width, height, tvSize);
        //设置View尺寸
        setViewSize(targetWidth, targetHeigh, tvViewSize);
        //设置图片地址
        setImageUrl(url, tvImageUrl);
        //设置图片
        ivThumb.setImageBitmap(mBitmapCache.get(url));
        AlertDialog alertDialog = builder.setTitle("提示").setView(dialogView).setPositiveButton("关闭",
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAlarmInfo.put(url, false);
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
        mAlarmInfo.put(url, true);
    }

    private void setImageUrl(final String url, TextView tvImageUrl) {
        if (TextUtils.isEmpty(url)) {
            tvImageUrl.setVisibility(View.GONE);
        } else {
            tvImageUrl.setVisibility(View.VISIBLE);
            if(!url.startsWith("http") && !url.startsWith("https")){
                String tempUrl = url;
                if(url.contains("/")){
                    int index = url.lastIndexOf("/");
                    tempUrl =url.substring(index+1,url.length());
                }
                try {
                    final String resourceName = LargeImage.APPLICATION.getApplicationContext().getResources().getResourceName(Integer.parseInt(tempUrl));
                    if(TextUtils.isEmpty(resourceName)){
                        tvImageUrl.setText(ResHelper.getString(R.string.large_image_url, "本地图片"));
                    }else {
                        tvImageUrl.setText(ResHelper.getString(R.string.large_image_url, resourceName));
                    }
                    tvImageUrl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            copyToClipboard(resourceName);
                        }
                    });
                } catch (NumberFormatException e){
                    //不是请求网络，也没用resId,统一显示为本地图片
                    e.printStackTrace();
                    tvImageUrl.setText(ResHelper.getString(R.string.large_image_url, "本地图片"));
                }
            }else{
                tvImageUrl.setText(ResHelper.getString(R.string.large_image_url, url));
                tvImageUrl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        copyToClipboard(url);
                    }
                });
            }
        }
    }

    private void copyToClipboard(String resourceName) {
        try {
            //获取剪贴板管理器
            ClipboardManager cm =
                    (ClipboardManager) LargeImage.APPLICATION.getSystemService(Context.CLIPBOARD_SERVICE);
            //创建普通字符型ClipData
            ClipData clipData = ClipData.newPlainText("Label", resourceName);
            cm.setPrimaryClip(clipData);
            Toast.makeText(LargeImage.APPLICATION.getApplicationContext(), "复制成功！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(LargeImage.APPLICATION.getApplicationContext(), "复制失败！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setViewSize(int targetWidth, int targetHeigh, TextView tvViewSize) {
        if (targetWidth <= 0 || targetHeigh <= 0) {
            tvViewSize.setVisibility(View.GONE);
        } else {
            tvViewSize.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            sb.append(targetWidth).append("*").append(targetHeigh);
            tvViewSize.setText(ResHelper.getString(R.string.large_view_size, sb.toString()));
        }
    }

    private void setImageSize(int width, int height, TextView tvSize) {
        if (width <= 0 && height <= 0) {
            tvSize.setVisibility(View.GONE);
        } else {
            tvSize.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            sb.append(width).append("*").append(height);
            tvSize.setText(ResHelper.getString(R.string.large_image_size, sb.toString()));
        }
    }

    private void setMemorySize(double memorySize, LinearLayout llMemorySize, TextView tvMemorySize, ImageView ivMemorySize) {
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
    }

    private void setFileSize(double fileSize, LinearLayout llFileSize, TextView tvFileSize, ImageView ivFileSize) {
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
    }
    /**
     * @param openDialog 是否开启弹窗
     */
    public void setOpenDialog(boolean openDialog) {
        this.openDialog = openDialog;
    }

    public boolean isOpenDialog() {
        return openDialog;
    }

    public Map<String, Bitmap> getmBitmapCache() {
        return mBitmapCache;
    }

    public Map<String, LargeImageInfo> getmInfoCache() {
        return mInfoCache;
    }
}
