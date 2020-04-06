package org.zzy.lib.largeimage.aop.fresco;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;

import com.facebook.cache.common.CacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.nativecode.Bitmaps;
import com.facebook.imagepipeline.request.Postprocessor;

import org.zzy.lib.largeimage.LargeImageManager;

import static com.facebook.imagepipeline.request.BasePostprocessor.FALLBACK_BITMAP_CONFIGURATION;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/4 10:35
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class FrescoLargeImageProcessor implements Postprocessor {

    private Postprocessor mOriginProcessor;
    private Uri mUri;

    public FrescoLargeImageProcessor(Postprocessor mOriginProcessor, Uri mUri) {
        this.mOriginProcessor = mOriginProcessor;
        this.mUri = mUri;
    }

    @Override
    public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
        sourceBitmap = LargeImageManager.getInstance().transform(mUri.toString(),sourceBitmap,"Fresco");
        if(null != mOriginProcessor){
            return mOriginProcessor.process(sourceBitmap,bitmapFactory);
        }
        final Bitmap.Config sourceBitmapConfig = sourceBitmap.getConfig();
        CloseableReference<Bitmap> destBitmapRef =
                bitmapFactory.createBitmapInternal(
                        sourceBitmap.getWidth(),
                        sourceBitmap.getHeight(),
                        sourceBitmapConfig != null ? sourceBitmapConfig : FALLBACK_BITMAP_CONFIGURATION);
        try {
            process(destBitmapRef.get(),sourceBitmap);
            return CloseableReference.cloneOrNull(destBitmapRef);
        }finally {
            CloseableReference.closeSafely(destBitmapRef);
        }
    }

    public void process(Bitmap destBitmap, Bitmap sourceBitmap) {
        internalCopyBitmap(destBitmap, sourceBitmap);
        process(destBitmap);
    }

    public void process(Bitmap bitmap) {
    }

    private static void internalCopyBitmap(Bitmap destBitmap,Bitmap sourceBitmap){
        if(destBitmap.getConfig() == sourceBitmap.getConfig()){
            Bitmaps.copyBitmap(destBitmap, sourceBitmap);
        }else{
            Canvas canvas = new Canvas(destBitmap);
            canvas.drawBitmap(sourceBitmap,0,0,null);
        }
    }

    @Override
    public String getName() {
        if(null != mOriginProcessor){
            return mOriginProcessor.getName();
        }
        return "LargeImage&Fresco&FrescoLargeImageProcessor";
    }

    @Override
    public CacheKey getPostprocessorCacheKey() {
        if(null != mOriginProcessor){
            return mOriginProcessor.getPostprocessorCacheKey();
        }
        return null;
    }
}
