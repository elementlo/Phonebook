package com.weikuo.elemenzhang.phonebookwk.controller;

import android.Manifest;
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
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.tamir7.contacts.Contact;
import com.github.tamir7.contacts.Contacts;
import com.weikuo.elemenzhang.phonebookwk.MainActivity;
import com.weikuo.elemenzhang.phonebookwk.R;
import com.weikuo.elemenzhang.phonebookwk.adapter.ContactAdapter;
import com.weikuo.elemenzhang.phonebookwk.utils.GeneralTools;
import com.weikuo.elemenzhang.phonebookwk.view.customview.RecyclerViewFastScroller;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.Address;
import ezvcard.property.StructuredName;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import tk.zielony.naturaldateformat.AbsoluteDateFormat;
import tk.zielony.naturaldateformat.NaturalDateFormat;

/**
 * Created by elemenzhang on 2017/6/9.
 */
@RuntimePermissions
public class ContactFragment extends Fragment {
    @BindView(R.id.rv_contact)
    RecyclerView rvContact;
    @BindView(R.id.btn_backup)
    Button mFab;
    @BindView(R.id.fastscroller)
    RecyclerViewFastScroller fastScroller;
    @BindView(R.id.rl_bottommenu)RelativeLayout bottomMenu;
    @BindView(R.id.tv_itemnum)TextView tvItemNum;
    @BindView(R.id.tv_percent)TextView tvPercent;
    @BindView(R.id.btn_done)Button btnDone;

    private List<Contact> contactList;
    private ContactAdapter contactAdapter;
    private SparseArray<Boolean> checkBoxStateArray;
    private ArrayList<VCard> vcardList;
    private final int INIT_CANTACT_LIST = 0;
    private final int BACK_UP_SUCCEED = 1;
    private final int BACK_UP_FAILED = 2;
    private final int BROADCAST_ARCHIVE=3;
    private final int SUBMIT_SUM_CHECK=4;
    private AbsoluteDateFormat absFormat;
    private int sumCheck=0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case INIT_CANTACT_LIST:
                    contactAdapter = new ContactAdapter(getActivity(), contactList);
                    rvContact.setAdapter(contactAdapter);
                    fastScroller.setRecyclerView(rvContact);
                    fastScroller.setViewsToUse(R.layout.recycler_view_fast_scroller__fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
                    break;
                case BACK_UP_SUCCEED:
                    Snackbar.make(mFab, "Back-up succeeds!", Snackbar.LENGTH_SHORT).
                            setAction("Action", null).show();
                    ((MainActivity) getActivity()).dismissProgressbar();
                    EventBus.getDefault().post(BROADCAST_ARCHIVE);
                    break;
                case BACK_UP_FAILED:
                    Snackbar.make(mFab, "Back-up fails!", Snackbar.LENGTH_SHORT).
                            setAction("Action", null).show();
                    ((MainActivity) getActivity()).dismissProgressbar();
                    break;
                case SUBMIT_SUM_CHECK:
                    sumCheck=msg.arg1;
                    break;
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_tab_content, null);
        ButterKnife.bind(this, contentView);
        ContactFragmentPermissionsDispatcher.requestContactPermissionWithCheck(this);
        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {
        vcardList = new ArrayList<>();
        absFormat=new AbsoluteDateFormat(getActivity(), NaturalDateFormat.DATE);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).showProgressbar();
                if (contactAdapter != null) {
                    checkBoxStateArray = contactAdapter.getCheckBoxStateArray();
                    if (checkBoxStateArray == null) {
                        return;
                    }
                    mFab.setVisibility(View.GONE);
                    bottomMenu.setVisibility(View.VISIBLE);
                    ContactFragmentPermissionsDispatcher.backupTaskWithCheck(ContactFragment.this);
                }
            }
        });
        rvContact.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);
                final int firstVisibleItemPosition = findFirstVisibleItemPosition();
                if (firstVisibleItemPosition != 0) {
                    // this avoids trying to handle un-needed calls
                    if (firstVisibleItemPosition == -1)
                        //not initialized, or no items shown, so hide fast-scroller
                        fastScroller.setVisibility(View.GONE);
                    return;
                }
                final int lastVisibleItemPosition = findLastVisibleItemPosition();
                int itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1;
                //if all items are shown, hide the fast-scroller
                fastScroller.setVisibility(contactAdapter.getItemCount() > itemsShown ? View.VISIBLE : View.GONE);
            }
        });
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void backupTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < checkBoxStateArray.size(); i++) {
                        if (checkBoxStateArray.get(i)) {
                            VCard vcard = createVCard(contactList.get(i));
                            vcard.validate(VCardVersion.V4_0);
                            vcardList.add(vcard);
                            Message message = mHandler.obtainMessage();
                            message.what=SUBMIT_SUM_CHECK;
                            message.arg1=vcardList.size();
                            mHandler.sendMessage(message);
                        }
                    }
                    String date=absFormat.format(new Date().getTime());

                    File file = new File(Environment.getExternalStorageDirectory() + "/PHONEBOOK");
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File bookFile = new File(file + "/"+date+".vcf");
                    if (bookFile.exists()){
                        bookFile=GeneralTools.generateFileName(file,date);
                    }
                    Ezvcard.write(vcardList).go(bookFile);
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

    @NeedsPermission({Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
    public void requestContactPermission() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                contactList = Contacts.getQuery().find();
                Message message = mHandler.obtainMessage();
                message.what = INIT_CANTACT_LIST;
                mHandler.sendMessage(message);
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ContactFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        requestContactPermission();
    }

    public ContactAdapter getContactAdapter() {
        return contactAdapter;
    }

    public List<Contact> getContactList() {
        return contactList;
    }

    private static VCard createVCard(Contact contact) throws IOException {
        VCard vcard = new VCard();
        StructuredName n = new StructuredName();
        n.setFamily(contact.getFamilyName());
        n.setGiven(contact.getGivenName());
        n.getPrefixes().add(contact.getDisplayName());
        vcard.setStructuredName(n);
        vcard.setOrganization(contact.getCompanyName());

        if (contact.getAddresses() != null && contact.getAddresses().size() != 0) {
            Address adr = new Address();
            adr.setStreetAddress(contact.getAddresses().get(0).getStreet());
            adr.setLocality(contact.getAddresses().get(0).getCity());
            adr.setPostalCode(contact.getAddresses().get(0).getPostcode());
            adr.setCountry(contact.getAddresses().get(0).getCountry());
            adr.setRegion(contact.getAddresses().get(0).getRegion());
            adr.setLabel(contact.getAddresses().get(0).getLabel());
            vcard.addAddress(adr);
        }

        if (contact.getEmails() != null && contact.getEmails().size() != 0) {
            for (int i = 0; i < contact.getEmails().size(); i++) {
                vcard.addEmail(contact.getEmails().get(i).getAddress());
            }
        }

        if (contact.getPhoneNumbers() != null && contact.getPhoneNumbers().size() != 0) {
            for (int i = 0; i < contact.getPhoneNumbers().size(); i++) {
                vcard.addTelephoneNumber(contact.getPhoneNumbers().get(i).getNumber());
            }
        }
        return vcard;
    }

    @OnClick(R.id.btn_done)
    public void doneClick(){
        bottomMenu.setVisibility(View.GONE);
        mFab.setVisibility(View.VISIBLE);
    }

}
