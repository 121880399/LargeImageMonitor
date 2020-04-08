package org.zzy.lib.largeimage;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/4 8:09
 * 描    述：大图的信息
 * 修订历史：
 * ================================================
 */
public class LargeImageInfo implements Parcelable {

    /**
     * 图片地址
     */
    private String url;

    /**
     * 图片文件大小
     */
    private double fileSize;

    /**
     * 图片所占内存大小
     */
    private double memorySize;

    /**
     * 图片宽
     */
    private int width;

    /**
     * 图片高
     */
    private int height;

    /**
     * 加载图片所使用的框架
     */
    private String framework;

    /**
     * View的宽
     */
    private int targetWidth;
    /**
     * View的高
     */
    private int targetHeight;


    public int getTargetWidth() {
        return targetWidth;
    }

    public void setTargetWidth(int targetWidth) {
        this.targetWidth = targetWidth;
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public void setTargetHeight(int targetHeight) {
        this.targetHeight = targetHeight;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getFileSize() {
        return fileSize;
    }

    public void setFileSize(double fileSize) {
        this.fileSize = fileSize;
    }

    public double getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(double memorySize) {
        this.memorySize = memorySize;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public LargeImageInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeDouble(this.fileSize);
        dest.writeDouble(this.memorySize);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeString(this.framework);
        dest.writeInt(this.targetWidth);
        dest.writeInt(this.targetHeight);
    }

    protected LargeImageInfo(Parcel in) {
        this.url = in.readString();
        this.fileSize = in.readDouble();
        this.memorySize = in.readDouble();
        this.width = in.readInt();
        this.height = in.readInt();
        this.framework = in.readString();
        this.targetWidth = in.readInt();
        this.targetHeight = in.readInt();
    }

    public static final Creator<LargeImageInfo> CREATOR = new Creator<LargeImageInfo>() {
        @Override
        public LargeImageInfo createFromParcel(Parcel source) {
            return new LargeImageInfo(source);
        }

        @Override
        public LargeImageInfo[] newArray(int size) {
            return new LargeImageInfo[size];
        }
    };
}
