package com.test.registerandupload.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.widget.ListView;


/**
 * Custom implementation of SwipeToRefresh which allows the use of
 * multiple views inside the same SwipeToRefresh layout which otherwise
 * would break the scrolling up of the child scroll/list view and cause the
 * SwipeRefreshLayout to refresh even if the scroll view isn't at the top
 */
public class SwipeToRefreshLayout extends SwipeRefreshLayout {

    ListView listView;

    public SwipeToRefreshLayout(@NonNull Context context) {
        super(context);
    }

    public SwipeToRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListViewChild(ListView listViewChild){
        this.listView = listViewChild;
    }

    @Override
    public boolean canChildScrollUp() {
        if(listView != null)
            return listView.canScrollVertically(-1);
        return false;
    }
}
