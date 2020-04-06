package org.zzy.lib.largeimage.aop.imageloader;

import android.graphics.Bitmap;
import android.view.View;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.zzy.lib.largeimage.LargeImageManager;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/3 16:37
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ImageLoaderLargeImageListener implements ImageLoadingListener {

    /**
     * 原始Listener
     */
    private ImageLoadingListener mOriginListener;

    public ImageLoaderLargeImageListener(ImageLoadingListener listener){
        if(null == listener){
            mOriginListener = new SimpleImageLoadingListener();
        }else {
            mOriginListener = listener;
        }
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {
        mOriginListener.onLoadingStarted(imageUri,view);
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        mOriginListener.onLoadingFailed(imageUri,view,failReason);
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        LargeImageManager.getInstance().transform(imageUri,loadedImage,"ImageLoader");
        mOriginListener.onLoadingComplete(imageUri, view, loadedImage);
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {
        mOriginListener.onLoadingCancelled(imageUri, view);
    }
}
