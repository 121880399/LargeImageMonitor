package org.zzy.lib.largeimage.aop.glide;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;

import org.zzy.lib.largeimage.LargeImageManager;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/1 19:55
 * 描    述：当资源加载完成后，会回调这个类里面的onResourceReady方法
 * 在里面编写自己的逻辑
 * 修订历史：
 * ================================================
 */
public class GlideLargeImageListener<R> implements RequestListener<R> {


    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<R> target, boolean isFirstResource) {
        return false;
    }

    @Override
    public boolean onResourceReady(R resource, Object model, Target<R> target, DataSource dataSource, boolean isFirstResource) {
        int width =0;
        int heigh =0;
        if(target instanceof ViewTarget){
            View view = ((ViewTarget) target).getView();
            width = view.getWidth();
            heigh = view.getHeight();
        }
        if(resource instanceof Bitmap){
            LargeImageManager.getInstance().transform(model.toString(),(Bitmap)resource,"Glide",width,heigh);
        }else if(resource instanceof BitmapDrawable){
            LargeImageManager.getInstance().transform(model.toString(),(BitmapDrawable) resource,"Glide",width,heigh);
        }
        return false;
    }
}
