package com.xy.bezier.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.xy.bezier.R;
import com.xy.bezier.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by xingyun on 2016/10/27.
 */
public class CruisePathView extends View {
    private static final String TAG = "CruisePathView";
    private int mWidth;
    private int mHeight;
    private Paint mPaint;
    private PathMeasure mPathMeasure;
    private Path mPath;
    private Bitmap mBitmap;
    /**
     * 曲线高度个数分割
     */
    private static final int quadCount = 10;
    /**
     * 曲度
     */
    private static final float intensity = 0.2f;
    /**
     * 曲线摇摆的幅度
     */
    private int range = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
    /**
     * 款度往右偏移量,把开始点移出屏幕右部
     */
    private float mDx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
    private float mValue;
    private ObjectAnimator mPathAnim;
    private float[] pos;

    public CruisePathView(Context context) {
        this(context, null);
    }

    public CruisePathView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CruisePathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mWidth = DisplayUtils.getScreenWidth(getContext());
        mHeight = DisplayUtils.getScreenHeight(getContext());
        Log.d(TAG, "mWidth=" + mWidth + ", mHeight=" + mHeight);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(2);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        //测量路径的坐标位置
        mPathMeasure = new PathMeasure();

        mPath = new Path();
        CPoint cPoint = new CPoint(mWidth, mHeight / 2);
        List<CPoint> points = createPoints(cPoint);
        buildCurvePath(mPath, points);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_gift_cruise_ship);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw==>");
        drawCruisePath(canvas);
    }

    private void drawCruisePath(Canvas canvas) {
        if (mBitmap == null || mBitmap.isRecycled()) {
            return;
        }
        pos = new float[2];
        canvas.drawPath(mPath, mPaint);
        mPathMeasure.setPath(mPath, false);
        mPathMeasure.getPosTan(mWidth * mValue, pos, null);
        Log.d(TAG, "drawCruisePath==>pos[0]="+ pos[0]+", pos[1]="+ pos[1]);
        canvas.drawBitmap(mBitmap, mWidth - pos[0], pos[1] - mHeight / 5, null);
    }

    public void playPathAnim() {
        mPathAnim = ObjectAnimator.ofFloat(this, "phase", 0f, 1f);
        mPathAnim.setDuration(5000);
        mPathAnim.setInterpolator(new LinearInterpolator());
        mPathAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                float value = (float) valueAnimator.getAnimatedValue();
//                Log.d(TAG, "playPathAnim==>onAnimationUpdate...fraction=" + fraction + ", value=" + value);
                mValue = 1 - fraction;
                invalidate();
            }
        });
        mPathAnim.start();
    }

    /**
     * 根据点绘制曲线
     *
     * @param path
     * @param points
     */
    private void buildCurvePath(Path path, List<CPoint> points) {
        if (points != null && points.size() > 1) {
            for (int i = 0; i < points.size(); i++) {
                CPoint point = points.get(i);
                if (i == 0) {// 第一个元素
                    CPoint nextPoint = points.get(i + 1);
                    point.dx = (point.x - nextPoint.x) * intensity;
                    point.dy = (point.y - nextPoint.y) * intensity;
                    Log.d(TAG, "buildCurvePath==>i=0...dx=" + point.dx + ", dy=" + point.dy);
                } else if (i == points.size() - 1) {// 最后一个元素
                    CPoint prevPoint = points.get(i - 1);
                    point.dx = (prevPoint.x - point.x) * intensity;
                    point.dy = (prevPoint.y - point.y) * intensity;
                    Log.d(TAG, "buildCurvePath==>i=last..." + i + ", dx=" + point.dx + ", dy=" + point.dy);
                } else {
                    CPoint prevPoint = points.get(i - 1);
                    CPoint nextPoint = points.get(i + 1);
                    point.dx = (prevPoint.x - nextPoint.x) * intensity;
                    point.dy = (prevPoint.y - nextPoint.y) * intensity;
                    Log.d(TAG, "buildCurvePath==>i=other..." + i + ", dx=" + point.dx + ", dy=" + point.dy);
                }

                // 创建曲线
                if (i == 0) {
                    // moveTo不是绘制，是移动画笔到某个点（这个点作为绘制曲线时的起始点）
                    path.moveTo(point.x, point.y);
                } else {
                    CPoint prevPoint = points.get(i - 1);
                    /**
                     * 1. cubicTo绘制贝塞尔曲线
                     * path.cubicTo(x1,y1,x2,y2,x3,y3);
                     * (x1,y1)和(x2,y2)为控制点,(x3,y3)为结束点
                     *
                     * 2. cubicTo功能同quadTo，只是比quadTo多一个控制点
                     * path.quadTo(x1,y1,x2,y2);
                     * (x1,y1)为控制点，(x2,y2)为结束点
                     */
                    path.cubicTo(prevPoint.x + prevPoint.dx, prevPoint.y + prevPoint.dy,
                            prevPoint.x - prevPoint.dx, prevPoint.y - prevPoint.dy,
                            point.x, point.y);
                    Log.d(TAG, "buildCurvePath==>x1="+(prevPoint.x + prevPoint.dx)+", y1="+(prevPoint.y + prevPoint.dy)
                        +", x2="+(prevPoint.x - prevPoint.dx)+", y2="+(prevPoint.y - prevPoint.dy)
                        +", x3="+point.x+", y3="+point.y);
                }
            }
        }
    }

    /**
     * 创建路径经过的点
     *
     * @param cPoint
     * @return
     */
    private List<CPoint> createPoints(CPoint cPoint) {
        List<CPoint> points = new ArrayList<CPoint>();
        Log.d(TAG, "createPoints==>range=" + range + ", c.x=" + cPoint.x + ", c.y=" + cPoint.y);
//        Random random = new Random();
        for (int i = 0; i < quadCount; i++) {
            if (i == 0) {
                points.add(cPoint);
            } else {
                CPoint tmp = new CPoint(0, 0);
                // y轴方向上下浮动
                if (/*random.nextInt(100)*/i % 2 == 0) {
                    tmp.y = cPoint.y - /*random.nextInt(range)*/range;
                    Log.d(TAG, "ouuuuuuuu==>");
                } else {
                    tmp.y = cPoint.y + /*random.nextInt(range)*/range;
                    Log.d(TAG, "jiiiiiiii==>");
                }
                tmp.x = (int) (mWidth * ((float) (quadCount - i) / quadCount));
                Log.d(TAG, "createPoints==>x=" + tmp.x + ", y=" + tmp.y);
                points.add(tmp);
            }
        }
        return points;
    }

    private class CPoint {
        public float x = 0f;
        public float y = 0f;
        public float dx = 0f;
        public float dy = 0f;

        public CPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
