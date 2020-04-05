package org.zzy.lib.largeimage.aop.okhttp;

import android.text.TextUtils;

import org.zzy.lib.largeimage.LargeImageManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/5 8:43
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class LargeImageInterceptor implements Interceptor {


    @Override
    public Response intercept(Chain chain) throws IOException {
        //发出请求时不拦截
        Request request = chain.request();
        Response response = chain.proceed(request);
        //拦截响应
        String header = response.header("Content-Type");
        //如果是图片类型则拦截
        if(isImage(header)){
            process(response);
        }
        return response;
    }

    private void process(Response response){
        String header = response.header("Content-Length");
        //判断是否有数据
        if(!TextUtils.isEmpty(header)){
            try {
                //parseInt有可能抛出异常
                LargeImageManager.getInstance().saveImageInfo(response.request().url().toString(), Integer.parseInt(header));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
    * 判断是否是图片
    * 作者: ZhouZhengyi
    * 创建时间: 2020/4/5 9:45
    */
    private boolean isImage(String contentType){
        String mimeType = stripContentExtras(contentType);
        if(mimeType.startsWith("image")){
            return true;
        }else{
            return false;
        }
    }

    private String stripContentExtras(String contentType){
        int index = contentType.indexOf(';');
        return (index >= 0) ? contentType.substring(0, index) : contentType;
    }
}
