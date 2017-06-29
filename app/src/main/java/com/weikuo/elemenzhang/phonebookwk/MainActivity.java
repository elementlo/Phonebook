package com.weikuo.elemenzhang.phonebookwk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.weikuo.elemenzhang.phonebookwk.adapter.ContactAdapter;
import com.weikuo.elemenzhang.phonebookwk.adapter.ContentPagerAdapter;
import com.weikuo.elemenzhang.phonebookwk.controller.ArchiveFragment;
import com.weikuo.elemenzhang.phonebookwk.controller.BaseActivity;
import com.weikuo.elemenzhang.phonebookwk.controller.ContactFragment;
import com.weikuo.elemenzhang.phonebookwk.controller.StorageActivity;
import com.weikuo.elemenzhang.phonebookwk.utils.ACache;
import com.weikuo.elemenzhang.phonebookwk.utils.GeneralTools;
import com.weikuo.elemenzhang.phonebookwk.view.customview.MyViewPager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.tl_maintab)
    TabLayout mainTablayout;
    @BindView(R.id.vp_mainpager)
    MyViewPager mViewpager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    private RelativeLayout toStorage;
    private TextView tvStorage;
    private ActionBarDrawerToggle toggle;
    private View headerView;
    private CheckBox checkBoxAll;
    private SearchView searchView;
    private MenuItem deleteItem;
    private MenuItem searchItem;
    private MenuItem checkAllItem;

    private List<String> tabIndicators;
    private List<Fragment> tabFragments;
    private ContactFragment contactFragment;
    private ArchiveFragment archiveFragment;
    private int currentPage = 0;

    private ContentPagerAdapter pagerAdapter;
    private onDeleteItemClick onDeleteItemClick;
    private onDeleteArchClick onDeleteArchClick;
    private boolean isCheckMode = false;
    private ACache aCache;

    public void setOnDeleteItemClickListner(onDeleteItemClick onDeleteItemClick) {
        this.onDeleteItemClick = onDeleteItemClick;
    }

    public void setOnDeleteArchClick(onDeleteArchClick onDeleteArchClick) {
        this.onDeleteArchClick = onDeleteArchClick;
    }

    public interface onDeleteItemClick {
        void onItemClick();
    }

    public interface onDeleteArchClick {
        void onArchItemClick();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        initView();
        initTabContent();
        //startService(new Intent(this, ContactSyncService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void initTabContent() {
        tabFragments = new ArrayList<>();
        tabIndicators = new ArrayList<>();
        contactFragment = new ContactFragment();
        archiveFragment = new ArchiveFragment();
        tabFragments.add(contactFragment);
        tabFragments.add(archiveFragment);
        tabIndicators.add("Phone");
        tabIndicators.add("Archives");

        mainTablayout.setTabMode(TabLayout.MODE_FIXED);
        ViewCompat.setElevation(mainTablayout, 20);
        pagerAdapter = new ContentPagerAdapter(getSupportFragmentManager(), tabFragments, tabIndicators);
        mViewpager.setAdapter(pagerAdapter);
        mainTablayout.setupWithViewPager(mViewpager);
        mViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                currentPage = position;
            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                mViewpager.setScroll(true);
                if (position == 1) {
                    cancelCheckMode();
                    EventBus.getDefault().post(0 + "");
                }
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initView() {
        aCache = ACache.get(this);
        mViewpager.setScroll(true);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        headerView = navigationView.getHeaderView(0);
        toStorage = (RelativeLayout) headerView.findViewById(R.id.ll_storage);
        tvStorage = (TextView) headerView.findViewById(R.id.tv_storage_detail);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            tvStorage.setText(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Contact_Backup");
        }
        toStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StorageActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (isCheckMode) {
                cancelCheckMode();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        deleteItem=menu.findItem(R.id.item_delete);
        searchItem=menu.findItem(R.id.item_search);
        checkAllItem=menu.findItem(R.id.item_checkall);
        searchView = (SearchView) menu.findItem(R.id.item_search).getActionView();
        checkBoxAll = (CheckBox) menu.findItem(R.id.item_checkall).getActionView();
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAllItem.setVisible(false);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (currentPage == 0 && contactFragment.getContactAdapter() != null) {
                    contactFragment.getContactAdapter().filter(newText);
                } else {

                }
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                checkAllItem.setVisible(true);
                contactFragment.getContactAdapter().resetList();
                return false;
            }
        });

        checkBoxAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (contactFragment.getContactAdapter() == null) {
                    return;
                }
                SparseArray array = contactFragment.getContactAdapter().getCheckBoxStateArray();
                if (array != null) {
                    if (isChecked) {
                        EventBus.getDefault().post(array.size() + "");
                        checkAll(array);
                    } else {
                        EventBus.getDefault().post(0 + "");
                        cancelCheckMode();
                    }
                }
            }
        });
        return true;
    }

    public void checkAll(SparseArray array) {
        for (int i = 0; i < array.size(); i++) {
            array.put(i, true);
            ContactAdapter.SUM_NUM_CHECK = array.size();
        }
        checkMode(array.size());
        contactFragment.getContactAdapter().setCheckBoxStateArray(array);

    }

    public void cancelCheck(SparseArray array) {
        for (int i = 0; i < array.size(); i++) {
            array.put(i, false);
            ContactAdapter.SUM_NUM_CHECK = 0;
            //cancelCheckMode();
        }
        contactFragment.getContactAdapter().setCheckBoxStateArray(array);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (currentPage == 1) {
            checkAllItem.setVisible(false);
            deleteItem.setVisible(true);
            searchItem.setVisible(false);
        } else {
            checkAllItem.setVisible(true);
            deleteItem.setVisible(false);
            searchItem.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (currentPage == 0) {
            if (id == R.id.item_delete) {
                onDeleteItemClick.onItemClick();
                return true;
            }
        } else {
            if (id == R.id.item_delete) {
                onDeleteArchClick.onArchItemClick();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage) {
            Logger.d("share");
            GeneralTools.socialShareApks(this);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public MyViewPager getViewPager() {
        return mViewpager;
    }

    public void checkMode(int number) {
        isCheckMode = true;
        toggle.setDrawerIndicatorEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        deleteItem.setVisible(true);
        searchItem.setVisible(false);
        toolbar.setTitle(number + " selected");
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBoxAll.setChecked(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                toggle.setDrawerIndicatorEnabled(true);
                deleteItem.setVisible(false);
                searchItem.setVisible(true);
                toolbar.setTitle(getApplication().getApplicationInfo().labelRes);
                SparseArray array = contactFragment.getContactAdapter().getCheckBoxStateArray();
                cancelCheck(array);
            }
        });
    }

    public void cancelCheckMode() {
        if (getSupportActionBar() == null) {
            return;
        }
        isCheckMode = false;
        if (checkBoxAll != null) {
            checkBoxAll.setChecked(false);
        }
        //((CheckBox) toolbar.getMenu().findItem(R.id.item_checkall).getActionView()).setChecked(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toggle.setDrawerIndicatorEnabled(true);
        deleteItem.setVisible(false);
        searchItem.setVisible(true);
        toolbar.setTitle(getApplication().getApplicationInfo().labelRes);
        if (contactFragment.getContactAdapter() != null) {
            SparseArray array = contactFragment.getContactAdapter().getCheckBoxStateArray();
            cancelCheck(array);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCheckedNum(String number) {
        if (toolbar != null) {
            if (Integer.parseInt(number) > 0) {
                checkMode(Integer.parseInt(number));
            } else {
                cancelCheckMode();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPermession(ContactFragment.StoragePermission permission) {
        if (tvStorage != null) {
            tvStorage.setText(Environment.getExternalStorageDirectory() + "");
        }
    }

    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void onSorageEvent(StorageActivity.StorageChangeEvent storageChangeEvent){
        if (aCache.getAsString("path")!=null&&tvStorage!=null){
            tvStorage.setText(aCache.getAsString("path")+"");
        }
    }*/
}
