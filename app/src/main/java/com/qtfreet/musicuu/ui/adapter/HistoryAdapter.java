package com.qtfreet.musicuu.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qtfreet.musicuu.R;
import com.qtfreet.musicuu.model.OnHistoryItemClickListener;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Max on 2018/03/06
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context mContext;
    private List<String> mList;

    private OnHistoryItemClickListener onHistoryItemClickListener;

    public HistoryAdapter(Context mContext, List<String> historyList) {
        this.mContext = mContext;
        this.mList = historyList;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.history_item, parent, false);

        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {

        holder.song.setText(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_history)
        TextView song;

        HistoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onHistoryItemClickListener != null)
                        onHistoryItemClickListener.onHistoryItemClick(v,
                                mList.get(getAdapterPosition()), getAdapterPosition());
                }
            });
        }
    }

    public void setOnHistoryClickListener(OnHistoryItemClickListener listener) {
        this.onHistoryItemClickListener = listener;
    }

}
