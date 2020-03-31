package com.crewcloud.crewmain.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crewcloud.crewmain.R;
import com.crewcloud.crewmain.activity.BaseActivity;
import com.crewcloud.crewmain.datamodel.NoticeDocument;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Dazone on 8/23/2017.
 */

public class NoticeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private BaseActivity mActivity;
    private List<NoticeDocument> lstApp = new ArrayList<>();

    public interface onClickItemListener {
        void onClick(int position);

    }

    private onClickItemListener listener;

    public void setOnClickItem(onClickItemListener item) {
        listener = item;
    }

    public NoticeAdapter(BaseActivity mActivity) {
        this.mActivity = mActivity;
    }

    public void addAll(List<NoticeDocument> comments) {
        int curr = getItemCount();
        lstApp.addAll(comments);
        notifyItemRangeInserted(curr, getItemCount());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_layout_schedule_item, parent, false);
        return new NoticeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((NoticeAdapter.ViewHolder) holder).bind(position);

    }


    @Override
    public int getItemCount() {
        if (lstApp == null) {
            return 0;
        }
        return lstApp.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_inflate_layout_unread_mail_item_name)
        TextView tv_inflate_layout_unread_mail_item_name;@Bind(R.id.tv_inflate_layout_unread_mail_item_date)
        TextView tv_inflate_layout_unread_mail_item_date;


        public ViewHolder(View view) {
            super(view);
            try {
                ButterKnife.bind(this, view);
            } catch (Exception e) {
                e.printStackTrace();
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(getAdapterPosition());
                }
            });

        }

        void bind(int position) {
            final NoticeDocument noticeDocument = lstApp.get(position);
            tv_inflate_layout_unread_mail_item_name.setText(noticeDocument.Title);
            tv_inflate_layout_unread_mail_item_date.setText(noticeDocument.DivisionName);
        }

    }
}

