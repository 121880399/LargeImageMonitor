package org.zzy.imagemonitor;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.facebook.drawee.view.SimpleDraweeView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
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
        //2.测试内存大小超过的图片，测试文件大小超过的图片 预期结果：弹出提示（测试第一次加载和从缓存加载的一致性） 测试结果：通过
//        ImageLoader.getInstance().displayImage("http://desk.fd.zol-img.com.cn/t_s1680x1050/g5/M00/09/03/ChMkJ13I0eyIdyApAAMWAKdhvL4AAvKLwFDZMcAAxYY629.jpg",mIVImageLoader);
        //3.测试本地图片
        ImageLoader.getInstance().displayImage("drawable://"+R.drawable.ic_test_one,mIVImageLoader);
    }

    private void setGlide(){
        //1.测试文件和内存大小未超过的图片，预期结果：不弹出提示 测试结果：通过
//        Glide.with(this).load("https://c-ssl.duitang.com/uploads/item/202004/03/20200403204618_d2Ucc.thumb.300_300_c.jpeg").into(mIVGlide);
        //2.测试内存大小超过的图片，测试文件大小超过的图片 预期结果：弹出提示（测试第一次加载和从缓存加载的一致性） 测试结果：通过
//        Glide.with(this).load("http://desk.fd.zol-img.com.cn/t_s1680x1050/g5/M00/09/03/ChMkJ13I0O-IUxnqAA6O1fF1dWwAAvKJgAYVvEADo7t268.jpg").into(mIVGlide);
        //3.测试本地图片
        Glide.with(this).load(R.drawable.ic_test_two).into(mIVGlide);
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_test_three);
//        Glide.with(this).load(bitmap).into(mIVGlide);

    }

    private void setFresco(){
        //1.测试文件和内存大小未超过的图片，预期结果：不弹出提示 测试结果：通过
//        Uri uri = Uri.parse("https://c-ssl.duitang.com/uploads/item/202004/03/20200403204602_5zHGC.thumb.300_300_c.jpeg");
        //2.测试内存大小超过的图片，测试文件大小超过的图片 预期结果：弹出提示（测试第一次加载和从缓存加载的一致性） 测试结果：通过
//        Uri uri = Uri.parse("https://desk-fd.zol-img.com.cn/t_s960x600c5/g5/M00/09/03/ChMkJl3I0dmIAiyXABDF46MVj1IAAvKLgFJsZUAEMX7384.jpg");
        //3.测试本地图片
        Uri uri = Uri.parse("res://"+"drawable/"+R.drawable.ic_test_three);
        mIVFresco.setImageURI(uri);
    }

    private void setPicasso(){
        //1.测试文件和内存大小未超过的图片，预期结果：不弹出提示 测试结果：通过
//        Picasso.get().load("https://c-ssl.duitang.com/uploads/item/202004/03/20200403204520_H2cti.thumb.300_300_c.jpeg").into(mIVPicasso);
        //2.测试内存大小超过的图片，测试文件大小超过的图片 预期结果：弹出提示（测试第一次加载和从缓存加载的一致性） 测试结果：通过
//        Picasso.get().load("https://desk-fd.zol-img.com.cn/t_s960x600c5/g5/M00/0B/0D/ChMkJ1vfv8CIbx2-AAYDrq1u-J8AAs9xAHJAuMABgPG789.jpg").into(mIVPicasso);
        //3.测试本地图片
        Picasso.get().load(R.drawable.ic_test_four).into(mIVPicasso);


    }


}
