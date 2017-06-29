package com.weikuo.elemenzhang.phonebookwk.controller;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.orhanobut.logger.Logger;
import com.weikuo.elemenzhang.phonebookwk.MainActivity;
import com.weikuo.elemenzhang.phonebookwk.R;
import com.weikuo.elemenzhang.phonebookwk.adapter.ContactAdapter;
import com.weikuo.elemenzhang.phonebookwk.bean.RawContact;
import com.weikuo.elemenzhang.phonebookwk.utils.ACache;
import com.weikuo.elemenzhang.phonebookwk.utils.ContactsTools;
import com.weikuo.elemenzhang.phonebookwk.utils.DatabaseHelper;
import com.weikuo.elemenzhang.phonebookwk.utils.GeneralTools;
import com.weikuo.elemenzhang.phonebookwk.view.customview.ColorGroupSectionTitleIndicator;
import com.weikuo.elemenzhang.phonebookwk.view.customview.CustomDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

/**
 * Created by elemenzhang on 2017/6/9.
 */
@RuntimePermissions
public class ContactFragment extends Fragment {
    @BindView(R.id.rv_contact)
    RecyclerView rvContact;
    @BindView(R.id.fast_scroller)
    VerticalRecyclerViewFastScroller fastScroller;
    @BindView(R.id.btn_backup)
    Button mFab;
    @BindView(R.id.fast_scroller_section_title_indicator)
    ColorGroupSectionTitleIndicator sectionTitleIndicator;
    private CustomDialog dialog;
    private InterstitialAd mInterstitialAd;

    private List<Contact> contactList;
    private ArrayList<ContentProviderOperation> ops;
    private String[] args;
    private ContactAdapter contactAdapter;
    private SparseArray<Boolean> checkBoxStateArray;
    private ArrayList<VCard> vcardList;
    private final int INIT_CANTACT_LIST = 0;
    private final int BACK_UP_SUCCEED = 1;
    private final int BACK_UP_FAILED = 2;
    private final int BROADCAST_ARCHIVE = 3;
    private final int SUBMIT_SUM_CHECK = 4;
    private ACache cache;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case INIT_CANTACT_LIST:
                    ((MainActivity) getActivity()).dismissProgressbar();
                    if (contactList != null && contactList.size() > 0) {
                        fastScroller.setSectionIndicator(sectionTitleIndicator);
                    }
                    contactAdapter = new ContactAdapter(getActivity(), contactList);
                    rvContact.setAdapter(contactAdapter);
                    break;
                case BACK_UP_SUCCEED:
                    Snackbar.make(mFab, R.string.backup_complete, Snackbar.LENGTH_SHORT).
                            setAction("Action", null).show();
                    mFab.setEnabled(false);
                    ((MainActivity) getActivity()).dismissProgressbar();
                    ((MainActivity) getActivity()).getViewPager().setScroll(true);
                    EventBus.getDefault().post(new AchiveEvent());
                    break;
                case BACK_UP_FAILED:
                    Snackbar.make(mFab, R.string.backup_failed, Snackbar.LENGTH_SHORT).
                            setAction("Action", null).show();
                    ((MainActivity) getActivity()).getViewPager().setScroll(true);
                    ((MainActivity) getActivity()).dismissProgressbar();
                    break;
                case SUBMIT_SUM_CHECK:
                    if (dialog.getTvItemNum() != null) {
                        dialog.getTvItemNum().setText(msg.arg1 + " items");
                    }
                    break;
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_tab_content, null);
        ButterKnife.bind(this, contentView);
        EventBus.getDefault().register(this);
        ContactFragmentPermissionsDispatcher.requestContactPermissionWithCheck(this);
        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initAd();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        ((MainActivity) getActivity()).showProgressbar();
        cache = ACache.get(getActivity());
        dialog = new CustomDialog(getActivity());
        fastScroller.setRecyclerView(rvContact);

        rvContact.setOnScrollListener(fastScroller.getOnScrollListener());
        rvContact.setLayoutManager(new LinearLayoutManager(getActivity()));
        ops = new ArrayList<>();
        mFab.setEnabled(false);

        ((MainActivity) getActivity()).setOnDeleteItemClickListner(new MainActivity.onDeleteItemClick() {
            @Override
            public void onItemClick() {
                final List<String> transport = new ArrayList<String>();
                checkBoxStateArray = contactAdapter.getCheckBoxStateArray();
                for (int i = 0; i < checkBoxStateArray.size(); i++) {
                    if (checkBoxStateArray.get(i)) {
                        transport.add(contactList.get(i).getId() + "");
                    }
                }
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle("Delete Confirm")
                        .setMessage("Are you sure to delete the selected " + transport.size() + " item(s)?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((MainActivity) getActivity()).cancelCheckMode();
                                deletContact(transport);
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

            private void deletContact(List<String> transport) {
                for (int i = 0; i < transport.size(); i++) {
                    args = new String[]{transport.get(i)};
                    ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                            .withSelection(ContactsContract.RawContacts.CONTACT_ID + "=?", args).build());
                }
                try {
                    getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                } finally {
                    if (contactList != null) {
                        contactList.clear();
                        ContactFragmentPermissionsDispatcher.
                                requestContactPermissionWithCheck(ContactFragment.this);
                    }
                }
            }
        });
    }

    private void initAd() {
        mInterstitialAd = new InterstitialAd(getActivity());
        mInterstitialAd.setAdUnitId("ca-app-pub-4981779028680185/4014682158");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("14B12B09C604EC8AE8978A8BD298578C")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void backupTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    vcardList = new ArrayList<>();
                    for (int i = 0; i < checkBoxStateArray.size(); i++) {
                        if (checkBoxStateArray.get(i)) {
                            VCard vcard = ContactsTools.createVCard(contactList.get(i));
                            vcard.validate(VCardVersion.V4_0);
                            vcardList.add(vcard);
                        }
                    }
                    Message messageSum = mHandler.obtainMessage();
                    messageSum.arg1 = vcardList.size();
                    messageSum.what = SUBMIT_SUM_CHECK;
                    mHandler.sendMessage(messageSum);
                    File file;
                    if (cache.getAsBinary("path") == null) {
                        file = new File(Environment.getExternalStorageDirectory() + "/Contact_Backup");
                    } else {
                        file = new File(cache.getAsString("path"));
                    }
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    Ezvcard.write(vcardList).go(GeneralTools.formatDate(file));
                    Message messageSuc = mHandler.obtainMessage();
                    messageSuc.what = BACK_UP_SUCCEED;
                    mHandler.sendMessage(messageSuc);
                } catch (IOException e) {
                    e.printStackTrace();
                    Message messageFal = mHandler.obtainMessage();
                    messageFal.what = BACK_UP_FAILED;
                    mHandler.sendMessage(messageFal);
                }
            }
        }).start();
    }

    @NeedsPermission({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void requestContactPermission() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                contactList = Contacts.getQuery().find();
                Message message = mHandler.obtainMessage();
                message.what = INIT_CANTACT_LIST;
                mHandler.sendMessage(message);
                //getActivity().deleteDatabase("sqlite-contact.db");
                DatabaseHelper helper = DatabaseHelper.getHelper(getActivity());
                helper.onDelete();
                try {
                    if (!(helper.getUserDao().isTableExists())) {
                        helper.reCreateTable();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                RawContact rawContact;
                for (int i = 0; i < contactList.size(); i++) {
                    rawContact = new RawContact();
                    if (contactList.get(i).getGivenName() != null) {
                        rawContact.setName(contactList.get(i).getGivenName());
                    } else {
                        rawContact.setName("");
                    }
                    if (contactList.get(i).getEmails() != null && contactList.get(i).getEmails().size() > 0) {
                        rawContact.setEmail(contactList.get(i).getEmails().get(0).getAddress());
                    }
                    if (contactList.get(i).getFamilyName() != null) {
                        rawContact.setFamily(contactList.get(i).getFamilyName());
                    } else {
                        rawContact.setFamily("");
                    }
                    if (contactList.get(i).getPhoneNumbers() != null && contactList.get(i).getPhoneNumbers().size() > 0) {
                        rawContact.setPhone(contactList.get(i).getPhoneNumbers().get(0).getNumber());
                    } else {
                        rawContact.setPhone("");
                    }
                    if (contactList.get(i).getAddresses() != null && contactList.get(i).getAddresses().size() > 0) {
                        rawContact.setAddress(contactList.get(i).getAddresses().get(0).getFormattedAddress());
                    } else {
                        rawContact.setAddress("");
                    }
                    if (contactList.get(i).getCompanyName() != null) {
                        rawContact.setCompany(contactList.get(i).getCompanyName());
                    } else {
                        rawContact.setCompany("");
                    }
                    if (contactList.get(i).getNote() != null) {
                        rawContact.setNote(contactList.get(i).getNote());
                    } else {
                        rawContact.setNote("");
                    }
                    try {
                        helper.getUserDao().create(rawContact);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                }

            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ContactFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        ContactFragmentPermissionsDispatcher.requestContactPermissionWithCheck(this);
        //requestContactPermission();
        EventBus.getDefault().post(new StoragePermission());
    }

    public ContactAdapter getContactAdapter() {
        return contactAdapter;
    }

    public List<Contact> getContactList() {
        return contactList;
    }


/*    @OnClick(R.id.btn_done)
    public void doneClick() {
        //bottomMenu.setVisibility(View.GONE);
        mFab.setVisibility(View.VISIBLE);
        ((MainActivity) getActivity()).cancelCheckMode();
    }*/

    @OnClick(R.id.btn_backup)
    public void backupClick() {
        ((MainActivity) getActivity()).showProgressbar();
        if (contactAdapter != null) {
            checkBoxStateArray = contactAdapter.getCheckBoxStateArray();
            if (checkBoxStateArray == null) {
                return;
            }
            mFab.setVisibility(View.GONE);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            if (dialog.getDoneButton() != null) {
                dialog.getDoneButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mFab.setVisibility(View.VISIBLE);
                        dialog.cancel();
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        }
                        ((MainActivity) getActivity()).cancelCheckMode();
                    }
                });
            }
            ContactFragmentPermissionsDispatcher.backupTaskWithCheck(ContactFragment.this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAchiveMessage(Integer integer) {
        if (contactList != null) {
            contactList.clear();
            ContactFragmentPermissionsDispatcher.requestContactPermissionWithCheck(this);
            if (contactList != null && contactList.size() > 0) {
                fastScroller.setSectionIndicator(sectionTitleIndicator);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCheckMessage(String num) {
        if (Integer.parseInt(num) > 0) {
            mFab.setEnabled(true);
        } else {
            mFab.setEnabled(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactChange(ContactSyncService.ContactChanged contactChanged) {
        contactList = contactChanged.getContactList();
        if (contactList != null) {
            Logger.d("receive contacts changed");
            Message message = mHandler.obtainMessage();
            message.what = INIT_CANTACT_LIST;
            mHandler.sendMessage(message);
        }
    }

    public static class StoragePermission {
    }

    public static class AchiveEvent {

    }
}
