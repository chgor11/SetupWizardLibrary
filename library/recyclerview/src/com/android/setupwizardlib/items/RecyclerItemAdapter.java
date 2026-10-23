/*
 * Copyright (C) 2015 The Android Open Source Project
 * ...
 */

package com.android.setupwizardlib.items;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.setupwizardlib.R;

/**
 * An adapter used with RecyclerView to display an {@link ItemHierarchy}. The item hierarchy used to
 * create this adapter can be inflated by {@link com.android.setupwizardlib.items.ItemInflater} from
 * XML.
 */
public class RecyclerItemAdapter extends RecyclerView.Adapter<ItemViewHolder>
    implements ItemHierarchy.Observer {

  private static final String TAG = "RecyclerItemAdapter";

  /**
   * A view tag set by {@link View#setTag(Object)}. If set on the root view of a layout, it will not
   * create the default background for the list item. This means the item will not have ripple touch
   * feedback by default.
   */
  public static final String TAG_NO_BACKGROUND = "noBackground";

  /** Listener for item selection in this adapter. */
  public interface OnItemSelectedListener {

    /**
     * Called when an item in this adapter is clicked.
     *
     * @param item The Item corresponding to the position being clicked.
     */
    void onItemSelected(IItem item);
  }

  private final ItemHierarchy itemHierarchy;
  private OnItemSelectedListener listener;

  public RecyclerItemAdapter(ItemHierarchy hierarchy) {
    itemHierarchy = hierarchy;
    itemHierarchy.registerObserver(this);
  }

  /**
   * Gets the item at the given position.
   *
   * @see ItemHierarchy#getItemAt(int)
   */
  public IItem getItem(int position) {
    return itemHierarchy.getItemAt(position);
  }

  @Override
  public long getItemId(int position) {
    IItem mItem = getItem(position);
    if (mItem instanceof AbstractItem) {
      final int id = ((AbstractItem) mItem).getId();
      return id > 0 ? id : RecyclerView.NO_ID;
    } else {
      return RecyclerView.NO_ID;
    }
  }

  @Override
  public int getItemCount() {
    return itemHierarchy.getCount();
  }

  @Override
  public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    final View view = inflater.inflate(viewType, parent, false);
    final ItemViewHolder viewHolder = new ItemViewHolder(view);

    final Object viewTag = view.getTag();
    if (!TAG_NO_BACKGROUND.equals(viewTag)) {
      final TypedArray typedArray =
          parent.getContext().obtainStyledAttributes(R.styleable.SuwRecyclerItemAdapter);
      Drawable selectableItemBackground =
          typedArray.getDrawable(
              R.styleable.SuwRecyclerItemAdapter_android_selectableItemBackground);
      if (selectableItemBackground == null) {
        selectableItemBackground =
            typedArray.getDrawable(R.styleable.SuwRecyclerItemAdapter_selectableItemBackground);
      }

      Drawable background = view.getBackground();
      if (background == null) {
        background =
            typedArray.getDrawable(R.styleable.SuwRecyclerItemAdapter_android_colorBackground);
      }

      if (selectableItemBackground == null || background == null) {
        Log.e(
            TAG,
            "Cannot resolve required attributes."
                + " selectableItemBackground="
                + selectableItemBackground
                + " background="
                + background);
      } else {
        final Drawable[] layers = {background, selectableItemBackground};
        view.setBackground(new LayerDrawable(layers));
      }

      typedArray.recycle();
    }

    view.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            final IItem item = viewHolder.getItem();
            if (listener != null && item != null && item.isEnabled()) {
              listener.onItemSelected(item);
            }
          }
        });

    return viewHolder;
  }

  @Override
  public void onBindViewHolder(ItemViewHolder holder, int position) {
    final IItem item = getItem(position);
    holder.setEnabled(item.isEnabled());
    holder.setItem(item);
    item.onBindView(holder.itemView);
  }

  @Override
  public int getItemViewType(int position) {
    // Use layout resource as item view type. RecyclerView item type does not have to be
    // contiguous.
    IItem item = getItem(position);
    return item.getLayoutResource();
  }

  @Override
  public void onChanged(ItemHierarchy hierarchy) {
    notifyDataSetChanged();
  }

  @Override
  public void onItemRangeChanged(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
    notifyItemRangeChanged(positionStart, itemCount);
  }

  @Override
  public void onItemRangeInserted(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
    notifyItemRangeInserted(positionStart, itemCount);
  }

  @Override
  public void onItemRangeMoved(
      ItemHierarchy itemHierarchy, int fromPosition, int toPosition, int itemCount) {
    // There is no notifyItemRangeMoved
    // https://code.google.com/p/android/issues/detail?id=125984
    if (itemCount == 1) {
      notifyItemMoved(fromPosition, toPosition);
    } else {
      // If more than one, degenerate into the catch-all data set changed callback, since I'm
      // not sure how recycler view handles multiple calls to notifyItemMoved (if the result
      // is committed after every notification then naively calling
      // notifyItemMoved(from + i, to + i) is wrong).
      // Logging this in case this is a more common occurrence than expected.
      Log.i(TAG, "onItemRangeMoved with more than one item");
      notifyDataSetChanged();
    }
  }

  @Override
  public void onItemRangeRemoved(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
    notifyItemRangeRemoved(positionStart, itemCount);
  }

  /**
   * Find an item hierarchy within the root hierarchy.
   *
   * @see ItemHierarchy#findItemById(int)
   */
  public ItemHierarchy findItemById(int id) {
    return itemHierarchy.findItemById(id);
  }

  /** Gets the root item hierarchy in this adapter. */
  public ItemHierarchy getRootItemHierarchy() {
    return itemHierarchy;
  }

  /**
   * Sets the listener to listen for when user clicks on a item.
   *
   * @see OnItemSelectedListener
   */
  public void setOnItemSelectedListener(OnItemSelectedListener listener) {
    this.listener = listener;
  }
    }
