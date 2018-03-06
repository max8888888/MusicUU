package com.qtfreet.musicuu.ui.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.qtfreet.musicuu.R;

/**
 * Created by Max on 2018/03/06
 */

public class MySimpleDivider extends RecyclerView.ItemDecoration {

    private int dividerHeight  = 0;
    private Paint dividerPaint = null;

    public MySimpleDivider(Context context) {
        this.dividerPaint = new Paint();
        this.dividerPaint.setColor(Color.parseColor("#cccccc"));
        this.dividerHeight = dip2px(context, 1f);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int childCount = parent.getChildCount();
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        for (int i=0; i<childCount - 1;i++) {
            View view = parent.getChildAt(i);
            int top = view.getBottom();
            int bottom = (view.getBottom() + dividerHeight);
            c.drawRect(left, top, right, bottom, dividerPaint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        // 在底部空出1dp的空间，画divider线
        outRect.bottom = dividerHeight;
    }

    private int dip2px(Context context, Float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }
}