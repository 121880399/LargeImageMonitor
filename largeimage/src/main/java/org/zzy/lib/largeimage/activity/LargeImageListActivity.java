package org.zzy.lib.largeimage.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.zzy.lib.largeimage.R;

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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_largeimage_list);
        mToolBar = findViewById(R.id.toolbar);
        mToolBar.inflateMenu(R.menu.toolbar);
    }
}
