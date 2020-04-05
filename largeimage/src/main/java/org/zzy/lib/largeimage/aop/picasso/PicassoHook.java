package org.zzy.lib.largeimage.aop.picasso;

import android.net.Uri;

import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/2 10:47
 * 描    述：将process方法注入到
 * com.squareup.picasso.Request的构造方法中
 * 修订历史：
 * ================================================
 */
public class PicassoHook {

    public static List<Transformation> process(Uri uri, List<Transformation> transformations){
        if(null == transformations){
            transformations = new ArrayList<>();
        }
        transformations.add(new PicassoLargeImageTransformation(uri));
        return transformations;
    }
}
