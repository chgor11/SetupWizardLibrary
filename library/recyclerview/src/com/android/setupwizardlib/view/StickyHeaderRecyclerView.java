/*
 * Copyright (C) 2016 The Android Open Source Project
 * ...
 */

package com.android.setupwizardlib.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Insets;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;

/**
 * This class provides sticky header functionality in a recycler view, to use with
 * SetupWizardIllustration. To use this, add a header tagged with "sticky". The header will continue
 * to be drawn when the sticky element hits the top of the view.
 *
 * <p>There are a few things to note:
 *
 * <ol>
 *   <li>The view does not work well with padding. b/16190933
 *   <li>If fitsSystemWindows is true, then this will offset the sticking position by the height of
 *       the system decorations at the top of the screen.
 * </ol>
 */
public class StickyHeaderRecyclerView extends HeaderRecyclerView {

  private View sticky;
  private int statusBarInset = 0;
  private final RectF stickyRect = new RectF();

  public StickyHeaderRecyclerView(Context context) {
    super(context);
  }

  public StickyHeaderRecyclerView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public StickyHeaderRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    if (sticky == null) {
      updateStickyView();
    }
    if (sticky != null) {
      final View headerView = getHeader();
      if (headerView != null && headerView.getHeight() == 0) {
        headerView.layout(0, -headerView.getMeasuredHeight(), headerView.getMeasuredWidth(), 0);
      }
    }
  }

  @Override
  protected void onMeasure(int widthSpec, int heightSpec) {
    super.onMeasure(widthSpec, heightSpec);
    if (sticky != null) {
      measureChild(getHeader(), widthSpec, heightSpec);
    }
  }

  /**
   * Call this method when the "sticky" view has changed, so this view can update its internal
   * states as well.
   */
  public void updateStickyView() {
    final View header = getHeader();
    if (header != null) {
      sticky = header.findViewWithTag("sticky");
    }
  }

  @Override
  public void draw(Canvas canvas) {
    super.draw(canvas);
    if (sticky != null) {
      final View headerView = getHeader();
      final int saveCount = canvas.save();
      // The view to draw when sticking to the top
      final View drawTarget = headerView != null ? headerView : sticky;
      // The offset to draw the view at when sticky
      final int drawOffset = headerView != null ? sticky.getTop() : 0;
      // Position of the draw target, relative to the outside of the scrollView
      final int drawTop = drawTarget.getTop();
      if (drawTop + drawOffset < statusBarInset || !drawTarget.isShown()) {
        // RecyclerView does not translate the canvas, so we can simply draw at the top
        stickyRect.set(
            0,
            -drawOffset + statusBarInset,
            drawTarget.getWidth(),
            drawTarget.getHeight() - drawOffset + statusBarInset);
        canvas.translate(0, stickyRect.top);
        canvas.clipRect(0, 0, drawTarget.getWidth(), drawTarget.getHeight());
        drawTarget.draw(canvas);
      } else {
        stickyRect.setEmpty();
      }
      canvas.restoreToCount(saveCount);
    }
  }

  @Override
  public WindowInsets onApplyWindowInsets(WindowInsets insets) {
    if (getFitsSystemWindows()) {
      final Insets statusBars = insets.getInsets(WindowInsets.Type.statusBars());
      statusBarInset = statusBars.top;

      final WindowInsets.Builder builder = new WindowInsets.Builder(insets);
      // Zero out only the top inset, keep left/right/bottom untouched.
      builder.setInsets(
          WindowInsets.Type.systemBars(),
          Insets.of(statusBars.left, 0, statusBars.right, statusBars.bottom));
      return builder.build();
    }
    return insets;
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    if (stickyRect.contains(ev.getX(), ev.getY())) {
      ev.offsetLocation(-stickyRect.left, -stickyRect.top);
      return getHeader().dispatchTouchEvent(ev);
    } else {
      return super.dispatchTouchEvent(ev);
    }
  }
}
