package com.weikuo.elemenzhang.phonebookwk.view.customview;

import android.content.Context;
import android.util.AttributeSet;

import com.weikuo.elemenzhang.phonebookwk.bean.ColorGroup;

import xyz.danoz.recyclerviewfastscroller.sectionindicator.title.SectionTitleIndicator;

/**
 * Indicator for sections of type {@link ColorGroup}
 */
public class ColorGroupSectionTitleIndicator extends SectionTitleIndicator<String> {

    public ColorGroupSectionTitleIndicator(Context context) {
        super(context);
    }

    public ColorGroupSectionTitleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorGroupSectionTitleIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSection(String object) {
        setTitleText(object.charAt(0)+"");
    }


}
