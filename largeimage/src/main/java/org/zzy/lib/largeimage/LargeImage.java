package org.zzy.lib.largeimage;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import org.zzy.lib.largeimage.aop.okhttp.LargeImageInterceptor;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Dns;
import okhttp3.Interceptor;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/4 20:58
 * 描    述：门面类，主要用于初始化和配置各项参数
 * 修订历史：
 * ================================================
 */
public class LargeImage {
    public static String TAG = "LargeImageMonitor";

    public static Application APPLICATION;

    /**
     * 文件大小阈值 kb为单位
     */
    private double fileSizeThreshold = 500.0;
    /**
     * 内存大小阈值 kb为单位
     */
    private double memorySizeThreshold = 800.0;

    /**
     * 大图监控开关,这里只是存储数据的开关
     * 是否进行字节码修改需要在gradle文件中对插件进行设置
     */
    private boolean largeImageOpen = true;

    /**
     * Okhttp应用拦截器
     */
    private List<Interceptor> okHttpInterceptors = new ArrayList<>();
    /**
     * Okhttp网络拦截器
     */
    private List<Interceptor> okHttpNetworkInterceptors = new ArrayList<>();

    /**
     * 自定义DNS，实现全局HttpDNS
     */
    private Dns mDns = Dns.SYSTEM;

    private LargeImage(){}

    private static class Holder{
        private static LargeImage INSTANCE = new LargeImage();
    }

    public static LargeImage getInstance(){
        return Holder.INSTANCE;
    }

    public LargeImage install(Application app){
        APPLICATION = app;
        //默认添加拦截大图
        okHttpInterceptors.add(new LargeImageInterceptor());
        return this;
    }

    /**
     * 设置文件阈值 单位为kb
     * @param fileSizeThreshold 文件阈值 单位kb
     * @return
     */
    public LargeImage setFileSizeThreshold(double fileSizeThreshold) {
        this.fileSizeThreshold = fileSizeThreshold;
        return this;
    }

    /**
     * 设置内存阈值 单位为kb
     * @param memorySizeThreshold 内存阈值 单位为kb
     * @return
     */
    public LargeImage setMemorySizeThreshold(double memorySizeThreshold) {
        this.memorySizeThreshold = memorySizeThreshold;
        return this;
    }

    /**
     * 设置大图监控开关 true为开 false为关
     */
    public LargeImage setLargeImageOpen(boolean largeImageOpen) {
        this.largeImageOpen = largeImageOpen;
        return this;
    }

    /**
     * 因为实现了Okhttp的全局插桩，所以提供一个可以添加拦截器的方法
     * 让用户可以自定义拦截器实现自己项目和三方库的OKhttp全局监听
     * @param interceptor 应用拦截器
     */
    public LargeImage addOkHttpInterceptor(Interceptor interceptor){
        if(null != okHttpInterceptors){
            okHttpInterceptors.add(interceptor);
        }
        return this;
    }

    /**
     * 添加Okhttp网络拦截器
     * @param networkInterceptor 网络拦截器
     */
    public LargeImage addOkHttpNetworkInterceptor(Interceptor networkInterceptor){
        if(null != okHttpNetworkInterceptors) {
            okHttpNetworkInterceptors.add(networkInterceptor);
        }
        return this;
    }

    /**
     * 可以指定自己定义的HTTPDNS
     * @param dns 自定义DNS
     * @return
     */
    public LargeImage setDns(Dns dns){
        this.mDns = dns;
        return this;
    }

    public Dns getDns(){
        return mDns;
    }

    public List<Interceptor> getOkHttpInterceptors() {
        return okHttpInterceptors;
    }

    public List<Interceptor> getOkHttpNetworkInterceptors() {
        return okHttpNetworkInterceptors;
    }

    public double getFileSizeThreshold() {
        return fileSizeThreshold;
    }

    public double getMemorySizeThreshold() {
        return memorySizeThreshold;
    }

    public boolean isLargeImageOpen() {
        return largeImageOpen;
    }
}
