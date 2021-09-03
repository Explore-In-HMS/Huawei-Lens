/*
 * Copyright 2020. Explore in HMS. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.hms.referenceapp.huaweilens.dsc.cropper;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

/**
 * Animation to handle smooth cropping image matrix transformation change, specifically for
 * zoom-in/out.
 */
final class CropImageAnimation extends Animation implements Animation.AnimationListener {

    // region: Fields and Consts

    private final ImageView mImageView;

    private final CropOverlayView mCropOverlayView;

    private final float[] mStartBoundPoints = new float[8];

    private final float[] mEndBoundPoints = new float[8];

    private final RectF mStartCropWindowRect = new RectF();

    private final RectF mEndCropWindowRect = new RectF();

    private final float[] mStartImageMatrix = new float[9];

    private final float[] mEndImageMatrix = new float[9];

    private final RectF mAnimRect = new RectF();

    private final float[] mAnimPoints = new float[8];

    private final float[] mAnimMatrix = new float[9];
    // endregion

    public CropImageAnimation(ImageView cropImageView, CropOverlayView cropOverlayView) {
        mImageView = cropImageView;
        mCropOverlayView = cropOverlayView;

        setDuration(300);
        setFillAfter(true);
        setInterpolator(new AccelerateDecelerateInterpolator());
        setAnimationListener(this);
    }

    public void setStartState(float[] boundPoints, Matrix imageMatrix) {
        reset();
        System.arraycopy(boundPoints, 0, mStartBoundPoints, 0, 8);
        mStartCropWindowRect.set(mCropOverlayView.getCropWindowRect());
        imageMatrix.getValues(mStartImageMatrix);
    }

    public void setEndState(float[] boundPoints, Matrix imageMatrix) {
        System.arraycopy(boundPoints, 0, mEndBoundPoints, 0, 8);
        mEndCropWindowRect.set(mCropOverlayView.getCropWindowRect());
        imageMatrix.getValues(mEndImageMatrix);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {

        mAnimRect.left =
                mStartCropWindowRect.left
                        + (mEndCropWindowRect.left - mStartCropWindowRect.left) * interpolatedTime;
        mAnimRect.top =
                mStartCropWindowRect.top
                        + (mEndCropWindowRect.top - mStartCropWindowRect.top) * interpolatedTime;
        mAnimRect.right =
                mStartCropWindowRect.right
                        + (mEndCropWindowRect.right - mStartCropWindowRect.right) * interpolatedTime;
        mAnimRect.bottom =
                mStartCropWindowRect.bottom
                        + (mEndCropWindowRect.bottom - mStartCropWindowRect.bottom) * interpolatedTime;
        mCropOverlayView.setCropWindowRect(mAnimRect);

        for (int i = 0; i < mAnimPoints.length; i++) {
            mAnimPoints[i] =
                    mStartBoundPoints[i] + (mEndBoundPoints[i] - mStartBoundPoints[i]) * interpolatedTime;
        }
        mCropOverlayView.setBounds(mAnimPoints, mImageView.getWidth(), mImageView.getHeight());

        for (int i = 0; i < mAnimMatrix.length; i++) {
            mAnimMatrix[i] =
                    mStartImageMatrix[i] + (mEndImageMatrix[i] - mStartImageMatrix[i]) * interpolatedTime;
        }
        Matrix m = mImageView.getImageMatrix();
        m.setValues(mAnimMatrix);
        mImageView.setImageMatrix(m);

        mImageView.invalidate();
        mCropOverlayView.invalidate();
    }

    @Override
    public void onAnimationStart(Animation animation) {
        Log.d("2233","animationStart");
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        mImageView.clearAnimation();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        Log.d("2233","animationRepeat");
    }
}
