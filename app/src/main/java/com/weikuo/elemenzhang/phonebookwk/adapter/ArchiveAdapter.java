package com.weikuo.elemenzhang.phonebookwk.adapter;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.weikuo.elemenzhang.phonebookwk.R;
import com.weikuo.elemenzhang.phonebookwk.bean.Archives;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by elemenzhang on 2017/6/16.
 */

public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.MyViewHolder> {
    private RadioButton lastCheckedRB = null;

    private Context context;

    private List<Archives> archivesList;
    public static int archiveOption = 0;

    private OnItemClickListner onItemClickListner;

    public ArchiveAdapter(Context context, List<Archives> archivesList) {
        this.context = context;
        this.archivesList = archivesList;
        archiveOption=0;
    }

    public void setOnItemClickListner(OnItemClickListner onItemClickListner) {
        this.onItemClickListner = onItemClickListner;
    }

    public interface OnItemClickListner {
        void onItemClick(int position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).
                inflate(R.layout.item_recyclerview_archives, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        if (archivesList != null) {
            holder.tvItemVolume.setText(archivesList.get(position).getItemVolume() + " items");
            holder.tvArchiveName.setText(archivesList.get(position).getFileName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListner.onItemClick(position);
                }
            });
            if (position == 0) {
                lastCheckedRB = holder.cbArchive;
                lastCheckedRB.setChecked(true);
            }
            holder.cbArchive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        archiveOption = position;
                        RadioButton checkedRb = (RadioButton) buttonView;
                        if (lastCheckedRB != null) {
                            lastCheckedRB.setChecked(false);
                        }
                        lastCheckedRB = checkedRb;
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return archivesList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_item_volume)
        TextView tvItemVolume;
        @BindView(R.id.rb_archive_select)
        RadioButton cbArchive;
        @BindView(R.id.tv_archives_name)
        TextView tvArchiveName;
        @BindView(R.id.cl_itemview)
        ConstraintLayout itemView;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
