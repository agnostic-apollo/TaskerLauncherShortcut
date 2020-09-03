package com.agnostic.apollo.taskerlaunchershortcut;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.agnostic.apollo.taskerlaunchershortcut.utils.LauncherUtils;
import com.agnostic.apollo.taskerlaunchershortcut.utils.Logger;
import com.agnostic.apollo.taskerlaunchershortcut.utils.PermissionsUtils;
import com.agnostic.apollo.taskerlaunchershortcut.utils.QueryPreferences;

import java.util.ArrayList;
import java.util.List;

import static com.agnostic.apollo.taskerlaunchershortcut.utils.PermissionsUtils.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE;

public class ShortcutTypeChooserFragment extends Fragment {

    private ListView mShortcutTypesListView;
    private FragmentActivity mContext;

    public static final String ARG_SHORTCUT_TYPE = "arg_shortcut_type";
    public static final String SHORTCUT_TYPE_STATIC = "Static Shortcut";
    public static final String SHORTCUT_TYPE_DYNAMIC = "Dynamic Shortcut";
    public static final String SHORTCUT_TYPE_PINNED = "Pinned Shortcut";
    private String mShortcutIntentUri;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mContext = getActivity();
        QueryPreferences.setPinnedShortcutIntentUri(mContext, "");

        boolean permissionsGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            permissionsGranted = PermissionsUtils.checkSystemAlertWindowPermission(mContext);

        if(!permissionsGranted) {
            Logger.showToast(mContext, "Apps Require SYSTEM_ALERT_WINDOW Permission To Start Activities From Background For Android>=10");
            PermissionsUtils.askSystemAlertWindowPermission(mContext);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shortcut_type_chooser, container, false);

        if(mContext == null)
            return view;

        List<String> shortcutTypesArray= new ArrayList<>();
        shortcutTypesArray.add(SHORTCUT_TYPE_STATIC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            shortcutTypesArray.add(SHORTCUT_TYPE_DYNAMIC);
        shortcutTypesArray.add(SHORTCUT_TYPE_PINNED);

        mShortcutTypesListView = (ListView) view.findViewById(R.id.shortcut_types_list_view);
        ArrayAdapter<String> shortcutTypesAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, shortcutTypesArray);
        mShortcutTypesListView.setAdapter(shortcutTypesAdapter);
        mShortcutTypesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                String selectedShortcutType = (String)adapter.getItemAtPosition(position);
                Logger.logDebug(mContext, "Selected Shortcut Type: \"" + selectedShortcutType + "\"");
                setFragment(selectedShortcutType);
            }
        });

        updateUI();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        String shortcutIntentUri = QueryPreferences.getPinnedShortcutIntentUri(mContext);
        if (shortcutIntentUri != null && !shortcutIntentUri.isEmpty()) {
            ShortcutChooserActivity.returnShortcutIntentUri(mContext, shortcutIntentUri);
            QueryPreferences.setPinnedShortcutIntentUri(mContext, "");
        }

        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void updateUI() {

        processIntent();

    }

    private void setFragment(String selectedShortcutType) {
        if(mContext == null)
            return;

        Fragment fragment;

        if (selectedShortcutType.equals(SHORTCUT_TYPE_STATIC)) {
            fragment = StaticAndDynamicShortcutChooserFragment.newInstance(selectedShortcutType);
        } else if (selectedShortcutType.equals(SHORTCUT_TYPE_DYNAMIC)) {
            if(!LauncherUtils.isMyAppLauncherDefault(mContext)) {
                Logger.logDebugAndShowToast(mContext, "Dynamic Shortcuts Can Only Be Queried By Default Launcher App");
                Logger.logDebugAndShowToast(mContext, "Set TaskerLauncherShortcut As Default Launcher And Try Again");
                LauncherUtils.changeDefaultLauncherInitiate(mContext);
                return;
            }

            fragment = StaticAndDynamicShortcutChooserFragment.newInstance(selectedShortcutType);
        } else if (selectedShortcutType.equals(SHORTCUT_TYPE_PINNED)) {
            if(!LauncherUtils.isMyAppLauncherDefault(mContext)) {
                Logger.logDebugAndShowToast(mContext, "Pinned Shortcuts Can Only Be Received By The Default Launcher App");
                Logger.logDebugAndShowToast(mContext, "Set TaskerLauncherShortcut As Default Launcher And Try Again");
                LauncherUtils.changeDefaultLauncherInitiate(mContext);
                return;
            } else {
                mShortcutIntentUri=null;
                QueryPreferences.setPinnedShortcutIntentUri(mContext, "");
                Logger.showToast(mContext, "Go To Desired App And Create Shortcut With An Option Like \"Add To Home screen\"");
                LauncherUtils.startLauncherHome(mContext);
                return;
            }
            //fragment = PinnedShortcutChooserFragment.newInstance();
        } else {
            Logger.logError(mContext, "Unknown Selected Shortcut Type: \"" + selectedShortcutType + "\"");
            return;
        }

        mContext.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();

    }

    private void processIntent() {
        if(mContext == null)
            return;

        Intent intent = mContext.getIntent();
        if (intent != null) {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //permission granted
                if (PermissionsUtils.checkSystemAlertWindowPermission(mContext)) {
                    Logger.showToast(mContext, "Permissions Granted");
                } else { // permission not granted
                    Logger.showToast(mContext, "Permissions Not Granted");
                    try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
                    ShortcutChooserActivity.cancel(mContext);
                }
            }
        }
        updateUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 //permission granted
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Logger.showToast(mContext, "Permissions Granted");
                } else { // permission not granted
                    Logger.showToast(mContext, "Permissions Not Granted");
                    try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
                    ShortcutChooserActivity.cancel(mContext);
                }
                return;
            }
        }
    }

    public static ShortcutTypeChooserFragment newInstance() {
        Bundle args = new Bundle();
        ShortcutTypeChooserFragment fragment = new ShortcutTypeChooserFragment();
        return fragment;
    }

}
