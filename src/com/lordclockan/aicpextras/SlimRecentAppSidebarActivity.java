package com.lordclockan.aicpextras;

import android.app.Fragment;
import android.os.Bundle;

import com.lordclockan.aicpextras.dslv.ActionListViewSettings;

public class SlimRecentAppSidebarActivity extends SubActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionListViewSettings fragment = new ActionListViewSettings();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        args.putInt("actionMode", 7);
        args.putInt("maxAllowedActions", -1);
        args.putBoolean("useAppPickerOnly", true);
        args.putString("fragment", "com.lordclockan.aicpextras.RecentAppSidebarFragment");
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }
}
