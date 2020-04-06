package org.zzy.imagemonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.facebook.drawee.view.SimpleDraweeView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private ImageView mIVImageLoader;
    private ImageView mIVGlide;
    private SimpleDraweeView mIVFresco;
    private ImageView mIVPicasso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView(){
        mIVImageLoader = findViewById(R.id.iv_imageloader);
        mIVGlide = findViewById(R.id.iv_glide);
        mIVFresco = (SimpleDraweeView)findViewById(R.id.iv_fresco);
        mIVPicasso = findViewById(R.id.iv_picasso);
    }

    private void initData(){
        setImageLoader();
        setGlide();
        setFresco();
        setPicasso();
    }

    private void setImageLoader(){
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        //1.测试文件和内存大小未超过的图片，预期结果：不弹出提示 测试结果：通过
//        ImageLoader.getInstance().displayImage("https://c-ssl.duitang.com/uploads/item/202004/03/20200403204640_mNNyA.thumb.300_300_c.jpeg",mIVImageLoader);
        ImageLoader.getInstance().displayImage("http://desk.fd.zol-img.com.cn/t_s1680x1050/g5/M00/09/03/ChMkJ13I0O-IUxnqAA6O1fF1dWwAAvKJgAYVvEADo7t268.jpg",mIVImageLoader);
        //2.测试内存大小超过的图片
    }

    private void setGlide(){
        //1.测试文件和内存大小未超过的图片，预期结果：不弹出提示 测试结果：通过
//        Glide.with(this).load("https://c-ssl.duitang.com/uploads/item/202004/03/20200403204618_d2Ucc.thumb.300_300_c.jpeg").into(mIVGlide);
        Glide.with(this).load("https://c-ssl.duitang.com/uploads/item/202004/03/20200403204618_d2Ucc.thumb.300_300_c.jpeg").into(mIVGlide);
        //2.测试内存大小超过的图片
        //3.测试文件大小超过的图片
        //4.测试本地图片
    }

    private void setFresco(){
        //1.测试文件和内存大小未超过的图片，预期结果：不弹出提示 测试结果：通过
//        Uri uri = Uri.parse("https://c-ssl.duitang.com/uploads/item/202004/03/20200403204602_5zHGC.thumb.300_300_c.jpeg");
        Uri uri = Uri.parse("https://c-ssl.duitang.com/uploads/item/202004/03/20200403204602_5zHGC.thumb.300_300_c.jpeg");
        //2.测试内存大小超过的图片
        mIVFresco.setAspectRatio(0.5f);
        mIVFresco.setImageURI(uri);
    }

    private void setPicasso(){
        //1.测试文件和内存大小未超过的图片，预期结果：不弹出提示 测试结果：通过
//        Picasso.get().load("https://c-ssl.duitang.com/uploads/item/202004/03/20200403204520_H2cti.thumb.300_300_c.jpeg").into(mIVPicasso);
        Picasso.get().load("https://c-ssl.duitang.com/uploads/item/202004/03/20200403204520_H2cti.thumb.300_300_c.jpeg").into(mIVPicasso);
        //2.测试内存大小超过的图片
    }


}
