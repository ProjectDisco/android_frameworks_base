/*
 * Copyright (C) 2010, The Android Open Source Project
 * Copyright (C) 2014-2015 ParanoidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.systemui.statusbar.pie;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Pie menu item
 * View holder for a pie slice.
 */
public class PieItem {

    private int inner;
    private int level;
    private int outer;

    private float animate;
    private float start;
    private float sweep;

    private boolean mSelected;
    private boolean mIsLesser;

    private String mName;

    private View mView;
    private List<PieItem> mItems;
    private Path mPath;


    /**
     * Creates a new pie item
     *
     * @Param view the item view
     * @Param conext the current context
     * @Param name the name used to refrence the item
     * @Param lesser the pie level on pie T/F = 1/2
     */
    protected PieItem(View view, Context context, int level, String name, boolean lesser) {
        mView = view;
        this.level = level;
        setAnimationAngle(getAnimationAngle());
        setAlpha(getAlpha());
        setName(name);
        mIsLesser = lesser;
    }

    protected void setLesser(boolean lesser) {
        mIsLesser = lesser;
    }

    protected boolean isLesser() {
        return mIsLesser;
    }

    protected void setPath(Path path) {
        mPath = path;
    }

    protected Path getPath() {
        return mPath;
    }

    protected boolean hasItems() {
        return mItems != null;
    }

    protected List<PieItem> getItems() {
        return mItems;
    }

    protected void addItem(PieItem item) {
        if (mItems == null) {
            mItems = new ArrayList<PieItem>();
        }
        mItems.add(item);
    }


    protected void setName(String name) {
        mName = name;
        mView.setTag(mName);
    }

    protected String getName() {
        return mName;
    }

    protected void setAlpha(float alpha) {
        if (mView != null) {
            mView.setAlpha(alpha);
        }
    }

    protected float getAlpha() {
        if (mView != null) {
            return mView.getAlpha();
        }
        return 1;
    }

    protected void setAnimationAngle(float a) {
        animate = a;
    }

    protected float getAnimationAngle() {
        return animate;
    }

    protected void setSelected(boolean s) {
        mSelected = s;
        if (mView != null) {
            mView.setSelected(s);
        }
    }

    protected boolean isSelected() {
        return mSelected;
    }

    protected int getLevel() {
        return level;
    }

    protected void setGeometry(float st, float sw, int inside, int outside) {
        start = st;
        sweep = sw;
        inner = inside;
        outer = outside;
    }

    protected float getStart() {
        return start;
    }

    protected float getStartAngle() {
        return start + animate;
    }

    protected float getSweep() {
        return sweep;
    }

    protected int getInnerRadius() {
        return inner;
    }

    protected int getOuterRadius() {
        return outer;
    }

    protected View getView() {
        return mView;
    }

    protected void setIcon(int resId) {
        ((ImageView) mView).setImageResource(resId);
    }

    protected void setColor(int color) {
        ImageView imageView = ((ImageView) mView);
        Drawable drawable = imageView.getDrawable();
        drawable.setColorFilter(color, Mode.SRC_ATOP);
        imageView.setImageDrawable(drawable);
    }
}
