package org.zzy.lib.largeimage.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.mmkv.MMKV;

import org.zzy.lib.largeimage.LargeImageInfo;
import org.zzy.lib.largeimage.LargeImageManager;
import org.zzy.lib.largeimage.R;
import org.zzy.lib.largeimage.adapter.LargeImageListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/10 21:33
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class LargeImageListActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private RecyclerView mRecyclerView;
    private LargeImageListAdapter mLargeImageAdapter;
    private List<LargeImageInfo> mLargeImageList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_largeimage_list);
        initView();
        initData();
    }

    private void initView(){
        mToolBar = findViewById(R.id.toolbar);
        mRecyclerView = findViewById(R.id.rv_largeImage);
        mToolBar.inflateMenu(R.menu.toolbar);
    }

    private void initData(){
        mLargeImageList = new ArrayList<>();
        MMKV mmkv = LargeImageManager.getInstance().getMmkv();
        for (String key : mmkv.allKeys()) {
            mLargeImageList.add(mmkv.decodeParcelable(key,LargeImageInfo.class));
        }
        mLargeImageAdapter = new LargeImageListAdapter(this,mLargeImageList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mLargeImageAdapter);
    }
}
