package com.agnostic.apollo.taskerlaunchershortcut;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.agnostic.apollo.taskerlaunchershortcut.model.AppInfoItem;
import com.agnostic.apollo.taskerlaunchershortcut.shortcuts.ShortcutFirer;
import com.agnostic.apollo.taskerlaunchershortcut.utils.LauncherUtils;
import com.agnostic.apollo.taskerlaunchershortcut.utils.Logger;

import static com.agnostic.apollo.taskerlaunchershortcut.LauncherHomeActivity.ACTION_FIRE_SHORTCUT_INTENT_URI;
import static com.agnostic.apollo.taskerlaunchershortcut.LauncherHomeActivity.ARG_SHORTCUT_INTENT_URI;


public class LauncherHomeFragment extends Fragment {

    private RecyclerView mAppsRecyclerView;
    private TextView mAppsTextView;
    private AppsAdapter mAppsAdapter;
    private String mQuery;
    private FragmentActivity mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_launcher_home, container, false);

        mAppsRecyclerView = (RecyclerView) view.findViewById(R.id.apps_recycler_view);
        mAppsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAppsTextView = (TextView) view.findViewById(R.id.empty_view);

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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_launcher_home, menu);

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
            case R.id.menu_item_search_shortcuts:
                startActivity(ShortcutChooserActivity.newShortcutTypeChooserIntent(mContext).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void updateUI() {

        processIntent();

        AppsLab appsLab = AppsLab.get(mContext);
        List<AppInfoItem> apps;
        if(mQuery==null)
            apps = appsLab.getApps();
        else
            apps = appsLab.searchAppsByName(mQuery);

        if (mAppsAdapter == null) {
            mAppsAdapter = new AppsAdapter(apps);
            mAppsRecyclerView.setAdapter(mAppsAdapter);
        } else {
            mAppsAdapter.setApps(apps);
            mAppsAdapter.notifyDataSetChanged();
            //mAdapter.notifyItemChanged(mAdapter.getPosition());
        }

        int appsSize = apps.size();

        if (appsSize==0) {
            mAppsRecyclerView.setVisibility(View.GONE);
            mAppsTextView.setVisibility(View.VISIBLE);
        }
        else {
            mAppsRecyclerView.setVisibility(View.VISIBLE);
            mAppsTextView.setVisibility(View.GONE);
        }

        updateSubtitle();
    }

    private void updateSubtitle() {

        if(mContext==null)
            return;

        AppsLab appsLab = AppsLab.get(mContext);

        int appsSize = mAppsAdapter.getItemCount();
        String subtitle = getResources()
                .getQuantityString(R.plurals.subtitle_apps_plural, appsSize, appsSize);

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
        if (intent != null) {
            Logger.logDebug(mContext, "Processing Intent To LauncherHome: \"" + intent.toString());

            if (intent.getAction() != null) {
                if (intent.getAction().equals(ACTION_FIRE_SHORTCUT_INTENT_URI))
                    ShortcutFirer.fireShortcutIntentUriIntent(mContext, intent);
                cancelFireShortcutIntentUriIntent(intent);
            }
        }
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

    private class AppsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mLabelTextView;
        private ImageView mIconImageView;
        private AppInfoItem mApp;
        private AppsAdapter mAdapter;
        int imageViewWidth=0;
        int imageViewHeight=0;

        public AppsHolder(View itemView, AppsAdapter adaptor ) {
            super(itemView);
            mLabelTextView = (TextView)
                    itemView.findViewById(R.id.list_item_app_label);
            mIconImageView = (ImageView)
                    itemView.findViewById(R.id.list_item_app_icon);
            final ViewTreeObserver vto = mIconImageView.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    imageViewWidth = mIconImageView.getWidth();
                    imageViewHeight = mIconImageView.getHeight();
                    mIconImageView.setImageDrawable(mApp.getIcon());

                    //Then remove layoutChange Listener
                    ViewTreeObserver vto = mIconImageView.getViewTreeObserver();
                    vto.removeOnGlobalLayoutListener(this);
                }
            });

            itemView.setOnClickListener(this);
            mAdapter=adaptor;
        }

        public void bindApp(AppInfoItem app) {
            mApp = app;
            mLabelTextView.setText(mApp.getLabel());
            mIconImageView.setImageDrawable(mApp.getIcon());
        }

        @Override
        public void onClick(View v) {
            mAdapter.setPosition(getAdapterPosition());

            Context context = v.getContext();

            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(mApp.getPackageName().toString());
            startActivity(launchIntent);
        }
    }

    private class AppsAdapter extends RecyclerView.Adapter<AppsHolder> {

        private List<AppInfoItem> mApps;

        private int position;

        public AppsAdapter(List<AppInfoItem> apps) {
            mApps = apps;
        }

        @Override
        public AppsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater
                    .inflate(R.layout.list_item_app, parent, false);
            return new AppsHolder(view,this);
        }

        @Override
        public void onBindViewHolder(AppsHolder holder, int position) {
            AppInfoItem app = mApps.get(position);
            holder.bindApp(app);
        }

        @Override
        public int getItemCount() {
            return mApps.size();
        }

        public void setApps(List<AppInfoItem> apps) {
            mApps = apps;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        updateUI();
    }

    public static LauncherHomeFragment newInstance() {
        Bundle args = new Bundle();
        LauncherHomeFragment fragment = new LauncherHomeFragment();
        return fragment;
    }

}
