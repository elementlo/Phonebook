package com.weikuo.elemenzhang.phonebookwk.adapter;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.weikuo.elemenzhang.phonebookwk.R;
import com.weikuo.elemenzhang.phonebookwk.view.customview.RoundedLetterView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ezvcard.VCard;

/**
 * Created by elemenzhang on 2017/6/16.
 */

public class ResolvedContactsAdapter extends RecyclerView.Adapter<ResolvedContactsAdapter.MyViewHolder> {
    private Context context;

    private List<VCard> vCardList;
    private SparseArray<Boolean> checkBoxStateArray = new SparseArray<>();


    public ResolvedContactsAdapter(Context context, List<VCard> vCardList) {
        this.context = context;
        this.vCardList = vCardList;
        initCheckBoxStateArray();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(context).inflate
                (R.layout.item_recyclerview_contact, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        String name = vCardList.get(position).getStructuredName().getGiven();
        String surname = vCardList.get(position).getStructuredName().getFamily();
        holder.tvTitle.setVisibility(View.GONE);
        if (name == null && surname != null) {
            name = surname;
        } else if (name != null && surname != null) {
            name = name + " " + surname;
        } else if (name == null && surname == null) {
            name = "";
        }
        if (name != null) {
            holder.contactName.setText(name);
            if (name.toUpperCase() != null && !name.toUpperCase().isEmpty()) {
                holder.roundedLetterView.setTitleText(name.toUpperCase().charAt(0) + "");
            } else {
                holder.roundedLetterView.setTitleText("");
            }
        }
        holder.cbContact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkBoxStateArray.put(position, isChecked);
            }
        });
        if (checkBoxStateArray.get(position) == null) {
            checkBoxStateArray.put(position, false);
        }
        holder.cbContact.setChecked(checkBoxStateArray.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vCardList.size();
    }

    public SparseArray<Boolean> getCheckBoxStateArray() {
        return checkBoxStateArray;
    }

    public void setCheckBoxStateArray(SparseArray checkBoxStateArray) {
        this.checkBoxStateArray = checkBoxStateArray;
        notifyDataSetChanged();
    }

    private void initCheckBoxStateArray() {
        for (int i = 0; i < vCardList.size(); i++) {
            checkBoxStateArray.put(i, false);
        }
    }

    public void setSelectItem(int position) {
        if (checkBoxStateArray.get(position)) {
            checkBoxStateArray.put(position, false);
        } else {
            checkBoxStateArray.put(position, true);
        }
        notifyItemChanged(position);
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

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
