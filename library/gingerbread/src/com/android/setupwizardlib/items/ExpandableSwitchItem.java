/*
 * Copyright (C) 2017 The Android Open Source Project
 * ...
 */

package com.android.setupwizardlib.items;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.android.setupwizardlib.R;
import com.android.setupwizardlib.view.CheckableLinearLayout;

/**
 * A switch item which is divided into two parts: ...
 */
public class ExpandableSwitchItem extends SwitchItem
    implements OnCheckedChangeListener, OnClickListener {

  private CharSequence collapsedSummary;
  private CharSequence expandedSummary;
  private boolean isExpanded = false;

  public ExpandableSwitchItem() {
    super();
  }

  public ExpandableSwitchItem(Context context, AttributeSet attrs) {
    super(context, attrs);
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SuwExpandableSwitchItem);
    collapsedSummary = a.getText(R.styleable.SuwExpandableSwitchItem_suwCollapsedSummary);
    expandedSummary = a.getText(R.styleable.SuwExpandableSwitchItem_suwExpandedSummary);
    a.recycle();
  }

  @Override
  protected int getDefaultLayoutResource() {
    return R.layout.suw_items_expandable_switch;
  }

  @Override
  public CharSequence getSummary() {
    return isExpanded ? getExpandedSummary() : getCollapsedSummary();
  }

  /** @return True if the item is currently expanded. */
  public boolean isExpanded() {
    return isExpanded;
  }

  /** Sets whether the item should be expanded. */
  public void setExpanded(boolean expanded) {
    if (isExpanded == expanded) {
      return;
    }
    isExpanded = expanded;
    notifyItemChanged();
  }

  /** @return The summary shown when in collapsed state. */
  public CharSequence getCollapsedSummary() {
    return collapsedSummary;
  }

  /**
   * Sets the summary text shown when the item is collapsed. Corresponds to the {@code
   * app:suwCollapsedSummary} XML attribute.
   */
  public void setCollapsedSummary(CharSequence collapsedSummary) {
    this.collapsedSummary = collapsedSummary;
    if (!isExpanded()) {
      notifyChanged();
    }
  }

  /** @return The summary shown when in expanded state. */
  public CharSequence getExpandedSummary() {
    return expandedSummary;
  }

  /**
   * Sets the summary text shown when the item is expanded. Corresponds to the {@code
   * app:suwExpandedSummary} XML attribute.
   */
  public void setExpandedSummary(CharSequence expandedSummary) {
    this.expandedSummary = expandedSummary;
    if (isExpanded()) {
      notifyChanged();
    }
  }

  @Override
  public void onBindView(View view) {
    // TODO: If it is possible to detect, log a warning if this is being used with ListView.
    super.onBindView(view);
    View content = view.findViewById(R.id.suw_items_expandable_switch_content);
    content.setOnClickListener(this);

    if (content instanceof CheckableLinearLayout) {
      ((CheckableLinearLayout) content).setChecked(isExpanded());
    }

    tintCompoundDrawables(view);

    // Expandable switch item has focusability on the expandable layout on the left, and the
    // switch on the right, but not the item itself.
    view.setFocusable(false);
  }

  @Override
  public void onClick(View v) {
    setExpanded(!isExpanded());
  }

  // Tint the expand arrow with the text color
  private void tintCompoundDrawables(View view) {
    final TypedArray a =
        view.getContext().obtainStyledAttributes(new int[] {android.R.attr.textColorPrimary});
    final ColorStateList tintColor = a.getColorStateList(0);
    a.recycle();

    if (tintColor != null) {
      TextView titleView = (TextView) view.findViewById(R.id.suw_items_title);
      for (Drawable drawable : titleView.getCompoundDrawables()) {
        if (drawable != null) {
          drawable.setTintList(tintColor);
        }
      }
      for (Drawable drawable : titleView.getCompoundDrawablesRelative()) {
        if (drawable != null) {
          drawable.setTintList(tintColor);
        }
      }
    }
  }
    }
