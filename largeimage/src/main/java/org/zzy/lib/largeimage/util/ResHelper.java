package org.zzy.lib.largeimage.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import android.text.Html;
import android.text.TextUtils;


import androidx.annotation.ArrayRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.RawRes;
import androidx.annotation.StringRes;

import org.zzy.lib.largeimage.LargeImage;
import org.zzy.lib.largeimage.R;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author : Zhouzhengyi
 */

public class ResHelper {
    /**
     * 缓存区大小
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * 获取全局的Context
     *
     * @return Context
     */
    public static Context context() {
        return LargeImage.APPLICATION.getApplicationContext();
    }

    /**
     * 获取字符串资源
     *
     * @param resId 字符串资源ID
     * @return 字符串
     */
    public static String getString(@StringRes int resId) {
        Resources resources = LargeImage.APPLICATION.getApplicationContext().getResources();
        if (null == resources || 0 == resId) {
            return "";
        }
        return resources.getString(resId);
    }

    /**
     * 获取字符串资源
     *
     * @param resId 字符串资源ID
     * @param formatArgs 格式化
     * @return 字符串
     */
    public static String getString(@StringRes int resId, Object... formatArgs) {
        Resources resources = LargeImage.APPLICATION.getApplicationContext().getResources();
        if (null == resources || 0 == resId || null == formatArgs) {
            return "";
        }
        return resources.getString(resId, formatArgs);
    }

    /**
     * 获取颜色资源
     *
     * @param resId 字颜色资源ID
     * @return 颜色值
     */
    public static int getColor(@ColorRes int resId) {
        Resources resources = LargeImage.APPLICATION.getApplicationContext().getResources();
        if (null == resources || 0 == resId) {
            resId = R.color.c999999;
        }
        return resources.getColor(resId);
    }

    /**
     * 获取颜色状态集合
     *
     * @param resId 颜色集合资源ID
     * @return 颜色集合
     */
    public static ColorStateList getColorStateList(@ColorRes int resId) {
        Resources resources = LargeImage.APPLICATION.getApplicationContext().getResources();
        if (null == resources || 0 == resId) {
            resId = R.color.c999999;
        }
        return resources.getColorStateList(resId);
    }

    /**
     * 获取Dimension
     *
     * @param resId Dimen资源ID
     * @return Dimen
     */
    public static int getDimensionPixelOffset(@DimenRes int resId) {
        Resources resources = LargeImage.APPLICATION.getApplicationContext().getResources();
        if (null == resources || 0 == resId) {
            return 0;
        }
        return resources.getDimensionPixelOffset(resId);
    }

    /**
     * 获取drawable资源
     *
     * @param resId drawable资源ID
     * @return Drawable
     */
    public static Drawable getDrawable(@DrawableRes int resId) {
        Resources resources = LargeImage.APPLICATION.getApplicationContext().getResources();
        if (null == resources || 0 == resId) {
            return null;
        }
        return resources.getDrawable(resId);
    }

    /**
     * 获取字符串数组资源
     *
     * @param resId 字符串数组资源ID
     * @return 字符串数组
     */
    public static String[] getStringArray(@ArrayRes int resId) {
        Resources resources = LargeImage.APPLICATION.getApplicationContext().getResources();
        if (null == resources || 0 == resId) {
            return new String[]{};
        }
        return resources.getStringArray(resId);
    }


    /**
     * 从assets 中读取字符串
     *
     * @param assetsFilePath assets 文件路径
     * @return 内容
     */
    public static String readAssetsString(final String assetsFilePath) {
        InputStream is;
        try {
            is = context().getAssets().open(assetsFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        byte[] bytes = is2Bytes(is);
        if (bytes == null) { return null; }
        return new String(bytes);
    }


    /**
     * 拷贝 raw 下的文件
     *
     * @param resId 资源ID
     * @param destFilePath 目标文件路径
     * @return {@code true}: 成功<br>{@code false}: 失败
     */
    public static boolean copyFileFromRaw(@RawRes final int resId, final String destFilePath) {
        Resources resources = LargeImage.APPLICATION.getApplicationContext().getResources();
        return writeFileFromIS(destFilePath, resources.openRawResource(resId), false
        );
    }

    /**
     * 读取 raw 中的字符串
     *
     * @param resId 资源ID
     * @return 字符串内容
     */
    public static String readRawString(@RawRes final int resId) {
        Resources resources = LargeImage.APPLICATION.getApplicationContext().getResources();
        InputStream is = resources.openRawResource(resId);
        byte[] bytes = is2Bytes(is);
        if (bytes == null) { return null; }
        return new String(bytes);
    }

    /**
     * 从 InputStream 写入文件
     *
     * @param filePath 文件路径
     * @param is InputStream 输入流
     * @param append 是否追加写入
     * @return 写文件结果
     */
    private static boolean writeFileFromIS(final String filePath, InputStream is, boolean append) {
        return writeFileFromIS(getFileByPath(filePath), is, append);
    }

    /**
     * 从 InputStream 写入文件
     *
     * @param file 文件
     * @param is InputStream 输入流
     * @param append 是否追加写入
     * @return 写文件结果
     */
    private static boolean writeFileFromIS(final File file, final InputStream is, final boolean append) {
        if (!createOrExistsFile(file) || is == null) { return false; }
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, append));
            byte[] data = new byte[BUFFER_SIZE];
            int len;
            while ((len = is.read(data, 0, BUFFER_SIZE)) != -1) {
                os.write(data, 0, len);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建或者已存在的文件
     *
     * @param file 文件
     * @return true 创建成功或者是文件已存在
     */
    private static boolean createOrExistsFile(File file) {
        if (file == null) { return false; }
        if (file.exists()) { return file.isFile(); }
        if (!createOrExistsDir(file.getParentFile())) { return false; }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 创建或者已存在的文件目录
     *
     * @param file 文件
     * @return true 创建成功或者是文件已存在
     */
    private static boolean createOrExistsDir(File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 通过路径获取文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    private static File getFileByPath(String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    /**
     * 是否是空
     *
     * @param s 字符串
     * @return true 空，false 非空
     */
    private static boolean isSpace(String s) {
        if (s == null) { return true; }
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否是字节数组
     *
     * @param is InputStream 输入流
     * @return 字节数组
     */
    private static byte[] is2Bytes(final InputStream is) {
        if (is == null) { return null; }
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            byte[] b = new byte[BUFFER_SIZE];
            int len;
            while ((len = is.read(b, 0, BUFFER_SIZE)) != -1) {
                os.write(b, 0, len);
            }
            return os.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * @param content 内容
     * @param mode 模式
     *             Html.FROM_HTML_MODE_COMPACT：html块元素之间使用一个换行符分隔
     *             Html.FROM_HTML_MODE_LEGACY：html块元素之间使用两个换行符分隔
     */
    public static CharSequence fromHtml(String content, int mode){
        if(TextUtils.isEmpty(content)){
            return "";
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(content,mode);
        } else {
            return Html.fromHtml(content);
        }
    }
}
