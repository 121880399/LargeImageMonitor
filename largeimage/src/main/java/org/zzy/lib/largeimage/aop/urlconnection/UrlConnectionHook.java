package org.zzy.lib.largeimage.aop.urlconnection;

import org.zzy.lib.largeimage.aop.okhttp.LargeImageInterceptor;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/5 16:36
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class UrlConnectionHook {

    public static URLConnection process (URLConnection urlConnection){
        try {
            String host = HttpUrl.parse(urlConnection.getURL().toString()).host();
            return createOkHttpUrlConnection(urlConnection);
        }catch (Exception e){
            e.printStackTrace();
        }
        return urlConnection;
    }

    /**
    * 构建一个OkhttpClient，并且判断当前是http还是https，返回相应的OkhttpUrlConnection
     * ObsoleteUrlFactory类在OKhttp3.14版本开始就被删掉了，不是因为有什么问题，只是不推荐使用了
    * 作者: ZhouZhengyi
    * 创建时间: 2020/4/5 20:47
    */
    private static URLConnection createOkHttpUrlConnection(URLConnection urlConnection) throws MalformedURLException {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        addInterceptor(builder);
        OkHttpClient client = builder.build();

        String strUrl = urlConnection.getURL().toString();
        URL url = new URL(strUrl);
        String protocol = url.getProtocol().toLowerCase();
        if(protocol.equalsIgnoreCase("http")){
            return  new ObsoleteUrlFactory.OkHttpURLConnection(url,client);
        }
        if (protocol.equalsIgnoreCase("https")){
            return new ObsoleteUrlFactory.OkHttpsURLConnection(url,client);
        }
        return urlConnection;
    }

    private static void addInterceptor(OkHttpClient.Builder builder) {
        //判断当前是否已经添加过全局的Okhttp拦截器，如果添加了就返回
        for (Interceptor interceptor : builder.interceptors()) {
            if(interceptor instanceof LargeImageInterceptor){
                return;
            }
        }
        builder.addInterceptor(new LargeImageInterceptor());
    }
}
