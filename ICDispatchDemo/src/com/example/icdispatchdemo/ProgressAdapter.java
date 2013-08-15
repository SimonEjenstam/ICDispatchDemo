package com.example.icdispatchdemo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ProgressAdapter extends ArrayAdapter<Long> {

    private boolean[] mDone;
    private long[] mWait;

    public ProgressAdapter(Context context, int textViewResourceId, Long[] objects,
            boolean[] mDone, long[] mWait) {
        super(context, textViewResourceId, objects);
        this.mDone = mDone;
        this.mWait = mWait;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView root = (TextView) convertView;
        if (root == null) {
            root = new TextView(getContext());
        }
        if (mDone[position]) {
            root.setText("Runtime: " + getItem(position)
                    + "  OK");
        } else if (mWait[position] != Long.MAX_VALUE) {
            root.setText("Executing...");
        } else {
            root.setText("Waiting...");
        }

        return root;
    }

}
