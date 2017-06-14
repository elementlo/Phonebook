package com.weikuo.elemenzhang.phonebookwk.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.weikuo.elemenzhang.phonebookwk.R;

/**
 * Created by elemenzhang on 2017/6/9.
 */

public class ArchiveFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_tab_content, null);
        return contentView;
    }
}
