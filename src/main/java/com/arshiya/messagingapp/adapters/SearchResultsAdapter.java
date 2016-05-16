package com.arshiya.messagingapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.arshiya.messagingapp.Conversation;
import com.arshiya.messagingapp.R;
import com.arshiya.messagingapp.Utils;
import com.arshiya.messagingapp.model.SmsModel;

import java.util.Date;

import io.realm.RealmResults;

/**
 * Created by arshiya on 16/5/16.
 */
public class SearchResultsAdapter extends RealmRecyclerViewAdapter<SmsModel, SearchResultsAdapter.ViewHolder> implements View.OnClickListener {

    private RealmResults<SmsModel> realmResults;
    private Utils utils;
    private Context mContext;
    private OnItemClickListenerRecyclerView listener;

    public SearchResultsAdapter(RealmResults<SmsModel> realmResults, OnItemClickListenerRecyclerView listener){
        super(realmResults, true, 0);

        this.realmResults = realmResults;
        utils = new Utils();
        this.listener = listener;
    }


    @Override
    public void updateRealmResults(RealmResults<SmsModel> queryResults, int maxDepth) {
        super.updateRealmResults(queryResults, maxDepth);
        realmResults = queryResults;
        notifyDataSetChanged();

    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result_item, parent, false);

        view.setOnClickListener(this);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SmsModel smsModel = realmResults.get(position);

        holder.snippet.setText(smsModel.getBody());
        holder.address.setText(smsModel.getAddress());

        Date date = new Date(smsModel.getDate());
        holder.date.setText(utils.getDate(date));
    }

    @Override
    public int getItemCount() {
        int size = 0;
        if (null != realmResults){
            size = realmResults.size();
        }
        return size;
    }

    @Override
    public void onClick(View v) {
        listener.onItemClick(v, realmResults);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView address;
        TextView snippet;
        TextView date;

        public ViewHolder(View itemView) {
            super(itemView);

            address = (TextView) itemView.findViewById(R.id.address);
            snippet = (TextView) itemView.findViewById(R.id.snippet);
            date = (TextView) itemView.findViewById(R.id.date);
        }
    }


    public interface OnItemClickListenerRecyclerView{
        void onItemClick(View item, RealmResults<SmsModel> results);
    }
}
