package com.weikuo.elemenzhang.phonebookwk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.tamir7.contacts.Contact;
import com.weikuo.elemenzhang.phonebookwk.R;
import com.weikuo.elemenzhang.phonebookwk.view.customview.RecyclerViewFastScroller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by elemenzhang on 2017/6/12.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder>
        implements RecyclerViewFastScroller.BubbleTextGetter {
    private Context context;
    private List<Contact> contactList;
    private SparseArray<Boolean> checkBoxStateArray = new SparseArray<>();

    public ContactAdapter(Context context, List<Contact> contactList) {
        this.context=context;
        Collections.sort(contactList, new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                if (o1.getGivenName()!=null&&o2.getGivenName()!=null){
                    return o1.getGivenName().compareTo(o2.getGivenName());
                }
                else return 0;
            }
        });
        this.contactList=contactList;
        initCheckBoxStateArray();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).
                inflate(R.layout.item_recyclerview_contact,parent,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.contactName.setText(contactList.get(position).getDisplayName());
        holder.contactName.setText(contactList.get(position).getGivenName());
        holder.cbContact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkBoxStateArray.put(position,isChecked);
            }
        });
        if (checkBoxStateArray.get(position) == null) {
            checkBoxStateArray.put(position, false);
        }
        holder.cbContact.setChecked(checkBoxStateArray.get(position));
    }

    public SparseArray<Boolean> getCheckBoxStateArray(){
        return checkBoxStateArray;
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    private void initCheckBoxStateArray(){
        for (int i=0;i<checkBoxStateArray.size();i++){
            checkBoxStateArray.put(i,false);
        }
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        return Character.toString(contactList.get(pos).getGivenName().charAt(0));
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_contactname)
        TextView contactName;
        @BindView(R.id.cb_contact_select)
        CheckBox cbContact;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
