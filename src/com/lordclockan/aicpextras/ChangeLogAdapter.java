package com.lordclockan.aicpextras;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import com.lordclockan.R;

class ChangeLogAdapter extends RecyclerView.Adapter<ChangeLogAdapter.ViewHolder> {

    private List<ChangelogItem> mChangelogItems;
    private Context mContext;

    @Override
    public ChangeLogAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View changeLogView = inflater.inflate(R.layout.change_log_item, parent, false);

        return new ViewHolder(changeLogView);
    }

    @Override
    public void onBindViewHolder(ChangeLogAdapter.ViewHolder viewHolder, int position) {
        ChangelogItem changelogItem = mChangelogItems.get(position);
        TextView commitID = viewHolder.commitID;
        TextView commitMessage = viewHolder.commitMessage;

        commitID.setText(changelogItem.getCommit_id());
        if (changelogItem.getCommit_message() != null) {
            commitMessage.setText(changelogItem.getCommit_message());
            commitID.setTextColor(getContext().getResources().getColor(R.color.colorWhite));
            commitID.setTextSize(16);
            commitID.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            commitID.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            commitMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mChangelogItems.size();
    }

    ChangeLogAdapter(Context context, List<ChangelogItem> changelogItems) {
        mChangelogItems = changelogItems;
        mContext = context;
    }

    private Context getContext() {
        return mContext;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView commitID;
        TextView commitMessage;

        ViewHolder(View itemView) {
            super(itemView);
            commitID = (TextView) itemView.findViewById(R.id.commit_id);
            commitMessage = (TextView) itemView.findViewById(R.id.commit_message);
        }
    }
}
