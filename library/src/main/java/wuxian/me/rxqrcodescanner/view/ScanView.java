/*
 * Copyright (C) 2008 ZXing authors
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

package wuxian.me.rxqrcodescanner.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;

import java.util.Collection;
import java.util.HashSet;

import wuxian.me.zxingscanner.R;
import wuxian.me.zxingscanner.normalversion.camera.CameraManager;

public final class ScanView extends View implements IScanView {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192,
            128, 64};
    private static final long ANIMATION_DELAY = 100L;
    private static final int OPAQUE = 0xFF;

    private final Paint paint;
    private Bitmap resultBitmap;
    private int maskColor;
    private int resultColor;
    private int resultPointColor;
    private int scannerAlpha;
    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;
    private boolean laserLinePortrait = true;
    Rect mRect;
    int i = 0;
    GradientDrawable mDrawable;
    Paint textPaint;
    private boolean showLine = true;

    // This constructor is used when the class is built from an XML resource.
    public ScanView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every
        // time in onDraw().
        paint = new Paint();
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRect = new Rect();
        int left = Color.RED;
        int center = Color.RED;
        int right = Color.RED;
        mDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{left,
                center, right}
        );
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.black_alpha50);
        resultColor = resources.getColor(R.color.black_alpha50);
        resultPointColor = resources.getColor(R.color.possible_result_points);
        scannerAlpha = 0;
        possibleResultPoints = new HashSet<ResultPoint>(5);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (CameraManager.get() == null) {
            return;
        }
        Rect frame = CameraManager.get().getFramingRectForDraw();
        if (frame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
                paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {

            paint.setColor(getContext().getResources().getColor(android.R.color.black));
            // 画出四个角
            canvas.drawRect(frame.left - 10, frame.top - 10, frame.left + 20,
                    frame.top, paint);
            canvas.drawRect(frame.left - 10, frame.top - 10, frame.left,
                    frame.top + 20, paint);
            canvas.drawRect(frame.right - 20, frame.top - 10, frame.right + 10,
                    frame.top, paint);
            canvas.drawRect(frame.right, frame.top - 10, frame.right + 10,
                    frame.top + 20, paint);

            canvas.drawRect(frame.left - 10, frame.bottom, frame.left + 20,
                    frame.bottom + 10, paint);
            canvas.drawRect(frame.left - 10, frame.bottom - 20, frame.left,
                    frame.bottom + 10, paint);
            canvas.drawRect(frame.right - 20, frame.bottom, frame.right + 10,
                    frame.bottom + 10, paint);
            canvas.drawRect(frame.right, frame.bottom - 20, frame.right + 10,
                    frame.bottom + 10, paint);

            int middle = frame.width() / 2;
            textPaint.setTextSize(20);
            textPaint.setColor(Color.WHITE);

            // Draw a red "laser scanner" line through the middle to show
            // decoding is active
            paint.setColor(Color.RED);
            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;

            // 上下走的线
            if (laserLinePortrait) {
                if (!showLine) {
                    return;
                }

                if ((i += 5) < frame.bottom - frame.top) {
                    /*
                     * canvas.drawRect(frame.left + 2, frame.top - 2 + i,
					 * frame.right - 1, frame.top + 2 + i, paint);
					 */
                    int r = 8;
                    mDrawable.setShape(GradientDrawable.RECTANGLE);
                    mDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                    setCornerRadii(mDrawable, r, r, r, r);
                    mRect.set(frame.left + 2, frame.top - 1 + i,
                            frame.right - 1, frame.top + i);
                    mDrawable.setBounds(mRect);
                    mDrawable.draw(canvas);
                    invalidate();
                } else {
                    i = 0;
                }

            } else {
                float left = frame.left + (frame.right - frame.left) / 2 - 2;
                canvas.drawRect(left, frame.top, left + 2, frame.bottom - 2,
                        paint);
            }
            Collection<ResultPoint> currentPossible = possibleResultPoints;
            Collection<ResultPoint> currentLast = lastPossibleResultPoints;
            if (currentPossible.isEmpty()) {
                lastPossibleResultPoints = null;
            } else {
                possibleResultPoints = new HashSet<ResultPoint>(5);
                lastPossibleResultPoints = currentPossible;
                paint.setAlpha(OPAQUE);
                paint.setColor(resultPointColor);
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frame.left + point.getX(), frame.top +
                            point.getY(), 6.0f, paint);
                }
            }
            if (currentLast != null) {
                paint.setAlpha(OPAQUE / 2);
                paint.setColor(resultPointColor);
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frame.left + point.getX(), frame.top +
                            point.getY(), 3.0f, paint);
                }
            }

            // Request another update at the animation interval, but only
            // repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
                    frame.right, frame.bottom);
        }
    }

    @Override
    public void drawScanFrame() {
        resultBitmap = null;
        showLine = true;
        invalidate();
    }

    @Override
    public void stopDrawScanFrame() {
        showLine = false;
    }

    public void setCornerRadii(GradientDrawable drawable, float r0, float r1,
                               float r2, float r3) {
        drawable.setCornerRadii(new float[]{r0, r0, r1, r1, r2, r2, r3, r3});
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live
     * scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    @Override
    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }

    public void setLineVisible(boolean visible) {
        showLine = visible;
        if (!visible) {
            i = 0;
        } else {
            invalidate();
        }
    }

}