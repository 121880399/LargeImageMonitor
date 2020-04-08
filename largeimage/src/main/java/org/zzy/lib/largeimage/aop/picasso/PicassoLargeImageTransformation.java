package org.zzy.lib.largeimage.aop.picasso;

import android.graphics.Bitmap;
import android.net.Uri;

import com.squareup.picasso.Transformation;

import org.zzy.lib.largeimage.LargeImageManager;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/2 11:33
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class PicassoLargeImageTransformation implements Transformation {

    private String mUri;
    private int targetWidth;
    private int targetHeight;

    public PicassoLargeImageTransformation(Uri uri,int resourceId,int targetWidth, int targetHeight){
        if(uri == null){
            mUri = String.valueOf(resourceId);
        }else{
            mUri = uri.toString();
        }
        this.targetWidth=targetWidth;
        this.targetHeight=targetHeight;
    }

    @Override
    public Bitmap transform(Bitmap source) {
       return LargeImageManager.getInstance().transform(mUri,source,"Picasso",targetWidth,targetHeight);
    }

    @Override
    public String key() {
        return "LargeImage&Picasso&PicassoLargeImageTransformation";
    }
}
