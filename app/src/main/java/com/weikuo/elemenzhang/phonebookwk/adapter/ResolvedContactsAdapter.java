package com.weikuo.elemenzhang.phonebookwk.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by elemenzhang on 2017/6/16.
 */

public class ResolvedContactsAdapter extends RecyclerView.Adapter {

    static class MyViewHolder extends RecyclerView.ViewHolder{

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
