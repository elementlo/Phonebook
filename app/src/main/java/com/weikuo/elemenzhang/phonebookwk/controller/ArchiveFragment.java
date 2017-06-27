package com.weikuo.elemenzhang.phonebookwk.controller;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.weikuo.elemenzhang.phonebookwk.MainActivity;
import com.weikuo.elemenzhang.phonebookwk.R;
import com.weikuo.elemenzhang.phonebookwk.adapter.ArchiveAdapter;
import com.weikuo.elemenzhang.phonebookwk.bean.Archives;
import com.weikuo.elemenzhang.phonebookwk.utils.ContactInfo;
import com.weikuo.elemenzhang.phonebookwk.utils.GeneralTools;
import com.weikuo.elemenzhang.phonebookwk.view.customview.SpacesItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
    public static final int SUCCESS_FLAG = 1;
    private final int FAIL_FLAG = 2;
    private final int RECOVER_WHAT = 3;
    private final int BROADCAST_CONTACTS = 4;
    private final int DELETE_SUCCEED = 5;
    private File file;

    private boolean hasReverse = false;

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
                            intent.putExtra("fileName", archivesList.get(position).getFileName());
                            startActivity(intent);
                        }
                    });
                    rvArchives.setAdapter(adapter);
                    break;
                case SUCCESS_FLAG:
                    ((MainActivity) getActivity()).getViewPager().setScroll(true);
                    ((MainActivity) getActivity()).dismissProgressbar();
                    Snackbar.make(btnRes, R.string.restore_complete, Snackbar.LENGTH_SHORT).
                            setAction("Action", null).show();
                    EventBus.getDefault().post(BROADCAST_CONTACTS);
                    break;
                case FAIL_FLAG:
                    ((MainActivity) getActivity()).getViewPager().setScroll(true);
                    ((MainActivity) getActivity()).dismissProgressbar();
                    Snackbar.make(btnRes, R.string.restore_fail, Snackbar.LENGTH_SHORT).
                            setAction("Action", null).show();
                    break;
                case DELETE_SUCCEED:
                    if (archivesList != null) {
                        archivesList.clear();
                        ArchiveFragmentPermissionsDispatcher.getVCFFromStorageWithCheck
                                (ArchiveFragment.this, GeneralTools.getStorageFilePath(getActivity()));
                    }
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
        initView();
        return contentView;
    }

    private void initView() {
        rvArchives.addItemDecoration(new SpacesItemDecoration(6));
        ((MainActivity) getActivity()).setOnDeleteArchClick(new MainActivity.onDeleteArchClick() {
            @Override
            public void onArchItemClick() {
                if (archivesList != null && archivesList.size() > 0
                        && ArchiveAdapter.archiveOption < archivesList.size()) {
                    file = new File(archivesList.get(ArchiveAdapter.archiveOption).getFilePath());
                    AlertDialog dialog = new AlertDialog.Builder(getActivity())
                            .setTitle("Delete Confirm")
                            .setMessage("Are you sure to delete the selected item?")
                            .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteArchive(file);
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    dialog.show();
                }
            }
        });
    }

    /*class DialogClick implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialog, int which) {
            deleteArchive(file);
        }
    }*/

    public void deleteArchive(final File file) {
        if (file.exists() && file.isFile()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    file.delete();
                    Message message = handler.obtainMessage();
                    message.what = DELETE_SUCCEED;
                    handler.sendMessage(message);
                }
            }).run();
        }
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
                        (ArchiveFragment.this, GeneralTools.getStorageFilePath(getActivity()));
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
                Collections.reverse(archivesList);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (archivesList.size() > 0) {
                            btnRes.setEnabled(true);
                        } else {
                            btnRes.setEnabled(false);
                        }
                    }
                });

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
        if (archivesList == null || archivesList.size() == 0) {
            return;
        }
        ((MainActivity) getActivity()).showProgressbar();
        ((MainActivity) getActivity()).getViewPager().setScroll(false);
        recoverContact();
    }

    public void recoverContact() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(archivesList.
                            get(ArchiveAdapter.archiveOption).getFilePath());
                    List<VCard> vCards = Ezvcard.parse(file).all();
                    for (VCard vCard : vCards) {
                        insertHandler.addContacts(getActivity(), vCard);
                    }
                    Message message = handler.obtainMessage();
                    message.obj = "recover success";
                    message.what = SUCCESS_FLAG;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    Message message = handler.obtainMessage();
                    message.obj = "recover fail";
                    message.what = FAIL_FLAG;
                    handler.sendMessage(message);
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Integer integer) {
        if (archivesList != null) {
            archivesList.clear();
            ArchiveFragmentPermissionsDispatcher.getVCFFromStorageWithCheck
                    (ArchiveFragment.this, GeneralTools.getStorageFilePath(getActivity()));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPermissionEvent(ContactFragment.StoragePermission permission) {
        getVCFFromStorage(GeneralTools.getStorageFilePath(getActivity()));
    }
}
