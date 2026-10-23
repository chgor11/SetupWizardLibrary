/*
 * Copyright (C) 2015 The Android Open Source Project
 * ...
 */

package com.android.setupwizardlib.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;

import androidx.annotation.Nullable;

/**
 * Button for navigation bar, which includes tinting of its compound drawables to be used for dark
 * and light themes.
 */
@SuppressLint("AppCompatCustomView")
public class NavigationBarButton extends Button {

  public NavigationBarButton(Context context) {
    super(context);
    init();
  }

  public NavigationBarButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    // Drawable start/end set from XML bypasses the public setters, so fetch,
    // mutate, and re-set them here to make sure tinting can be applied safely.
    Drawable[] drawables = getCompoundDrawablesRelative();
    for (int i = 0; i < drawables.length; i++) {
      if (drawables[i] != null) {
        drawables[i] = drawables[i].mutate();
      }
    }
    setCompoundDrawablesRelativeWithIntrinsicBounds(
        drawables[0], drawables[1], drawables[2], drawables[3]);
  }

  @Override
  public void setCompoundDrawables(
      @Nullable Drawable left,
      @Nullable Drawable top,
      @Nullable Drawable right,
      @Nullable Drawable bottom) {
    super.setCompoundDrawables(mutate(left), mutate(top), mutate(right), mutate(bottom));
    tintDrawables();
  }

  @Override
  public void setCompoundDrawablesRelative(
      @Nullable Drawable start,
      @Nullable Drawable top,
      @Nullable Drawable end,
      @Nullable Drawable bottom) {
    super.setCompoundDrawablesRelative(mutate(start), mutate(top), mutate(end), mutate(bottom));
    tintDrawables();
  }

  @Override
  public void setTextColor(ColorStateList colors) {
    super.setTextColor(colors);
    tintDrawables();
  }

  private void tintDrawables() {
    final ColorStateList textColors = getTextColors();
    if (textColors != null) {
      for (Drawable drawable : getAllCompoundDrawables()) {
        if (drawable != null) {
          drawable.setTintList(textColors);
        }
      }
      invalidate();
    }
  }

  private Drawable[] getAllCompoundDrawables() {
    Drawable[] drawables = new Drawable[6];
    Drawable[] compoundDrawables = getCompoundDrawables();
    drawables[0] = compoundDrawables[0]; // left
    drawables[1] = compoundDrawables[1]; // top
    drawables[2] = compoundDrawables[2]; // right
    drawables[3] = compoundDrawables[3]; // bottom
    Drawable[] compoundDrawablesRelative = getCompoundDrawablesRelative();
    drawables[4] = compoundDrawablesRelative[0]; // start
    drawables[5] = compoundDrawablesRelative[2]; // end
    return drawables;
  }

  @Nullable
  private static Drawable mutate(@Nullable Drawable drawable) {
    return drawable != null ? drawable.mutate() : null;
  }
}
