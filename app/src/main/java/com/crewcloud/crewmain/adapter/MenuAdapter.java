package com.crewcloud.crewmain.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crewcloud.crewmain.R;
import com.crewcloud.crewmain.activity.BaseActivity;
import com.crewcloud.crewmain.datamodel.LeftMenu;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Dazone on 8/28/2017.
 */

public class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private BaseActivity mActivity;
    private List<LeftMenu> lstApp = new ArrayList<>();

    public MenuAdapter(BaseActivity mActivity) {
        this.mActivity = mActivity;
    }

    public void addAll(List<LeftMenu> comments) {
        int curr = getItemCount();
        lstApp.addAll(comments);
        notifyItemRangeInserted(curr, getItemCount());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_adapter, parent, false);
        return new MenuAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MenuAdapter.ViewHolder) holder).bind(position);

    }


    @Override
    public int getItemCount() {
        if (lstApp == null) {
            return 0;
        }
        return lstApp.size();
    }

    public void clear() {
        lstApp.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_menu)
        TextView tvMenu;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    leftMenuListener.onClickItem(getAdapterPosition());
                }
            });
        }

        void bind(int position) {
            LeftMenu menuItem = lstApp.get(position);
            tvMenu.setText(menuItem.getName());
        }
    }

    public interface ItemLeftMenuListener {
        void onClickItem(int position);
    }

    private ItemLeftMenuListener leftMenuListener;

    public void setLeftMenuListener(ItemLeftMenuListener leftMenuListener) {
        this.leftMenuListener = leftMenuListener;
    }
}