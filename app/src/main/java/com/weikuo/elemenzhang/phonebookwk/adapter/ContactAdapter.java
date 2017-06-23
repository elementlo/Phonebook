package com.weikuo.elemenzhang.phonebookwk.adapter;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.github.tamir7.contacts.Contact;
import com.orhanobut.logger.Logger;
import com.weikuo.elemenzhang.phonebookwk.R;
import com.weikuo.elemenzhang.phonebookwk.view.customview.RoundedLetterView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by elemenzhang on 2017/6/12.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> implements SectionIndexer {
    private Context context;

    private List<Contact> contactList;
    private List<Contact> backContactList;
    private SparseArray<Boolean> checkBoxStateArray = new SparseArray<>();
    public static int SUM_NUM_CHECK = 0;

    public ContactAdapter(Context context, List<Contact> contactList) {
        this.context = context;
        backContactList = new ArrayList<>();
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(contactList, new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                if (o1.getGivenName() != null && o2.getGivenName() != null) {
                    int former = o1.getGivenName().charAt(0);
                    int latter = o2.getGivenName().charAt(0);
                    if (former < 65 || former > 90) {
                        return 1;
                    } else if (latter < 65 || latter > 90) {
                        return -1;
                    } else {
                        return o1.getGivenName().compareTo(o2.getGivenName());
                    }
                } else if (o1.getGivenName() == null && o2.getGivenName() != null) {
                    return 1;
                } else if (o1.getGivenName() != null && o2.getGivenName() == null) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        this.contactList = contactList;
        backContactList.addAll(contactList);
        initCheckBoxStateArray();
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).
                inflate(R.layout.item_recyclerview_contact, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        String name = contactList.get(position).getGivenName();
        String surname = contactList.get(position).getFamilyName();
        if (name == null) {
            name = surname;
        }
        if (name != null) {
            holder.contactName.setText(name);
            holder.roundedLetterView.setTitleText(name.toUpperCase().charAt(0) + "");
            int selection = name.charAt(0);
            int positionForSelection = getPositionForSelection(selection);
            if (position == positionForSelection) {
                holder.tvTitle.setVisibility(View.VISIBLE);
                holder.tvTitle.setText(name.toUpperCase().charAt(0) + "");
            } else {
                holder.tvTitle.setVisibility(View.INVISIBLE);
            }
        }


        if (checkBoxStateArray.get(position) == null) {
            checkBoxStateArray.put(position, false);
        }
        holder.cbContact.setChecked(checkBoxStateArray.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(position);
                EventBus.getDefault().post(SUM_NUM_CHECK + "");
            }
        });
    }

    public void setSelectItem(int position) {
        if (checkBoxStateArray.get(position)) {
            checkBoxStateArray.put(position, false);
            SUM_NUM_CHECK--;
        } else {
            checkBoxStateArray.put(position, true);
            SUM_NUM_CHECK++;
        }
        Logger.d(SUM_NUM_CHECK);
        notifyItemChanged(position);
    }

    public int getPositionForSelection(int selection) {
        for (int i = 0; i < contactList.size(); i++) {
            String name = contactList.get(i).getGivenName();
            if (name == null) {
                name = contactList.get(i).getFamilyName();
            }
            if (name != null) {
                char first = name.toUpperCase().charAt(0);
                if (first == selection) {
                    return i;
                }
            }
        }
        return -1;

    }

    public SparseArray<Boolean> getCheckBoxStateArray() {
        return checkBoxStateArray;
    }

    public void setCheckBoxStateArray(SparseArray checkBoxStateArray) {
        this.checkBoxStateArray = checkBoxStateArray;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    private void initCheckBoxStateArray() {
        for (int i = 0; i < contactList.size(); i++) {
            checkBoxStateArray.put(i, false);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }


    public void filter(String query) {
        query = query.toLowerCase();

        final List<Contact> filteredModelList = new ArrayList<>();
        for (Contact person : backContactList) {
            String nameEn = null;
            if (person.getGivenName() != null) {
                nameEn = person.getGivenName().toLowerCase();
            }
            String phoneEn = null;
            if (person.getPhoneNumbers() != null) {
                phoneEn = person.getPhoneNumbers().toString();
            }
            /*if (person.getEmails() != null && person.getEmails().size() > 0) {
                emailEn = person.getEmails().get(0).getAddress();
            }*/
            if (nameEn != null) {
                if (nameEn.contains(query)) {
                    filteredModelList.add(person);
                }
            }
        }
        contactList.clear();
        contactList.addAll(filteredModelList);
        notifyDataSetChanged();
    }


    public void resetList() {
        contactList.addAll(backContactList);
        notifyDataSetChanged();
    }

    @Override
    public Object[] getSections() {
        String nameArrary[] = new String[contactList.size()];
        for (int i = 0; i < nameArrary.length; i++) {
            nameArrary[i] = contactList.get(i).getGivenName();
        }
        return nameArrary;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        if (position >= contactList.size()) {
            position = contactList.size() - 1;
        }
        return position;
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_contactname)
        TextView contactName;
        @BindView(R.id.cb_contact_select)
        CheckBox cbContact;
        @BindView(R.id.rlv_name_view)
        RoundedLetterView roundedLetterView;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.rv_item)
        ConstraintLayout itemView;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
