package com.crewcloud.crewmain.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crewcloud.crewmain.R;
import com.crewcloud.crewmain.activity.BaseActivity;
import com.crewcloud.crewmain.datamodel.Application;


import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by Dazone on 8/22/2017.
 */

public class ApplicationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private BaseActivity mActivity;
    private List<Application> lstApp = new ArrayList<>();

    public ApplicationAdapter(BaseActivity mActivity) {
        this.mActivity = mActivity;
    }

    public void addAll(List<Application> comments) {
        int curr = getItemCount();
        lstApp.addAll(comments);
        notifyItemRangeInserted(curr, getItemCount());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_layout_app_item, parent, false);
        return new ApplicationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ApplicationAdapter.ViewHolder) holder).bind(position);

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

        @Bind(R.id.iv_inflate_layout_app_item_icon)
        ImageView iv_inflate_layout_app_item_icon;

        @Bind(R.id.tv_inflate_layout_app_item_name)
        TextView tv_inflate_layout_app_item_name;

        @Bind(R.id.badge_notification_3)
        TextView tvBadge;

        public ViewHolder(View view) {
            super(view);
            try {
                ButterKnife.bind(this, view);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        void bind(int position) {
            final Application application = lstApp.get(position);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onClickItem(application);
                    }
                }
            });


            switch (application.ProjectCode) {
                case "_EAPP": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.ic_launcher_approval);
                    tv_inflate_layout_app_item_name.setText(R.string.approval);

                    break;
                }

                case "Mail3": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.ic_launcher_mail);
                    tv_inflate_layout_app_item_name.setText(R.string.crewmail);
                    break;
                }

                case "Schedule": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.ic_launcher_schedule);
                    tv_inflate_layout_app_item_name.setText(R.string.schedule);


                    break;
                }

                case "DDay": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.ic_launcher_dday);
                    tv_inflate_layout_app_item_name.setText(R.string.d_day);
                    break;
                }

                case "OA": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.ic_allboad);
                    tv_inflate_layout_app_item_name.setText(application.ApplicationName);
                    break;
                }
                case "HappyCall": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.ic_happy_call);
                    tv_inflate_layout_app_item_name.setText(application.ApplicationName);
                    break;
                }
                case "Board": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.ic_launcher_community);
                    tv_inflate_layout_app_item_name.setText(R.string.community);

                    break;
                }

                case "Notice": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.ic_launcher_notice);
                    tv_inflate_layout_app_item_name.setText(R.string.notice);

                    break;
                }

                case "Contacts": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.ic_launcher_contact);
                    tv_inflate_layout_app_item_name.setText(R.string.contact);
                    break;
                }

                case "CrewChat": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.ic_launcher_crewchat);
                    tv_inflate_layout_app_item_name.setText(R.string.crewchat);
                    if (!application.totalUnreadCount.equals("0")) {
                        ShortcutBadger.applyCount(mActivity, Integer.parseInt(application.totalUnreadCount));
                        tvBadge.setVisibility(View.VISIBLE);
                        tvBadge.setText(application.totalUnreadCount);
                    }
//                    tvBadge.setVisibility(View.VISIBLE);
//                    tvBadge.setText("2");

                    break;
                }

                case "WorkingTime": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.time_card);
                    tv_inflate_layout_app_item_name.setText(R.string.time_card);
                    break;
                }

                default: {
//                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.ic_apps_other);
                    break;
                }
            }

        }
    }

    public interface onClickItemListener {
        void onClickItem(Application application);
    }

    private onClickItemListener listener;

    public void setOnClickLitener(onClickItemListener litener) {
        this.listener = litener;
    }


}
