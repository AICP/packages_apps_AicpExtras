/*
 * Copyright (C) 2013 The ChameleonOS Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lordclockan.aicpextras;

import android.Manifest;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.gesture.GestureLibrary;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.text.TextUtils;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;

import com.lordclockan.aicpextras.R;
import com.lordclockan.aicpextras.utils.ShortcutPickHelper;

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.Comparator;
import java.io.File;

public class GestureAnywhereBuilderActivity extends ListActivity
        implements ShortcutPickHelper.OnPickListener {

    private static final int STATUS_SUCCESS = 0;
    private static final int STATUS_CANCELLED = 1;
    private static final int STATUS_NO_STORAGE = 2;
    private static final int STATUS_NOT_LOADED = 3;

    private static final int MENU_ID_EDIT = 1;
    private static final int MENU_ID_REMOVE = 2;

    private static final int DIALOG_RENAME_GESTURE = 1;

    private static final int REQUEST_NEW_GESTURE = 1;
    private static final int REQUEST_PICK_SHORTCUT = 100;
    private static final int REQUEST_PICK_APPLICATION = 101;
    private static final int REQUEST_CREATE_SHORTCUT = 102;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private Context context;
    private LinearLayout mView;

    // Type: long (id)
    private static final String GESTURES_INFO_ID = "gestures.info_id";

    private final File mStoreFile = new File(Environment
            .getExternalStorageDirectory(), "/data/ga_gestures");

    private final Comparator<NamedGesture> mSorter = new Comparator<NamedGesture>() {
        public int compare(NamedGesture object1, NamedGesture object2) {
            return object1.name.compareTo(object2.name);
        }
    };

    private static GestureLibrary sStore;

    private GesturesAdapter mAdapter;
    private GesturesLoadTask mTask;
    private TextView mEmpty;

    private Dialog mRenameDialog;
    private EditText mInput;
    private NamedGesture mCurrentRenameGesture;

    private ShortcutPickHelper mPicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ga_gestures_list);

        mAdapter = new GesturesAdapter(this);
        setListAdapter(mAdapter);

        mView = (LinearLayout) findViewById(R.id.gesturesList);

        if (sStore == null) {
            sStore = GestureLibraries.fromFile(mStoreFile);
        }
        mEmpty = (TextView) findViewById(android.R.id.empty);
        loadGestures();

        mPicker = new ShortcutPickHelper(this, this);

        registerForContextMenu(getListView());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkStoragePermission()) {
            requestPermission();
        }
        if (mStoreFile.exists()) mStoreFile.setReadable(true, false);
    }

    static GestureLibrary getStore() {
        return sStore;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void reloadGestures(View v) {
        loadGestures();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addGesture(View v) {
        mPicker.pickShortcut(null, null, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_NEW_GESTURE:
                    Settings.System.putLong(getContentResolver(),
                            Settings.System.GESTURE_ANYWHERE_CHANGED,
                            System.currentTimeMillis());
                    loadGestures();
                    break;
                case REQUEST_CREATE_SHORTCUT:
                case REQUEST_PICK_APPLICATION:
                case REQUEST_PICK_SHORTCUT:
                    mPicker.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }
    }

    private void loadGestures() {
        if (mTask != null && mTask.getStatus() != GesturesLoadTask.Status.FINISHED) {
            mTask.cancel(true);
        }
        mTask = (GesturesLoadTask) new GesturesLoadTask().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTask != null && mTask.getStatus() != GesturesLoadTask.Status.FINISHED) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    private void checkForEmpty() {
        if (mAdapter.getCount() == 0) {
            mEmpty.setText(R.string.ga_gestures_empty);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(((TextView) info.targetView).getText());

        menu.add(0, MENU_ID_EDIT, 0, R.string.ga_gestures_edit);
        menu.add(0, MENU_ID_REMOVE, 0, R.string.ga_gestures_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)
                item.getMenuInfo();
        final NamedGesture gesture = (NamedGesture) menuInfo.targetView.getTag();

        switch (item.getItemId()) {
            case MENU_ID_EDIT:
                editGesture(gesture);
                return true;
            case MENU_ID_REMOVE:
                deleteGesture(gesture);
                return true;
        }

        return super.onContextItemSelected(item);
    }

    private void deleteGesture(NamedGesture gesture) {
        sStore.removeGesture(gesture.name + '|' + gesture.uri, gesture.gesture);
        sStore.save();

        final GesturesAdapter adapter = mAdapter;
        adapter.setNotifyOnChange(false);
        adapter.remove(gesture);
        adapter.sort(mSorter);
        checkForEmpty();
        adapter.notifyDataSetChanged();

        Toast.makeText(this, R.string.ga_gestures_delete_success, Toast.LENGTH_SHORT).show();
    }

    private void editGesture(NamedGesture gesture) {
        Intent intent = new Intent(this, GestureAnywhereCreateGestureActivity.class);
        intent.putExtra("uri", gesture.uri);
        intent.putExtra("name", gesture.name);
        startActivityForResult(intent, REQUEST_NEW_GESTURE);
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        if (TextUtils.isEmpty(uri) || TextUtils.isEmpty(friendlyName)) {
            return;
        }
        Intent intent = new Intent(this, GestureAnywhereCreateGestureActivity.class);
        intent.putExtra("uri", uri);
        intent.putExtra("name", friendlyName);
        startActivityForResult(intent, REQUEST_NEW_GESTURE);
    }

    private class GesturesLoadTask extends AsyncTask<Void, NamedGesture, Integer> {
        private int mThumbnailSize;
        private int mThumbnailInset;
        private int mPathColor;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            final Resources resources = getResources();
            mPathColor = resources.getColor(R.color.ga_gesture_color);
            mThumbnailInset = (int) resources.getDimension(R.dimen.ga_gesture_thumbnail_inset);
            mThumbnailSize = (int) resources.getDimension(R.dimen.ga_gesture_thumbnail_size);

            findViewById(R.id.addButton).setEnabled(false);
            findViewById(R.id.reloadButton).setEnabled(false);

            mAdapter.setNotifyOnChange(false);
            mAdapter.clear();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (isCancelled()) return STATUS_CANCELLED;
            if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                return STATUS_NO_STORAGE;
            }

            final GestureLibrary store = sStore;

            if (store.load()) {
                for (String name : store.getGestureEntries()) {
                    if (isCancelled()) break;

                    for (Gesture gesture : store.getGestures(name)) {
                        final Bitmap bitmap = gesture.toBitmap(mThumbnailSize, mThumbnailSize,
                                mThumbnailInset, mPathColor);
                        final NamedGesture namedGesture = new NamedGesture();
                        final int separator = name.indexOf('|');
                        namedGesture.gesture = gesture;
                        namedGesture.name = name.substring(0, separator);
                        namedGesture.uri = name.substring(separator + 1);

                        mAdapter.addBitmap(namedGesture.gesture.getID(), bitmap);
                        publishProgress(namedGesture);
                    }
                }

                return STATUS_SUCCESS;
            }

            return STATUS_NOT_LOADED;
        }

        @Override
        protected void onProgressUpdate(NamedGesture... values) {
            super.onProgressUpdate(values);

            final GesturesAdapter adapter = mAdapter;
            adapter.setNotifyOnChange(false);

            for (NamedGesture gesture : values) {
                adapter.add(gesture);
            }

            adapter.sort(mSorter);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (result == STATUS_NO_STORAGE) {
                getListView().setVisibility(View.GONE);
                mEmpty.setVisibility(View.VISIBLE);
                mEmpty.setText(getString(R.string.ga_gestures_error_loading,
                        mStoreFile.getAbsolutePath()));
            } else {
                findViewById(R.id.addButton).setEnabled(true);
                findViewById(R.id.reloadButton).setEnabled(true);
                checkForEmpty();
            }
        }
    }

    static class NamedGesture {
        String name;
        String uri;
        Gesture gesture;
    }

    private class GesturesAdapter extends ArrayAdapter<NamedGesture> {
        private final LayoutInflater mInflater;
        private final Map<Long, Drawable> mThumbnails = Collections.synchronizedMap(
                new HashMap<Long, Drawable>());

        public GesturesAdapter(Context context) {
            super(context, 0);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        void addBitmap(Long id, Bitmap bitmap) {
            mThumbnails.put(id, new BitmapDrawable(bitmap));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.ga_gestures_item, parent, false);
            }

            final NamedGesture gesture = getItem(position);
            final TextView label = (TextView) convertView;

            label.setTag(gesture);
            label.setText(gesture.name);
            label.setCompoundDrawablesWithIntrinsicBounds(mThumbnails.get(gesture.gesture.getID()),
                    null, null, null);

            return convertView;
        }
    }

    private boolean checkStoragePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(mView, R.string.storage_permission_granted ,Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(mView, R.string.storage_permission_not_granted ,Snackbar.LENGTH_LONG).show();
                }
            return;
        }
    }
}
