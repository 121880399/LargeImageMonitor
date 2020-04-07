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

    /**
    * 629-文件:246kb -内存:6m 789-文件:145kb-内存:562kb  7384-文件:178kb-内存:2M  268-文件：523kb-内存：249kb
     * 目前存在问题：每个框架采用的编码格式不一样，而在显示弹框的时候采用的是picaso，这时候如果数据不一样会更新数据
     * 在线上也会出现这样的情况，因为就算能知道客户使用的框架，也不一定能拿到该框架使用的编码格式
    * 作者: ZhouZhengyi
    * 创建时间: 2020/4/7 0:20
    */
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
        //2.测试内存大小超过的图片
        //3.测试文件大小超过的图片
        ImageLoader.getInstance().displayImage("http://desk.fd.zol-img.com.cn/t_s1680x1050/g5/M00/09/03/ChMkJ13I0eyIdyApAAMWAKdhvL4AAvKLwFDZMcAAxYY629.jpg",mIVImageLoader);
    }

    private void setGlide(){
        //1.测试文件和内存大小未超过的图片，预期结果：不弹出提示 测试结果：通过
//        Glide.with(this).load("https://c-ssl.duitang.com/uploads/item/202004/03/20200403204618_d2Ucc.thumb.300_300_c.jpeg").into(mIVGlide);
        //2.测试内存大小超过的图片
        Glide.with(this).load("http://desk.fd.zol-img.com.cn/t_s1680x1050/g5/M00/09/03/ChMkJ13I0O-IUxnqAA6O1fF1dWwAAvKJgAYVvEADo7t268.jpg").into(mIVGlide);
        //3.测试文件大小超过的图片
        //目前的情况是，如果从缓存中加载图片，且内存不超过阈值，则不会报警
        //4.测试本地图片
    }

    private void setFresco(){
        //1.测试文件和内存大小未超过的图片，预期结果：不弹出提示 测试结果：通过
//        Uri uri = Uri.parse("https://c-ssl.duitang.com/uploads/item/202004/03/20200403204602_5zHGC.thumb.300_300_c.jpeg");
        //2.测试内存大小超过的图片
        //3.测试文件大小超过的图片
        Uri uri = Uri.parse("https://desk-fd.zol-img.com.cn/t_s960x600c5/g5/M00/09/03/ChMkJl3I0dmIAiyXABDF46MVj1IAAvKLgFJsZUAEMX7384.jpg");
        mIVFresco.setAspectRatio(1f);
        mIVFresco.setImageURI(uri);
    }

    private void setPicasso(){
        //1.测试文件和内存大小未超过的图片，预期结果：不弹出提示 测试结果：通过
//        Picasso.get().load("https://c-ssl.duitang.com/uploads/item/202004/03/20200403204520_H2cti.thumb.300_300_c.jpeg").into(mIVPicasso);
        //2.测试内存大小超过的图片
        Picasso.get().load("https://desk-fd.zol-img.com.cn/t_s960x600c5/g5/M00/0B/0D/ChMkJ1vfv8CIbx2-AAYDrq1u-J8AAs9xAHJAuMABgPG789.jpg").into(mIVPicasso);
        //3.测试文件大小超过的图片

    }


}
