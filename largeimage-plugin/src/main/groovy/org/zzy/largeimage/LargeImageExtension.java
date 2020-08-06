package org.zzy.largeimage;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/5 21:24
 * 描    述：JAVABean类，对应gradle中配置的参数
 * 属性必须为public类型，否则无法赋值
 * 修订历史：
 * ================================================
 */
public class LargeImageExtension {
    /**
     * 大图检测插件的开关
     */
    public boolean largeImagePluginSwitch = true;

    /**
     * 是否开启自动压缩图片，如果没有开启
     * convertTowebp与openMultiThread属性无效
     */
    public boolean openAutoCompress = false;

    /**
     * 是否转换为webp格式
     */
    public boolean convertToWebp = false;

    /**
     * 是否开启多线程压缩
     */
    public boolean openMultiThread = true;

    /**
     * 白名单上面的图片不进行自动压缩
     */
    public List<String> whiteList = new ArrayList<>();

    /**
     * 开启自动压缩图片后 文件大小阈值 kb为单位
     */
    public double fileSizeThreshold = 500.0;

}
