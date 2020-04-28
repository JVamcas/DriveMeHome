package com.owambo.jvamcas.drivemehome.utils.transform;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.owambo.jvamcas.drivemehome.utils.Const;
import com.squareup.picasso.Transformation;

public class CircleTransformation implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {

        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squareBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squareBitmap != source)
            source.recycle();
        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squareBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        squareBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return Const.CIRCLE;
    }
}
