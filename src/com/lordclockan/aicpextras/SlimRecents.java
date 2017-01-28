package com.lordclockan.aicpextras;

import android.os.Bundle;

public class SlimRecents extends SubActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SlimRecentPanel()).commit();
    }
}
