package org.zzy.lib.largeimage;


import android.net.Uri;

import com.squareup.picasso.Transformation;

import org.zzy.lib.largeimage.aop.picasso.PicassoHook;

import java.util.List;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/1 19:14
 * 描    述：用来测试生成ASM码
 * 修订历史：
 * ================================================
 */
public class Test {
//    private List<RequestListener> requestListeners;
    //模拟glide
//    private void init(){
//        requestListeners=GlideHook.process(requestListeners);
//    }

    //模拟picasso
//    private void init(Uri uri, List<Transformation> transformations, int resourceId, int targetWidth, int targetHeight){
//        transformations = PicassoHook.process(uri,transformations,resourceId,targetWidth,targetHeight);
//    }

    //模拟fresco
//    private void init(ImageRequestBuilder builder){
//        builder.setPostprocessor(FrescoHook.process(builder.getSourceUri(),builder.getPostprocessor(),builder.getResizeOptions()));
//    }
//
//    public static class Builder {
//        private int a;
//        private Set<com.facebook.imagepipeline.listener.RequestListener> mRequestListeners;
//    }

    //模拟imageloader
//    public void displayImage(String uri, ImageAware imageAware, DisplayImageOptions options,
//                             ImageSize targetSize, ImageLoadingListener listener, ImageLoadingProgressListener progressListener) {
//        listener = ImageLoaderHook.process(listener);
//    }
//    final List<Interceptor> interceptors = new ArrayList<>();
//    final List<Interceptor> networkInterceptors = new ArrayList<>();
//
//    private void init(){
//        interceptors.addAll(LargeImage.getInstance().getOkHttpInterceptors());
//        networkInterceptors.addAll(LargeImage.getInstance().getOkHttpNetworkInterceptors());
//    }
//    Dns dns;
//    public void openConnection() {
//        dns = LargeImage.getInstance().getDns();
//    }


}
