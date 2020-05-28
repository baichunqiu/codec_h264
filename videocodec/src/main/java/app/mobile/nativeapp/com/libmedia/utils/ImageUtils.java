/*
 * MIT License
 *
 * Copyright (c) 2017 Huawque
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * @author:weichang
 * https://github.com/javandoc/MediaPlus
 */

package app.mobile.nativeapp.com.libmedia.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ImageUtils {

    public static Bitmap BuildWaterMark(Context ctx, String imgFileName) {
        ImageUtils utils = new ImageUtils();
        Bitmap logo = utils.readAsset(ctx, imgFileName);
        Bitmap waterMark = Bitmap.createBitmap(logo.getWidth(), logo.getHeight(), Bitmap.Config.ARGB_8888);
        utils.addLogo(waterMark, logo, 0, 0);
        return waterMark;
    }

    public static Bitmap BuildWaterMark(Context ctx, Bitmap bitmap) {
        ImageUtils utils = new ImageUtils();
        Bitmap waterMark = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        utils.addLogo(waterMark, bitmap, 0, 0);
        return waterMark;
    }

    public Bitmap readAsset(Context ctx, String fileName) {
        Bitmap image = null;
        AssetManager am = ctx.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            image = BitmapFactory.decodeStream(is, null, opts);

            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;

    }

    public Bitmap scaleBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float newWidth = width / 2;
        float newHeight = height / 2;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newbm;
    }

    public Bitmap getAlplaBitmap(Bitmap sourceImg, int number) {
        int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];
        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg.getWidth(), sourceImg.getHeight());
        number = number * 255 / 100;
        for (int i = 0; i < argb.length; i++) {
            argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);
        }
        sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg.getHeight(), Bitmap.Config.ARGB_8888);
        return sourceImg;

    }


    public boolean addText(Context ctx, Bitmap Dst, String text, int textSize, int textColor, Typeface font, int alpha, int positionX, int positionY) {

        Canvas canvas = new Canvas(Dst);
        Paint p = new Paint();
        p.setColor(textColor);
        p.setAlpha(alpha);
        p.setTypeface(font);
        p.setAntiAlias(true);//去除锯齿
        p.setFilterBitmap(true);//对位图进行滤波处理
        p.setTextSize(textSize);
        canvas.drawText(text, positionX, positionY + getFontHeight(p) / 2, p);
        return true;
    }

    public boolean addLogo(Bitmap Dst, Bitmap logo, int positionX, int positionY) {
        Canvas canvas = new Canvas(Dst);
        canvas.drawBitmap(logo, positionX, positionY, null);
        return true;
    }


    public static float getFontHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }

    public static float getFontLeading(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.leading - fm.ascent;
    }

    public static void IntArrayToByteArray(byte[] des, int[] src) {
        for (int i = 0; i < src.length; i++) {
            des[4 * i + 0] = ((byte) ((src[i] & 0xFF000000) >> 24));//A
            des[4 * i + 1] = ((byte) ((src[i] & 0x00FF0000) >> 16));//R
            des[4 * i + 2] = ((byte) ((src[i] & 0x0000FF00) >> 8));//G
            des[4 * i + 3] = ((byte) (src[i] & 0x000000FF));//B
        }
    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
