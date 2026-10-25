/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.setupwizardlib.util;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.RequiresPermission;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * A helper class to manage the system navigation bar and status bar using AndroidX window insets.
 *
 * <p>When the useImmersiveMode intent extra is true, a screen in Setup Wizard should hide the
 * system bars using methods from this class. For Lollipop, {@link
 * #hideSystemBars(android.view.Window)} will completely hide the system navigation bar and change
 * the status bar to transparent, and layout the screen contents (usually the illustration) behind
 * it.
 */
public class SystemBarHelper {


  /** Hide the navigation bar for a dialog using AndroidX window-insets APIs. */
  public static void hideSystemBars(final Dialog dialog) {
    if (dialog == null || dialog.getWindow() == null) {
      return;
    }

    final Window window = dialog.getWindow();
    temporarilyDisableDialogFocus(window);
    WindowCompat.setDecorFitsSystemWindows(window, false);

    WindowInsetsControllerCompat controller =
        WindowCompat.getInsetsController(window, window.getDecorView());
    controller.setSystemBarsBehavior(
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    controller.hide(WindowInsetsCompat.Type.navigationBars());
  }

  /** Hide the navigation bar and lay out content behind the system bars. */
  public static void hideSystemBars(final Window window) {
    if (window == null) {
      return;
    }

    WindowCompat.setDecorFitsSystemWindows(window, false);
    WindowInsetsControllerCompat controller =
        WindowCompat.getInsetsController(window, window.getDecorView());
    controller.setSystemBarsBehavior(
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    controller.hide(WindowInsetsCompat.Type.navigationBars());
  }

  /**
   * Revert the actions of hideSystemBars. Note that this will remove the system UI visibility flags
   * regardless of whether it is originally present. You should also manually reset the navigation
   * bar and status bar colors, as this method doesn't know what value to revert it to.
   */
  public static void showSystemBars(final Dialog dialog, final Context context) {
    showSystemBars(dialog.getWindow(), context);
  }

  /**
   * Revert the actions of hideSystemBars. Note that this will remove the system UI visibility flags
   * regardless of whether it is originally present. You should also manually reset the navigation
   * bar and status bar colors, as this method doesn't know what value to revert it to.
   */
  public static void showSystemBars(final Window window, final Context context) {
    if (window == null) {
      return;
    }

    WindowInsetsControllerCompat controller =
        WindowCompat.getInsetsController(window, window.getDecorView());
    controller.show(WindowInsetsCompat.Type.navigationBars());
  }

  /**
   * Sets whether the back button on the software navigation bar is visible. This only works if you
   * have the STATUS_BAR permission. Otherwise framework will filter out this flag and this method
   * call will not have any effect.
   *
   * <p>IMPORTANT: Do not assume that users have no way to go back when the back button is hidden.
   * Many devices have physical back buttons, and accessibility services like TalkBack may have
   * gestures mapped to back. Please use onBackPressed, onKeyDown, or other similar ways to make
   * sure back button events are still handled (or ignored) properly.
   */
  @RequiresPermission("android.permission.STATUS_BAR")
  public static void setBackButtonVisible(final Window window, final boolean visible) {
    // AndroidX has no public API for hiding only the software-navigation back button.
    // This operation requires the signature-level STATUS_BAR permission and is not
    // available to ordinary applications. Intentionally do not use deprecated systemUiVisibility.
  }

  /**
   * Set a view to be resized when the keyboard is shown. This will set the bottom margin of the
   * view to be immediately above the keyboard, and assumes that the view sits immediately above the
   * navigation bar.
   *
   * <p>Note that you must set {@link android.R.attr#windowSoftInputMode} to {@code adjustResize}
   * for this class to work. Otherwise window insets are not dispatched and this method will have no
   * effect.
   *
   * <p>This will only take effect in versions Lollipop or above. Otherwise this is a no-op.
   *
   * @param view The view to be resized when the keyboard is shown.
   */
  public static void setImeInsetView(final View view) {
    ViewCompat.setOnApplyWindowInsetsListener(view, new WindowInsetsListener());
    ViewCompat.requestApplyInsets(view);
  }

  /**
   * Apply a hack to temporarily set the window to not focusable, so that the navigation bar will
   * not show up during the transition.
   */
  private static void temporarilyDisableDialogFocus(final Window window) {
    window.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    // Add the SOFT_INPUT_IS_FORWARD_NAVIGATION_FLAG. This is normally done by the system when
    // FLAG_NOT_FOCUSABLE is not set. Setting this flag allows IME to be shown automatically
    // if the dialog has editable text fields.
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION);
    new Handler(Looper.getMainLooper())
        .post(
            new Runnable() {
              @Override
              public void run() {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
              }
            });
  }

  private static class WindowInsetsListener implements OnApplyWindowInsetsListener {
    private int bottomOffset;
    private boolean hasCalculatedBottomOffset = false;

    @Override
    public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
      if (!hasCalculatedBottomOffset) {
        bottomOffset = getBottomDistance(view);
        hasCalculatedBottomOffset = true;
      }

      int systemBars = WindowInsetsCompat.Type.systemBars();
      int ime = WindowInsetsCompat.Type.ime();
      Insets systemBarInsets = insets.getInsets(systemBars);
      Insets imeInsets = insets.getInsets(ime);
      int bottomInset = Math.max(systemBarInsets.bottom, imeInsets.bottom);
      int bottomMargin = Math.max(bottomInset - bottomOffset, 0);

      ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
      // Keep the original guard to avoid measuring the view as empty.
      if (bottomMargin < lp.bottomMargin + view.getHeight()) {
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, bottomMargin);
        view.setLayoutParams(lp);
        Insets adjustedSystemBars = Insets.of(
            systemBarInsets.left, systemBarInsets.top, systemBarInsets.right, 0);
        Insets adjustedIme = Insets.of(imeInsets.left, imeInsets.top, imeInsets.right, 0);
        return new WindowInsetsCompat.Builder(insets)
            .setInsets(systemBars, adjustedSystemBars)
            .setInsets(ime, adjustedIme)
            .build();
      }
      return insets;
    }
  }

  private static int getBottomDistance(View view) {
    int[] coords = new int[2];
    view.getLocationInWindow(coords);
    return view.getRootView().getHeight() - coords[1] - view.getHeight();
  }
}
