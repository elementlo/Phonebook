package com.weikuo.elemenzhang.phonebookwk.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by elemenzhang on 2017/6/9.
 */

public class ContentPagerAdapter extends FragmentPagerAdapter {
    List<Fragment> fragmentList;
    List<String> tabIndicators;
    public ContentPagerAdapter(FragmentManager fm, List<Fragment> fragmentList,List tabIndicators) {
        super(fm);
        this.fragmentList=fragmentList;
        this.tabIndicators=tabIndicators;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabIndicators.get(position);
    }
}
