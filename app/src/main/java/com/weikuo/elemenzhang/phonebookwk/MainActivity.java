package com.weikuo.elemenzhang.phonebookwk;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
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
import android.widget.Toast;

import com.github.tamir7.contacts.Contact;
import com.orhanobut.logger.Logger;
import com.weikuo.elemenzhang.phonebookwk.adapter.ContentPagerAdapter;
import com.weikuo.elemenzhang.phonebookwk.controller.ArchiveFragment;
import com.weikuo.elemenzhang.phonebookwk.controller.BaseActivity;
import com.weikuo.elemenzhang.phonebookwk.controller.ContactFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.tl_maintab)
    TabLayout mainTablayout;
    @BindView(R.id.vp_mainpager)
    ViewPager mViewpager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    private CheckBox cbCheckAll;

    private List<String> tabIndicators;
    private List<Fragment> tabFragments;
    private ContactFragment contactFragment;
    private ArchiveFragment archiveFragment;
    private int currentPage = 0;

    private ContentPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        initView();
        initTabContent();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initTabContent() {
        tabFragments = new ArrayList<Fragment>();
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
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initView() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        cbCheckAll = (CheckBox) menu.findItem(R.id.item_checkall).getActionView();

        SearchView searchView = (SearchView) menu.findItem(R.id.item_search).getActionView();
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.findItem(R.id.item_delete).setVisible(false);
                menu.findItem(R.id.item_checkall).setVisible(false);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (currentPage == 0 && contactFragment.getContactAdapter() != null &&
                        contactFragment.getContactList() != null) {
                    List<Contact> filterContacts = contactFragment.getContactAdapter().
                            filter(contactFragment.getContactList(), newText);
                    contactFragment.getContactAdapter().setFilter(filterContacts);
                } else {

                }
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                menu.findItem(R.id.item_delete).setVisible(true);
                menu.findItem(R.id.item_checkall).setVisible(true);
                return false;
            }
        });

        ((CheckBox) menu.findItem(R.id.item_checkall).getActionView()).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (contactFragment.getContactAdapter() == null) {
                    return;
                }
                SparseArray array = contactFragment.getContactAdapter().getCheckBoxStateArray();
                if (array != null) {
                    if (isChecked) {
                        for (int i = 0; i < array.size(); i++) {
                            array.put(i, true);
                        }
                        //ContactAdapter.checkedVolume=array.size();
                    } else {
                        for (int i = 0; i < array.size(); i++) {
                            array.put(i, false);
                        }
                        //ContactAdapter.checkedVolume=0;
                    }
                    contactFragment.getContactAdapter().setCheckBoxStateArray(array);
                }
            }
        });
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (currentPage==1){
            menu.findItem(R.id.item_checkall).setVisible(false);
        }else
            menu.findItem(R.id.item_checkall).setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.item_checkall) {
            Logger.d("here");


            return true;
        } else if (id == R.id.item_delete) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

/*    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCheckedVolume(Boolean checkVolume){
        if (cbCheckAll.isChecked()){
            cbCheckAll.setChecked(false);
        }
    }*/
}
