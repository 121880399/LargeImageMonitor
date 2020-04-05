package org.zzy.imagemonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class MainActivity extends AppCompatActivity {

    private ImageView mIVImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView(){
        mIVImageLoader = findViewById(R.id.iv_imageloader);
    }

    private void initData(){
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                // 设置图片在下载期间显示的图片
                .showImageOnLoading(R.drawable.ic_launcher_background)
                // 设置图片Uri为空或是错误的时候显示的图片
                .showImageForEmptyUri(R.drawable.ic_launcher_background)
                // 设置图片加载/解码过程中错误时候显示的图片
                .showImageOnFail(R.drawable.ic_launcher_background)
                // 设置下载的图片是否缓存在内存中
                .cacheInMemory(true)
                // 设置下载的图片是否缓存在SD卡中
                .cacheOnDisc(true)
                // 是否考虑JPEG图像EXIF参数（旋转，翻转）
                .considerExifParams(true)
                // 设置图片以如何的编码方式显示
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                // 设置图片的解码类型//
                .bitmapConfig(Bitmap.Config.RGB_565)
                // 设置图片的解码配置
                // .decodingOptions(options)
                // .delayBeforeLoading(int delayInMillis)//int
                // delayInMillis为你设置的下载前的延迟时间
                // 设置图片加入缓存前，对bitmap进行设置
                // .preProcessor(BitmapProcessor preProcessor)
                // 设置图片在下载前是否重置，复位
                .resetViewBeforeLoading(true)
                // 是否设置为圆角，弧度为多少
                .displayer(new RoundedBitmapDisplayer(20))
                // 是否图片加载好后渐入的动画时间
                .displayer(new FadeInBitmapDisplayer(100))
                // 构建完成
                .build();
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        ImageLoader.getInstance().displayImage("https://c-ssl.duitang.com/uploads/item/201410/30/20141030043810_Tkecv.jpeg",mIVImageLoader,options);
    }


}
