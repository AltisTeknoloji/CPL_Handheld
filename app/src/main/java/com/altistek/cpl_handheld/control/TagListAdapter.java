package com.altistek.cpl_handheld.control;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.CopyOnWriteArrayList;
import com.altistek.cpl_handheld.R;

public class TagListAdapter extends BaseAdapter {

    private static final String TAG = TagListAdapter.class.getSimpleName();

    private static final boolean D = false;

    private static final int MAX_LIST_COUNT = 50000;

    private int mListCycleCount = 0;

    private CopyOnWriteArrayList<ListItem> mItemList;

    private CopyOnWriteArrayList<String> mTagList;

    private Context mContext;

    private class ItemHolder {

        public TextView mUpText;
    }

    public TagListAdapter(Context ctx) {
        super();
        if (D) Log.d(TAG, "TagListAdapter");
        mContext = ctx;
        mItemList = new CopyOnWriteArrayList<>();
        mTagList = new CopyOnWriteArrayList<>();
    }

    @Override
    public int getCount() {
        if (D) Log.d(TAG, "getCount");
        return mItemList.size();
    }

    @Override
    public Object getItem(int arg0) {
        if (D) Log.d(TAG, "getItem");
        return mItemList.get(arg0);
    }


    @Override
    public long getItemId(int arg0) {
        if (D) Log.d(TAG, "getItemId");
        return arg0;
    }

    public String getEPC(int arg0) {
        if (D) Log.d(TAG, "getEPC");
        return mItemList.get(arg0).EPC;
    }

    public String getBarcode(int arg0) {
        if (D) Log.d(TAG, "getBarcode");
        return mItemList.get(arg0).Barcode;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        if (D) Log.d(TAG, "getView");
        ItemHolder holder;
        if (arg1 == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            arg1 = inflater.inflate(R.layout.list_item, null);

            holder = new ItemHolder();
            holder.mUpText = (TextView) arg1.findViewById(R.id.mTagText);
            arg1.setTag(holder);
        } else {
            holder = (ItemHolder) arg1.getTag();
        }
        ListItem item = mItemList.get(arg0);

        //holder.mDownText.setVisibility(View.GONE);
        holder.mUpText.setText(item.Barcode);

        return arg1;
    }

    public boolean addItem(String barcode, String EPC, boolean filter) {
        if (D) Log.d(TAG, "addItem " + filter);
        if (filter) {
            if (mTagList.contains(barcode)) {
                if (D) Log.d(TAG, "count++ " + filter);
                int idx = mTagList.indexOf(barcode);
                //mItemList.get(idx).mDupCount = (mItemList.get(idx).mDupCount) + 1;
                this.notifyDataSetInvalidated();
                return false;
            }
            if (mItemList.size() == MAX_LIST_COUNT) {
                mTagList.clear();
                mItemList.clear();
                notifyDataSetChanged();
                mListCycleCount++;
                return false;
            }
            ListItem item = new ListItem();
            item.EPC = EPC;
            item.Barcode = barcode;
            //item.mDupCount = 1;

            mTagList.add(barcode);
            mItemList.add(item);
            notifyDataSetChanged();
            return true;
        } else {
            if (mItemList.size() == MAX_LIST_COUNT) {
                mTagList.clear();
                mItemList.clear();
                notifyDataSetChanged();
                mListCycleCount++;
            }
            ListItem item = new ListItem();

            // + Long.toString(mItemList.size() + 1);
            //item.mUt = barcode;

            item.EPC = EPC;
            item.Barcode = barcode;
            //item.mDupCount = 1;
            mItemList.add(item);
            notifyDataSetChanged();
            return true;
        }
    }

    public void removeAllItem() {
        if (D) Log.d(TAG, "removeAllItem");
        mItemList.clear();
        mTagList.clear();
        mListCycleCount = 0;
        notifyDataSetChanged();
    }
}