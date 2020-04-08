package org.zzy.lib.largeimage.aop.fresco;

import android.net.Uri;

import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.request.Postprocessor;

import java.util.HashSet;
import java.util.Set;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/2 22:08
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class FrescoHook {

    public static Postprocessor process(Uri uri, Postprocessor postprocessor,ResizeOptions resizeOptions){
       return new FrescoLargeImageProcessor(postprocessor,uri,resizeOptions);
    }
}
