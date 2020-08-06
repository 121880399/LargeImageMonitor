package org.zzy.largeimage.utils;

import java.io.File;
import java.math.BigDecimal;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/8/6 16:54
 * 描    述： 图片工具类
 * 修订历史：
 * ================================================
 */
public class ImageUtil {

    public static final String JPG = ".jpg";
    public static final String PGN = ".png";
    public static final String JPEG = ".jpeg";
    public static final String DOT_9PNG = ".9.png";

    /**
    * 判断是否是图片文件
    * 作者: ZhouZhengyi
    * 创建时间: 2020/8/6 21:43
    */
    public static boolean isImageFile(File file){
        return (file.getName().endsWith(JPG)
                || file.getName().endsWith(PGN)
                || file.getName().endsWith(JPEG)
                || file.getName().endsWith(DOT_9PNG));
    }


    /**
    * 判断是否是大图文件
     * @param threshold 图片文件的阈值，单位为KB
    * 作者: ZhouZhengyi
    * 创建时间: 2020/8/6 22:05
    */
    public static boolean isLargeImageFile(File imgFile,double threshold){
        if(isImageFile(imgFile)){
            if(imgFile.length() >= (threshold*1024)){
                return true;
            }
        }
        return false;
    }

}
