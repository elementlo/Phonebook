package com.weikuo.elemenzhang.phonebookwk.controller;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.weikuo.elemenzhang.phonebookwk.R;
import com.weikuo.elemenzhang.phonebookwk.utils.ACache;
import com.weikuo.elemenzhang.phonebookwk.utils.GeneralTools;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by elemenzhang on 2017/6/21.
 */

public class StorageActivity extends BaseActivity {
    @BindView(R.id.rl_sdcard)
    RelativeLayout itemSdcard;
    @BindView(R.id.rb_inter)
    RadioButton rbInter;
    @BindView(R.id.rb_outer)
    RadioButton rbOuter;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    private List<String> storageList;
    private ACache cache;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        cache = ACache.get(this);
        toolbar.setTitle("Backup to");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        storageList = GeneralTools.getExtSDCardPathList();
        if (storageList != null && storageList.size() > 1) {
            itemSdcard.setVisibility(View.VISIBLE);
        }
        rbInter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    rbOuter.setChecked(false);
                    if (GeneralTools.isSDCardMounted()) {
                        cache.put("path", Environment.getExternalStorageDirectory() + "/Contact_Backup");
                    }
                }
            }
        });
        rbOuter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    rbInter.setChecked(false);
                    cache.put("path", storageList.get(0)+"/Contact_Backup");
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
