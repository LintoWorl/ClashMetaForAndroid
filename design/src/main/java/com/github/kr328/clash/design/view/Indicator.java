package com.github.kr328.clash.design.view;

import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Px;

/**
 * 可以实现该接口，自定义Indicator 可参考{@link IndicatorView}
 */
public interface Indicator {

    void initIndicatorCount(int pagerCount, int currentPage);

    View getView();

    RelativeLayout.LayoutParams getParams();

    void onPageScrolled(int position, float positionOffset, @Px int positionOffsetPixels);

    void onPageSelected(int position);

    void onPageScrollStateChanged(int state);
}
