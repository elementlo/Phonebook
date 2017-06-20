package com.weikuo.elemenzhang.phonebookwk.controller;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.widget.Button;

import com.weikuo.elemenzhang.phonebookwk.R;
import com.weikuo.elemenzhang.phonebookwk.adapter.ResolvedContactsAdapter;
import com.weikuo.elemenzhang.phonebookwk.utils.ContactInfo;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
    @BindView(R.id.btn_restore)Button btnRes;

    List<VCard> vcards;
    private SparseArray<Boolean> checkBoxStateArray;
    private String filePath = "";
    private final int RESOLVE_FINISHIED=0;
    private final int RESOLVE_SUB_SUCCEED=1;

    ContactInfo.ContactHandler insertHandler = ContactInfo.ContactHandler.getInstance();

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
                case RESOLVE_SUB_SUCCEED:
                    Snackbar.make(btnRes, "Back-up succeeds!", Snackbar.LENGTH_SHORT).
                            setAction("Action", null).show();
                    EventBus.getDefault().post(ArchiveFragment.SUCCESS_FLAG);
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

    @OnClick(R.id.btn_restore)
    public void onRestoreClick(){
        checkBoxStateArray=adapter.getCheckBoxStateArray();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < checkBoxStateArray.size(); i++) {
                    if (checkBoxStateArray.get(i)){
                        insertHandler.addContacts(ResolveActivity.this,vcards.get(i));
                        Message message=handler.obtainMessage();
                        message.what=RESOLVE_SUB_SUCCEED;
                        handler.sendMessage(message);
                    }
                }
            }
        }).start();
    }
}
