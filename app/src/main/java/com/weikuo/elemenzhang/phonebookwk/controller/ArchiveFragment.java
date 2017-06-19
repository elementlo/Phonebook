package com.weikuo.elemenzhang.phonebookwk.controller;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.orhanobut.logger.Logger;
import com.weikuo.elemenzhang.phonebookwk.MainActivity;
import com.weikuo.elemenzhang.phonebookwk.R;
import com.weikuo.elemenzhang.phonebookwk.adapter.ArchiveAdapter;
import com.weikuo.elemenzhang.phonebookwk.bean.Archives;
import com.weikuo.elemenzhang.phonebookwk.utils.ContactInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by elemenzhang on 2017/6/9.
 */
@RuntimePermissions
public class ArchiveFragment extends Fragment {
    @BindView(R.id.rv_archives)
    RecyclerView rvArchives;
    @BindView(R.id.btn_restore)
    Button btnRes;

    private List<Archives> archivesList = new ArrayList<>();
    private final int SHOW_AR_LIST = 0;
    public final int SUCCESS_FLAG = 1;
    public final int FAIL_FLAG = 2;
    public final int RECOVER_WHAT = 3;

    private ArchiveAdapter adapter;
    ContactInfo.ContactHandler insertHandler = ContactInfo.ContactHandler.getInstance();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_AR_LIST:
                    rvArchives.setLayoutManager(new LinearLayoutManager(getActivity()));
                    adapter = new ArchiveAdapter(getActivity(), archivesList);
                    adapter.setOnItemClickListner(new ArchiveAdapter.OnItemClickListner() {
                        @Override
                        public void onItemClick(int position) {
                            Intent intent = new Intent(getActivity(), ResolveActivity.class);
                            intent.putExtra("filePath", archivesList.get(position).getFilePath());
                            startActivity(intent);
                        }
                    });
                    rvArchives.setAdapter(adapter);
                    break;
                case RECOVER_WHAT:
                    ((MainActivity) getActivity()).dismissProgressbar();
                    Snackbar.make(btnRes, "Back-up succeeds!", Snackbar.LENGTH_SHORT).
                            setAction("Action", null).show();
                    break;
            }
            super.handleMessage(msg);

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_tab_archives, null);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this, contentView);
        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadVcfTask();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    private void loadVcfTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArchiveFragmentPermissionsDispatcher.getVCFFromStorageWithCheck
                        (ArchiveFragment.this, new File(Environment.getExternalStorageDirectory()+"/PHONEBOOK"));
            }
        }).start();
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void getVCFFromStorage(File path) {
        List<VCard> vcards = null;
        Archives archives;
        if (path != null && path.exists()) {
            File file[] = path.listFiles();
            if (file != null) {
                for (File f : file) {
                    if (f.getAbsolutePath().endsWith("vcf")) {
                        try {
                            vcards = Ezvcard.parse(f).all();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        archives = new Archives();
                        archives.setFileName(f.getName());
                        archives.setFilePath(f.getAbsolutePath());
                        if (vcards != null && vcards.size() > 0) {
                            archives.setItemVolume(vcards.size() + "");
                        }
                        archivesList.add(archives);
                    }
                }
                Message message = handler.obtainMessage();
                message.what = SHOW_AR_LIST;
                handler.sendMessage(message);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArchiveFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnClick(R.id.btn_restore)
    public void restore() {
        if (ArchiveAdapter.archiveOption == -1) {
            return;
        }
        ((MainActivity) getActivity()).showProgressbar();
        recoverContact();
    }

    public void recoverContact() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                try {
                    File file=new File(archivesList.
                            get(ArchiveAdapter.archiveOption).getFilePath());
                    List<VCard> vCards = Ezvcard.parse(file).all();
                    for (VCard vCard : vCards) {
                        insertHandler.addContacts(getActivity(), vCard);
                    }
                    message.obj = "recover success";
                    message.arg1 = SUCCESS_FLAG;
                } catch (Exception e) {
                    message.obj = "recover fail";
                    message.arg1 = FAIL_FLAG;
                    e.printStackTrace();
                } finally {
                    message.what = RECOVER_WHAT;
                    handler.sendMessage(message);
                }
            }
        }).start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Integer integer){
        Logger.d("here");
        archivesList.clear();
        ArchiveFragmentPermissionsDispatcher.getVCFFromStorageWithCheck
                (ArchiveFragment.this, new File(Environment.getExternalStorageDirectory()+"/PHONEBOOK"));
    }

}
