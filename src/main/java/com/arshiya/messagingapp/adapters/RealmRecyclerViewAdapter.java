package com.arshiya.messagingapp.adapters;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by arshiya on 9/5/16.
 */
public abstract class RealmRecyclerViewAdapter<T extends RealmObject, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {


    protected RealmResults<T> realmResults;
    protected List<T> lastCopyOfRealmResults;
    int maxDepth = 0;

    private RealmChangeListener realmResultsListener;
    Realm realm;

    public RealmRecyclerViewAdapter(RealmResults<T> realmResults, boolean automaticUpdate) {
        this(realmResults, automaticUpdate, 0);
    }

    /**
     *
     * @param realmResults
     * @param automaticUpdate
     * @param maxDepth limit of the deep copy when copying realmResults. All references after this depth will be {@code null}. Starting depth is {@code 0}.
     *                 A copy of realmResults is made at start, and on every change to compare against future changes. Detected changes are used to update
     *                 the RecyclerView as appropriate
     */
    public RealmRecyclerViewAdapter(RealmResults<T> realmResults, boolean automaticUpdate, int maxDepth) {

        this.realmResultsListener = (!automaticUpdate) ? null : getRealmResultsChangeListener();

        if (realmResultsListener != null && realmResults != null) {
            realmResults.addChangeListener(realmResultsListener);
        }
        this.realmResults = realmResults;
        realm = Realm.getDefaultInstance();
        this.maxDepth = maxDepth;

        lastCopyOfRealmResults = realm.copyFromRealm(realmResults, this.maxDepth);
    }



    @Override
    public int getItemCount() {
        return realmResults != null ? realmResults.size() : 0;
    }

    /**
     * Make sure this is called before a view is destroyed to avoid memory leaks do to the listeners.
     * Do this by calling setAdapter(null) on your RecyclerView
     * @param recyclerView
     */
    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (realmResultsListener != null) {
            if (realmResults != null) {
                realmResults.removeChangeListener(realmResultsListener);
            }
        }
        realm.close();
    }

    /**
     * Update the RealmResults associated with the Adapter. Useful when the query has been changed.
     * If the query does not change you might consider using the automaticUpdate feature.
     *
     * @param queryResults the new RealmResults coming from the new query.
     * @param maxDepth limit of the deep copy when copying realmResults. All references after this depth will be {@code null}. Starting depth is {@code 0}.
     *                 A copy of realmResults is made at start, and on every change to compare against future changes. Detected changes are used to update
     *                 the RecyclerView as appropriate
     */
    public void updateRealmResults(RealmResults<T> queryResults, int maxDepth) {
        if (realmResultsListener != null) {
            if (realmResults != null) {
                realmResults.removeChangeListener(realmResultsListener);
            }
        }

        realmResults = queryResults;
        if (realmResults != null && realmResultsListener !=null) {
            realmResults.addChangeListener(realmResultsListener);
        }
        this.maxDepth = maxDepth;
        lastCopyOfRealmResults = realm.copyFromRealm(realmResults,this.maxDepth);

        notifyDataSetChanged();
    }

    public T getItem(int position) {
        return realmResults.get(position);
    }


    private RealmChangeListener getRealmResultsChangeListener() {
        return new RealmChangeListener() {
//            @Override
//            public void onChange(Object element) {
//
//            }

            @Override
            public void onChange(Object element) {
                if (lastCopyOfRealmResults != null && !lastCopyOfRealmResults.isEmpty()) {
                    if (realmResults.isEmpty()) {
                        // If the list is now empty, just notify the recyclerView of the change.
                        lastCopyOfRealmResults = realm.copyFromRealm(realmResults,maxDepth);
                        notifyDataSetChanged();
                        return;
                    }
                    Patch patch = DiffUtils.diff(lastCopyOfRealmResults, realmResults);
                    List<Delta> deltas = patch.getDeltas();
                    lastCopyOfRealmResults = realm.copyFromRealm(realmResults,maxDepth);
                    if (!deltas.isEmpty()) {
                        List<Delta> deleteDeltas = new ArrayList<>();
                        List<Delta> insertDeltas = new ArrayList<>();
                        for (final Delta delta : deltas) {
                            switch (delta.getType()){
                                case DELETE:
                                    deleteDeltas.add(delta);
                                    break;
                                case INSERT:
                                    insertDeltas.add(delta);
                                    break;
                                case CHANGE:
                                    notifyItemRangeChanged(
                                            delta.getRevised().getPosition(),
                                            delta.getRevised().size());
                                    break;
                            }
                        }
                        for (final Delta delta : deleteDeltas) {
                            notifyItemRangeRemoved(
                                    delta.getOriginal().getPosition(),
                                    delta.getOriginal().size());
                        }
                        //item's should be removed before insertions are performed
                        for (final Delta delta : insertDeltas) {
                            notifyItemRangeInserted(
                                    delta.getRevised().getPosition(),
                                    delta.getRevised().size());
                        }
                    }
                } else {
                    notifyDataSetChanged();
                    lastCopyOfRealmResults = realm.copyFromRealm(realmResults,maxDepth);
                }
            }
        };
    }
}