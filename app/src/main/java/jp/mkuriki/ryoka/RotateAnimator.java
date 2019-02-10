package jp.mkuriki.ryoka;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class RotateAnimator extends Animation {
	
    private Camera camera;

    // 表面のView
    private View frontView;
    // 裏面のView
    private View BackView;
    private float centerX;
    private float centerY;
    private boolean forward = true;
    private boolean visibilitySwapped;

    public RotateAnimator(View frontView, View BackView, int centerX, int centerY) {
        this.frontView = frontView;
        this.BackView = BackView;
        this.centerX = centerX;
        this.centerY = centerY;

        setDuration(2000);
        setFillAfter(true);
        setInterpolator(new AccelerateDecelerateInterpolator());
    }

    public void reverse() {
        forward = false;
        View temp = BackView;
        BackView = frontView;
        frontView = temp;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        camera = new Camera();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final double radians = Math.PI * interpolatedTime;
        float degrees = (float) (180.0 * radians / Math.PI);

        if (interpolatedTime >= 0.5f) {
            degrees -= 180.f;

            if (!visibilitySwapped) {
                frontView.setVisibility(View.GONE);
                BackView.setVisibility(View.VISIBLE);
                visibilitySwapped = true;
            }
        }

        if (!forward)
            degrees = -degrees;

        final Matrix matrix = t.getMatrix();

        camera.save();
        camera.rotateY(degrees); // ここをrotateX()にすると、縦方向に回転
        camera.getMatrix(matrix);
        camera.restore();

        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
    }
}