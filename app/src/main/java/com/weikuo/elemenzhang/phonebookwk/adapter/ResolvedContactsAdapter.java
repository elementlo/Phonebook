package com.weikuo.elemenzhang.phonebookwk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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

    public ResolvedContactsAdapter(Context context, List<VCard> vCardList) {
        this.context = context;
        this.vCardList = vCardList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder viewHolder = new MyViewHolder(LayoutInflater.from(context).inflate
                (R.layout.item_recyclerview_contact, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String name = vCardList.get(position).getStructuredName().getGiven();
        holder.tvTitle.setVisibility(View.GONE);
        if (name != null) {
            holder.contactName.setText(name);
            holder.roundedLetterView.setTitleText(name.toUpperCase().charAt(0) + "");
        }
    }

    @Override
    public int getItemCount() {
        return vCardList.size();
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

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
