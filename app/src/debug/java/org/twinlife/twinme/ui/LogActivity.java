/*
 *  Copyright (c) 2018 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */
package org.twinlife.twinme.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.DebugService;
import org.twinlife.twinlife.debug.DebugTwinlifeImpl;
import org.twinlife.twinme.utils.TwinmeActivityImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display various dumps provided by the DebugService.
 */
public class LogActivity extends TwinmeActivityImpl {
    private static final boolean DEBUG = true;
    private static final String LOG_TAG = "LogActivity";
    private ListView mList;
    private DebugService mDebugService;
    private ArrayAdapter<String> mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDebugService = DebugTwinlifeImpl.getDebugService();
        initViews();
    }

    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        setContentView(R.layout.log_activity);
        View backClickableView = findViewById(R.id.log_activity_back_clickable_view);
        backClickableView.setOnClickListener(v -> onBackClick());

        Spinner dumpSelector = findViewById(R.id.debug_select_log);
        List<String> dumpList = mDebugService.getDumpNames();
        ArrayAdapter<String> dumpAdapter = new ArrayAdapter<>(LogActivity.this, android.R.layout.simple_list_item_1, dumpList);
        dumpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dumpSelector.setAdapter(dumpAdapter);
        dumpSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < dumpList.size()) {
                    String name = dumpList.get(position);

                    displayDump(name);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mList = findViewById(R.id.log_list_view);
        mListAdapter = new ArrayAdapter<>(LogActivity.this,
                R.layout.log_list_item_text, new ArrayList<>());
        mList.setAdapter(mListAdapter);
        mList.setScrollContainer(false);
        displayDump(dumpList.get(0));
    }

    private void displayDump(String name) {
        List<String[]> items = mDebugService.getLogs(name);

        List<String> list = new ArrayList<>();
        if (items != null) {
            for (String[] cols : items) {
                StringBuilder sb = new StringBuilder();
                boolean needSep = false;
                for (String col : cols) {
                    if (needSep) {
                        sb.append("    ");
                    }
                    sb.append(col);
                    needSep = true;
                }
                list.add(sb.toString());
            }
        }
        updateList(list);
    }

    private void updateList(List<String> items) {

        mListAdapter.clear();
        mListAdapter.addAll(items);
        mListAdapter.notifyDataSetChanged();
        ViewGroup.LayoutParams layoutParams = mList.getLayoutParams();
        layoutParams.height = items.size() * 150;
        mList.setLayoutParams(layoutParams);

        mList.requestLayout();
    }

    private void onBackClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onBackClick");
        }

        finish();
    }
}
