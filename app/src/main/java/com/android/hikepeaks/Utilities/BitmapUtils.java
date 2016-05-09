package com.android.hikepeaks.Utilities;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.TypedValue;

public final class BitmapUtils {

    public static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    public static Bitmap getCircularBitmap(Bitmap bitmap, int roundFrameColor) {
        if(bitmap == null)
            return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int radius = Math.min(height / 2, width / 2);
        int cx = width / 2;
        int cy = height / 2;
        Bitmap output = Bitmap.createBitmap(width+4, height+4, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(cx+2, cy+2, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        // draw circle frame around
        paint.setXfermode(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(roundFrameColor);
        paint.setStrokeWidth(2);
        canvas.drawCircle(cx+2, cy+2, radius, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    @ColorInt
    public static int getThemeColor
            (
                    @NonNull final Context context,
                    @AttrRes final int attributeColor
            )
    {
        
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute (attributeColor, value, true);
        return value.data;
    }
}
