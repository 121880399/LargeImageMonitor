package org.zzy.lib.largeimage.aop.glide;

import com.bumptech.glide.request.RequestListener;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/1 16:30
 * 描    述：将该类的process方法注入到
 * com.bumptech.glide.request.SingleRequest的构造方法中
 * 修订历史：
 * ================================================
 */
public class GlideHook  {

    public static List<RequestListener> process(List<RequestListener> requestListener){
        if(requestListener == null ){
            requestListener = new ArrayList<>();
        }
        requestListener.add(new GlideLargeImageListener());
        return requestListener;
    }
}
