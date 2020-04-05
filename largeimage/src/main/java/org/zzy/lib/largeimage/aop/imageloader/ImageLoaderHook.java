package org.zzy.lib.largeimage.aop.imageloader;

import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/3 16:27
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ImageLoaderHook {

    public static ImageLoadingListener  process(ImageLoadingListener listener){
        return new ImageLoaderLargeImageListener(listener);
    }
}
