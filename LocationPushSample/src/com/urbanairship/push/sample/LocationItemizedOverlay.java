package com.urbanairship.push.sample;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class LocationItemizedOverlay extends ItemizedOverlay<OverlayItem> {

    Context mContext;
    OverlayItem item;

    public LocationItemizedOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker));
        mContext = context;
    }

    @Override
    protected OverlayItem createItem(int i) {
        return item;
    }

    @Override
    public int size() {
        return item == null ? 0 : 1;
    }

    @Override
    protected boolean onTap(int index) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(item.getTitle());
        dialog.setMessage(item.getSnippet());
        dialog.show();
        return true;
    }

    public void setItem(OverlayItem newOverlayItem) {
        item = newOverlayItem;
        populate();
    }

}
