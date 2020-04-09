package org.zzy.imagemonitor;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.zzy.lib.largeimage.LargeImage;

import java.io.File;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/3 11:16
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化大图监控
        LargeImage.getInstance().install(this).setFileSizeThreshold(400.0).setMemorySizeThreshold(100);
        Fresco.initialize(this);

    }
}
