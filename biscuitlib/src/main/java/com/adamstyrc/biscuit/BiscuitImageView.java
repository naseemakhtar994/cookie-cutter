package com.adamstyrc.biscuit;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by adamstyrc on 31/03/16.
 */
public class BiscuitImageView extends ImageView {

    private BiscuitParams biscuitParams;

    public BiscuitImageView(Context context) {
        super(context);
        init();
    }

    public BiscuitImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public BiscuitImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @TargetApi(value = 21)
    public BiscuitImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    public void init() {
        setScaleType(ScaleType.MATRIX);

        biscuitParams = new BiscuitParams();

        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                biscuitParams.updateWithView(getWidth(), getHeight());
                setImageCentered();
                setOnTouchListener(new BiscuitTouchListener(biscuitParams, getImageMatrix()));
            }
        });
    }

    private void setImageCentered() {
        Matrix matrix = getImageMatrix();
        Bitmap bitmap = getBitmap();

        if (bitmap != null && biscuitParams.getCircle() != null) {
            RectF drawableRect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());

            Circle circle = biscuitParams.getCircle();
            RectF viewRect;
            if (bitmap.getWidth() > bitmap.getHeight()) {
                float scale = (float) circle.getDiameter() / bitmap.getHeight();
                float scaledWidth = scale * bitmap.getWidth();
                float x = (scaledWidth - getWidth()) / 2;
                viewRect = new RectF(-x, circle.getTopBound(), getWidth() + x, circle.getBottomBound());
            } else {
                float scale = (float) circle.getDiameter() / bitmap.getWidth();
                float scaledHeight = scale * bitmap.getHeight();
                float y = (scaledHeight - getHeight()) / 2;
                viewRect = new RectF(circle.getLeftBound(), -y, circle.getRightBound(), getHeight() + y);
            }
            matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
            setImageMatrix(matrix);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Circle circle = biscuitParams.getCircle();
        if (circle == null) {
            return;
        }

        Paint paint;
        switch (biscuitParams.getShape()) {
            case CIRCLE:
                paint = biscuitParams.getCircleParams().paint;
                canvas.drawCircle(circle.getCx(), circle.getCy(), circle.getRadius(), paint);
                break;

            case HOLE:
                BiscuitParams.HoleParams hole = biscuitParams.getHoleParams();
                paint = hole.paint;
                Path path = hole.path;
                canvas.drawPath(path, paint);
                break;

            case SQUARE:
                paint = biscuitParams.getSquareParams().paint;
                canvas.drawRect(circle.getLeftBound(), circle.getTopBound(), circle.getRightBound(), circle.getBottomBound(), paint);
                break;
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);

        BitmapDrawable bitmapDrawable = (BitmapDrawable) getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        setImageBitmap(bitmap);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);

        setImageCentered();

        setOnTouchListener(new BiscuitTouchListener(biscuitParams, getImageMatrix()));
    }

    public Bitmap getCroppedBitmap() {
        Matrix matrix = getImageMatrix();

        MatrixParams matrixParams = MatrixParams.fromMatrix(matrix);
        Bitmap bitmap = getBitmap();
        Circle circle = biscuitParams.getCircle();

        int size = (int) (circle.getDiameter() / matrixParams.getScaleWidth());
        size -= 1;
        int y = getCropTop(matrixParams, circle);
        int x = getCropLeft(matrixParams, circle);

        Logger.log("x: " + x + " y: " + y + " size: " + size);
        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap,
                x,
                y,
                size,
                size);

        return croppedBitmap;
    }

    public BiscuitParams getBiscuitParams() {
        return biscuitParams;
    }

    private Bitmap getBitmap() {
        return ((BitmapDrawable) getDrawable()).getBitmap();
    }

    private int getCropLeft(MatrixParams matrixParams, Circle circle) {
        int translationX = (int) matrixParams.getX();
        int x = circle.getLeftBound() - translationX;
        x = Math.max(x, 0);
        x /= matrixParams.getScaleWidth();
        return x;
    }

    private int getCropTop(MatrixParams matrixParams, Circle circle) {
        int translationY = (int) matrixParams.getY();
        int y = circle.getTopBound() - translationY;
        y = Math.max(y, 0);
        y /= matrixParams.getScaleWidth();
        return y;
    }
}
