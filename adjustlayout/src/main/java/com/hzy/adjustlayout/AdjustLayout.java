package com.hzy.adjustlayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzy on 2018/11/18.
 */

public class AdjustLayout extends ViewGroup {

    //测量结果缓存
    private List<int[]> measureCaches;
    //重力方向，默认居中，采用四位二进制数表示
    private int gravityFlag;
    //行间距
    private int rowSpace;
    //列间距
    private int colSpace;

    public AdjustLayout(Context context) {
        this(context,null);
    }

    public AdjustLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AdjustLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AdjustLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.AdjustLayout);
        gravityFlag = typedArray.getInteger(R.styleable.AdjustLayout_gravity,0b0101);
        rowSpace = typedArray.getDimensionPixelSize(R.styleable.AdjustLayout_rowSpace,0);
        colSpace = typedArray.getDimensionPixelSize(R.styleable.AdjustLayout_colSpace,0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        long start = System.currentTimeMillis();
        int childCount = getChildCount();
        if(childCount < 1){
            //wrap_content支持
            int width = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST?getSuggestedMinimumWidth():getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            int height = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST?getSuggestedMinimumHeight():getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            setMeasuredDimension(width,height);
        }else{
            measureCaches = new ArrayList<>();
            //最大可用宽度
            int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
            int minWidth = 0;
            //已使用的宽高
            int usedWidth = 0,usedHeight = 0;
            //行高
            int rowHeight = 0;
            //当前行子view个数
            int rowChildCount = 0;

            for(int i=0;i<childCount;i++){
                //加上列间隔
                if(rowChildCount > 0) {
                    usedWidth += colSpace;
                }

                View child = getChildAt(i);
                final LayoutParams lp = child.getLayoutParams();
                //区别在于textView、button等是否自动换行以适应布局，前者match_parent只会占有剩余的空间，而不会等于AdjustLayout的宽度
//                measureChild(child,getChildMeasureSpec(widthMeasureSpec,usedWidth,lp.width),
//                        getChildMeasureSpec(heightMeasureSpec,usedHeight,lp.height));
                measureChild(child,widthMeasureSpec,heightMeasureSpec);
                int childWidth = child.getMeasuredWidth();
                if(usedWidth+childWidth+getPaddingLeft()+getPaddingRight() > totalWidth){
                    //换行
                    usedWidth -= colSpace;
                    minWidth = Math.max(usedWidth,minWidth);
                    measureCaches.add(new int[]{rowChildCount,usedWidth,usedHeight,rowHeight});
                    usedHeight += rowSpace+rowHeight;
                    usedWidth = 0;
                    rowHeight = 0;
                    rowChildCount = 0;
                    //再次测量
                    measureChild(child,widthMeasureSpec,
                            getChildMeasureSpec(heightMeasureSpec,usedHeight,lp.height));
                    usedWidth = child.getMeasuredWidth();
                    rowHeight = child.getMeasuredHeight();
                }else{
                    //每行高度由最高的那个子view决定
                    rowHeight = Math.max(rowHeight,child.getMeasuredHeight());
                    usedWidth += childWidth;
                }
                rowChildCount++;
            }
            //最后一行信息记录
            measureCaches.add(new int[]{rowChildCount,usedWidth,usedHeight,rowHeight});
            minWidth = Math.max(Math.max(getSuggestedMinimumWidth(),minWidth),usedWidth);
            usedHeight += rowHeight;
            usedHeight = Math.max(getSuggestedMinimumHeight(),usedHeight);

            //自测量信息
            int width = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST?minWidth:getDefaultSize(minWidth, widthMeasureSpec);
            int height = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST?usedHeight:getDefaultSize(usedHeight, heightMeasureSpec);
            setMeasuredDimension(width,height);
        }
        Log.e("TAG", "onMeasure time:"+(System.currentTimeMillis() - start));
    }

    @Override
    protected void onLayout(boolean flag, int l, int t, int r, int b) {
        long start = System.currentTimeMillis();
        int childIdx = 0;
        //居中模式
        int hgrv = gravityFlag & 0b0011;
        int vgrv = gravityFlag & 0b1100;
        for(int idx=0;idx<measureCaches.size();idx++){
            int[] cacheInfo = measureCaches.get(idx);
            int usedWidth = cacheInfo[1];
            int diff = r - l - usedWidth;
            int leftStart = (hgrv==0b00)?0:(hgrv==0b01?diff/2:diff);
            int rowWidthUsed = 0;
            for(int j = 0;j<cacheInfo[0];j++){
                if(j>0){
                    rowWidthUsed += colSpace;
                }
                View child = getChildAt(childIdx);
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();
                int ltmp = leftStart+rowWidthUsed;
                int tstart = (vgrv == 0b0000)?cacheInfo[2]:
                        (vgrv == 0b0100?cacheInfo[2]+(cacheInfo[3]-childHeight)/2:cacheInfo[2]+cacheInfo[3]-childHeight);
                child.layout(ltmp,tstart,ltmp+child.getMeasuredWidth(),tstart+childHeight);
                rowWidthUsed += childWidth;
                childIdx++;
            }
            rowWidthUsed = 0;
        }
        Log.e("TAG", "onLayout time:"+(System.currentTimeMillis() - start));

    }
}
