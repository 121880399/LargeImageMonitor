package org.zzy.lib.largeimage.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.zzy.lib.largeimage.LargeImageManager;
import org.zzy.lib.largeimage.R;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/11 20:04
 * 描    述：设置界面
 * 修订历史：
 * ================================================
 */
public class SettingActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private CheckBox mMenuSwitch;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
    }

    private void initView(){
        mToolBar = findViewById(R.id.toolbar);
        mMenuSwitch = findViewById(R.id.menu_switch);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mMenuSwitch.setChecked(LargeImageManager.getInstance().isOpenDialog());
        mMenuSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LargeImageManager.getInstance().setOpenDialog(!LargeImageManager.getInstance().isOpenDialog());
            }
        });
    }
}
