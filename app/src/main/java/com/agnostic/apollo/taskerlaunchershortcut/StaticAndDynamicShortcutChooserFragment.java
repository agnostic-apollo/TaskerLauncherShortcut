package com.agnostic.apollo.taskerlaunchershortcut;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agnostic.apollo.taskerlaunchershortcut.model.ShortcutInfoItem;
import com.agnostic.apollo.taskerlaunchershortcut.utils.LauncherUtils;
import com.agnostic.apollo.taskerlaunchershortcut.utils.Logger;

import static com.agnostic.apollo.taskerlaunchershortcut.LauncherHomeActivity.ARG_SHORTCUT_INTENT_URI;
import static com.agnostic.apollo.taskerlaunchershortcut.ShortcutChooserActivity.REQUEST_CREATE_SHORTCUT;
import static com.agnostic.apollo.taskerlaunchershortcut.ShortcutTypeChooserFragment.ARG_SHORTCUT_TYPE;
import static com.agnostic.apollo.taskerlaunchershortcut.ShortcutTypeChooserFragment.SHORTCUT_TYPE_DYNAMIC;
import static com.agnostic.apollo.taskerlaunchershortcut.ShortcutTypeChooserFragment.SHORTCUT_TYPE_STATIC;

public class StaticAndDynamicShortcutChooserFragment extends Fragment {

    private RecyclerView mShortcutsRecyclerView;
    private TextView mShortcutsTextView;
    private ShortcutsAdapter mShortcutsAdapter;
    private String mQuery;
    private FragmentActivity mContext;
    private String mShortcutType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mContext = getActivity();

        if(savedInstanceState != null){
            Logger.logDebug(mContext, "A SavedInstanceState exists");
            mShortcutType = savedInstanceState.getString(ARG_SHORTCUT_TYPE);
        }else{
            Logger.logDebug(mContext,  "A SavedInstanceState doesn't exist");
            if(getArguments()!=null) {
                mShortcutType = getArguments().getString(ARG_SHORTCUT_TYPE);
            }
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_static_and_dynamic_shortcut_chooser, container, false);

        mShortcutsRecyclerView = (RecyclerView) view.findViewById(R.id.shortcuts_recycler_view);
        mShortcutsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mShortcutsTextView = (TextView) view.findViewById(R.id.empty_view);

        updateUI();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mShortcutType != null) {
            outState.putString(ARG_SHORTCUT_TYPE, mShortcutType);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_static_and_dynamic_shortcut_chooser, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mQuery=s;
                updateUI();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery(mQuery, false);
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener()
        {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item)
            {
                mQuery=null;
                updateUI();
                return true; // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item)
            {
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_change_launcher:
                LauncherUtils.changeDefaultLauncherInitiate(mContext);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void updateUI() {

        processIntent();

        ShortcutsLab shortcutsLab = ShortcutsLab.get(mContext);
        List<ShortcutInfoItem> shortcuts;

        if (mShortcutType.equals(SHORTCUT_TYPE_STATIC)) {
            shortcuts = shortcutsLab.getStaticShortcuts("");
        } else if (mShortcutType.equals(SHORTCUT_TYPE_DYNAMIC)) {
            shortcuts = shortcutsLab.getDynamicShortcuts("");
        } else {
            Logger.logError(mContext, "Unknown Shortcut Type Passed To StaticAndDynamicShortcutChooserFragment: \"" + mShortcutType + "\"");
            return;
        }

        if(mQuery!=null)
            shortcuts = shortcutsLab.searchShortcuts(shortcuts, mQuery);

        if (mShortcutsAdapter == null) {
            mShortcutsAdapter = new ShortcutsAdapter(shortcuts);
            mShortcutsRecyclerView.setAdapter(mShortcutsAdapter);
        } else {
            mShortcutsAdapter.setShortcuts(shortcuts);
            mShortcutsAdapter.notifyDataSetChanged();
            //mAdapter.notifyItemChanged(mAdapter.getPosition());
        }

        int shortcutsSize = shortcuts.size();

        if (shortcutsSize==0) {
            mShortcutsRecyclerView.setVisibility(View.GONE);
            mShortcutsTextView.setVisibility(View.VISIBLE);
        }
        else {
            mShortcutsRecyclerView.setVisibility(View.VISIBLE);
            mShortcutsTextView.setVisibility(View.GONE);
        }

        //Logger.logDebug(mContext, "Static Shortcuts:\n" + TextUtils.join("\n", shortcutsLab.getStaticShortcuts("")));
        //Logger.logDebug(mContext, "Dynamic Shortcuts:\n" + TextUtils.join("\n", shortcutsLab.getDynamicShortcuts("")));
        //Logger.logDebug(mContext, "Pinned Shortcuts:\n" + TextUtils.join("\n", shortcutsLab.getPinnedShortcuts("")));

        updateSubtitle();
    }

    private void updateSubtitle() {

        if(mContext==null)
            return;

        ShortcutsLab shortcutsLab = ShortcutsLab.get(mContext);

        int shortuctsSize = mShortcutsAdapter.getItemCount();
        String subtitle = getResources()
                .getQuantityString(R.plurals.subtitle_shortcuts_plural, shortuctsSize, shortuctsSize);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if(activity!=null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if(actionBar!=null)
                actionBar.setSubtitle(subtitle);
        }
    }

    private void processIntent() {
        if(mContext==null)
            return;

        Intent intent = mContext.getIntent();
    }

    private void cancelFireShortcutIntentUriIntent(Intent intent) {
        if(mContext==null)
            return;

        if(intent!=null) {
            //cancel already received intent
            intent.putExtra(ARG_SHORTCUT_INTENT_URI, "");
            mContext.setIntent(intent);
        }
    }

    private class ShortcutsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mAppLabelTextView;
        private TextView mShortcutLabelTextView;
        private ImageView mIconImageView;
        private ShortcutInfoItem mShortcut;
        private ShortcutsAdapter mAdapter;
        int imageViewWidth=0;
        int imageViewHeight=0;

        public ShortcutsHolder(View itemView, ShortcutsAdapter adaptor ) {
            super(itemView);
            mAppLabelTextView = (TextView)
                    itemView.findViewById(R.id.list_item_app_label);
            mShortcutLabelTextView = (TextView)
                    itemView.findViewById(R.id.list_item_shortcut_label);
            mIconImageView = (ImageView)
                    itemView.findViewById(R.id.list_item_shortcut_icon);
            final ViewTreeObserver vto = mIconImageView.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    imageViewWidth = mIconImageView.getWidth();
                    imageViewHeight = mIconImageView.getHeight();
                    mIconImageView.setImageDrawable(mShortcut.getIcon());

                    //Then remove layoutChange Listener
                    ViewTreeObserver vto = mIconImageView.getViewTreeObserver();
                    vto.removeOnGlobalLayoutListener(this);
                }
            });

            itemView.setOnClickListener(this);
            mAdapter=adaptor;
        }

        public void bindApp(ShortcutInfoItem shortcut) {
            mShortcut = shortcut;
            mAppLabelTextView.setText(mShortcut.getAppLabel());
            mShortcutLabelTextView.setText(mShortcut.getShortLabel());
            mIconImageView.setImageDrawable(mShortcut.getIcon());
        }

        @Override
        public void onClick(View v) {
            mAdapter.setPosition(getAdapterPosition());

            onClickShortcut(mShortcut);
        }
    }

    private class ShortcutsAdapter extends RecyclerView.Adapter<ShortcutsHolder> {

        private List<ShortcutInfoItem> mShortcuts;

        private int position;

        public ShortcutsAdapter(List<ShortcutInfoItem> apps) {
            mShortcuts = apps;
        }

        @Override
        public ShortcutsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater
                    .inflate(R.layout.list_item_shortcut, parent, false);
            return new ShortcutsHolder(view,this);
        }

        @Override
        public void onBindViewHolder(ShortcutsHolder holder, int position) {
            ShortcutInfoItem shortcut = mShortcuts.get(position);
            holder.bindApp(shortcut);
        }

        @Override
        public int getItemCount() {
            return mShortcuts.size();
        }

        public void setShortcuts(List<ShortcutInfoItem> shortcuts) {
            mShortcuts = shortcuts;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

    }

    private void onClickShortcut(ShortcutInfoItem shortcut) {
        if(mContext==null || shortcut == null)
            return;

        ShortcutsLab shortcutsLab = ShortcutsLab.get(mContext);

        if (mShortcutType.equals(SHORTCUT_TYPE_STATIC)) {
            Intent intent = shortcut.getIntent();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && (intent==null || intent.getAction() == null || !intent.getAction().equals(Intent.ACTION_CREATE_SHORTCUT)))
                ShortcutChooserActivity.returnShortcutIntentUri(mContext, shortcutsLab.createDeepShortcutFromShortcutInfoItem(shortcut).toUri(0));
            else
                startStaticShortcutConfigActivity(shortcut.getIntent());
        } else if (mShortcutType.equals(SHORTCUT_TYPE_DYNAMIC)) {
            ShortcutChooserActivity.returnShortcutIntentUri(mContext, shortcutsLab.createDeepShortcutFromShortcutInfoItem(shortcut).toUri(0));
        } else {
            Logger.logError(mContext, "Unknown Shortcut Type Passed To StaticAndDynamicShortcutChooserFragment: \"" + mShortcutType + "\"");
        }
    }

    private void startStaticShortcutConfigActivity(Intent intent) {
        if(intent==null)
            return;

        try {
            Logger.logDebug(mContext, "Starting Static Shortcut Config Activity With Intent: \"" + intent.toUri(0) + "\"");
            startActivityForResult(intent, REQUEST_CREATE_SHORTCUT);
        } catch (ActivityNotFoundException e) {
            Logger.logErrorAndShowToast(mContext, "ActivityNotFoundException: " + e.getMessage());
            Logger.logStackTrace(mContext, e);
        } catch (SecurityException e) {
            Logger.logErrorAndShowToast(mContext, "SecurityException: " + e.getMessage());
            Logger.logError(mContext, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.");
            Logger.logStackTrace(mContext, e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == ShortcutChooserActivity.REQUEST_CREATE_SHORTCUT) {
            if(intent!=null) {
                Logger.logDebug(mContext, "Intent Returned To StaticAndDynamicShortcutChooserFragment By App: \"" + intent.toUri(0));

                Intent shortcut_intent = (Intent) intent.getParcelableExtra("android.intent.extra.shortcut.INTENT");
                if(shortcut_intent!=null) {
                    Logger.logDebug(mContext, "Shortcut Intent: \"" + shortcut_intent.toUri(0));
                    ShortcutChooserActivity.returnShortcutIntentUri(mContext, shortcut_intent.toUri(0));
                    return;
                }
            }

            Logger.logErrorAndShowToast(mContext, "Invalid Shortcut Intent Returned By App");
        }
    }

    public static StaticAndDynamicShortcutChooserFragment newInstance(String selectedShortcutType) {
        Bundle args = new Bundle();
        args.putString(ARG_SHORTCUT_TYPE, selectedShortcutType);
        StaticAndDynamicShortcutChooserFragment fragment = new StaticAndDynamicShortcutChooserFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
