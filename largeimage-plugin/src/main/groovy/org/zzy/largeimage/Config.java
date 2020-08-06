package org.zzy.largeimage;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/5 21:50
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class Config {
    public static String TAG = "LargeImageMonitor";
    /**
     * 大图检测插件的开关
     */
    private boolean largeImagePluginSwitch = true;

    /**
     * 是否开启自动压缩图片，如果没有开启
     * convertTowebp与openMultiThread属性无效
     */
    private boolean openAutoCompress = false;

    /**
     * 是否转换为webp格式
     */
    private boolean convertToWebp = false;

    /**
     * 是否开启多线程压缩
     */
    private boolean openMultiThread = true;

    /**
     * 白名单
     */
    private List<String> whiteList = new ArrayList<>();

    /**
     * 开启自动压缩图片后 文件大小阈值 kb为单位
     */
    private double fileSizeThreshold = 500.0;

    private Config(){}

    private static class Holder{
        private static Config INSTANCE = new Config();
    }

    public static Config getInstance(){
        return Holder.INSTANCE;
    }

    public boolean largeImagePluginSwitch() {
        return largeImagePluginSwitch;
    }

    public List<String> getWhiteList(){
        return whiteList;
    }

    public double getFileSizeThreshold(){
        return fileSizeThreshold;
    }

    public boolean isOpenAutoCompress(){
        return openAutoCompress;
    }


    public void init(LargeImageExtension extension){
        if(null != extension){
            this.largeImagePluginSwitch = extension.largeImagePluginSwitch;
            this.openAutoCompress = extension.openAutoCompress;
            this.convertToWebp = extension.convertToWebp;
            this.openMultiThread = extension.openMultiThread;
            this.whiteList = extension.whiteList;
            this.fileSizeThreshold = extension.fileSizeThreshold;
        }
    }
}
