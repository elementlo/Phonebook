package com.weikuo.elemenzhang.phonebookwk.controller;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.weikuo.elemenzhang.phonebookwk.R;
import com.weikuo.elemenzhang.phonebookwk.adapter.ResolvedContactsAdapter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ezvcard.Ezvcard;
import ezvcard.VCard;

/**
 * Created by elemenzhang on 2017/6/16.
 */

public class ResolveActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_archives_contact)
    RecyclerView rvArContact;

    List<VCard> vcards;
    private String filePath = "";
    private final int RESOLVE_FINISHIED=0;

    private ResolvedContactsAdapter adapter;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case RESOLVE_FINISHIED:
                    adapter=new ResolvedContactsAdapter(ResolveActivity.this,vcards);
                    dismissProgressbar();
                    rvArContact.setLayoutManager(new LinearLayoutManager(ResolveActivity.this));
                    rvArContact.setAdapter(adapter);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resolve);
        ButterKnife.bind(this);

        filePath = getIntent().getStringExtra("filePath");
        initView();
        resolveVCFTask();
    }

    private void resolveVCFTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(filePath);
                try {
                    vcards = Ezvcard.parse(file).all();
                    Message message=handler.obtainMessage();
                    message.what=RESOLVE_FINISHIED;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initView() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        showProgressbar();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
