
package com.rntscprinter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.Promise;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.tscdll.TscWifiActivity;

public class RNTSCPrinterModule extends ReactContextBaseJavaModule {

    Promise promise;

    TscWifiActivity Printer = null;

    @Override
    public String getName() {
        return "RNTSCPrinter";
    }

    @ReactMethod
    public void openport(ReadableMap params, Promise promise) {
        if (Printer == null) {
	        Printer = new TscWifiActivity();
        }
        String ip = params.getString("ip");
        Integer port = params.getInt("port");
        String isScuccess = Printer.openport(ip,port);
        promise.resolve(isScuccess == "1" ? true : false);
    }

    @ReactMethod
    public void closeport(Integer delay, Promise promise) {
        Printer.closeport(delay);
        promise.resolve(true);
    }

    @ReactMethod
    public void sendcommand(String command, Promise promise) {
        Printer.sendcommand(command);
        promise.resolve(true);
    }

    @ReactMethod
    public void clearbuffer(Promise promise) {
        Printer.clearbuffer();
        promise.resolve(true);
    }

    @ReactMethod
    public void printlabel(ReadableMap params, Promise promise) {
        Integer sets = params.getInt("sets");
        Integer copies = params.getInt("copies");
        Printer.printlabel(sets, copies);
        promise.resolve(true);
    }

    @ReactMethod
    public void downloadbmp(String fileName, Promise promise) {
        Printer.downloadbmp(fileName);
        promise.resolve(true);
    }

    @ReactMethod
    public void downloadttf(String fileName, Promise promise) {
        Printer.downloadttf(fileName);
        promise.resolve(true);
    }
    
    @ReactMethod
    public void downloadpcx(String fileName, Promise promise) {
        Printer.downloadpcx(fileName);
        promise.resolve(true);
    }

    @ReactMethod
    public void sendfile(String fileName, Promise promise) {
        Printer.sendfile(fileName);
        promise.resolve(true);
    }

    @ReactMethod
    public void formfeed(Promise promise) {
        Printer.formfeed();
        promise.resolve(true);
    }

    @ReactMethod
    public void nobackfeed(Promise promise) {
        Printer.nobackfeed();
        promise.resolve(true);
    }

    @ReactMethod
    public void printerstatus(Integer timeout, Promise promise) {
        promise.resolve(
            Printer.printerstatus(timeout)
        );
    }

    @ReactMethod
    public void printerfont(ReadableMap params, Promise promise) {
        Integer a = params.getInt("x");
        Integer b = params.getInt("y");
        String c = params.getString("font");
        Integer d = params.getInt("rotation");
        Integer e = params.getInt("zoomX");
        Integer f = params.getInt("zoomY");
        String g = params.getString("text");

        Printer.printerfont(a,b,c,d,e,f,g);
        promise.resolve(true);
    }

    @ReactMethod
    public void barcode(ReadableMap params, Promise promise) {
        Integer a = params.getInt("x");
        Integer b = params.getInt("y");
        String c = params.getString("type");
        Integer d = params.getInt("height");
        Integer e = params.getBoolean("printText") ? 1 : 0;
        Integer f = params.getInt("rotation");
        Integer g = params.getInt("narrow");
        Integer h = params.getInt("wide");
        String i = params.getString("code");
        Printer.barcode(a,b,c,d,e,f,g,h,i);
        promise.resolve(true);
    }

    public Bitmap bitmap2Gray(Bitmap bmSrc) {
        int width = bmSrc.getWidth();
        int height = bmSrc.getHeight();
        Bitmap bmpGray = null;
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.0F);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmSrc, 0.0F, 0.0F, paint);
        return bmpGray;
    }

    public Bitmap gray2Binary(Bitmap graymap) {
        int width = graymap.getWidth();
        int height = graymap.getHeight();
        Bitmap binarymap = null;
        binarymap = graymap.copy(Bitmap.Config.ARGB_8888, true);

        for(int i = 0; i < width; ++i) {
            for(int j = 0; j < height; ++j) {
                int col = binarymap.getPixel(i, j);
                int alpha = col & -16777216;
                int red = (col & 16711680) >> 16;
                int green = (col & '\uff00') >> 8;
                int blue = col & 255;
                short gray = (short)((double)((float)red) * 0.3 + (double)((float)green) * 0.59 + (double)((float)blue) * 0.11);

                if (gray <= 127) {
                    gray = 0;
                } else {
                    gray = 255;
                }

                int newColor = alpha | gray << 16 | gray << 8 | gray;
                binarymap.setPixel(i, j, newColor);
            }
        }

        return binarymap;
    }

    @ReactMethod
    public void windowsFont(ReadableMap params, Promise promise ) {

        Integer x_coordinates = params.getInt("x");
        Integer y_coordinates = params.getInt("y");
        Integer fontSize = params.getInt("size");
        String path = params.getString("path");
        String textToPrint = params.getString("text");
        Integer underLine = params.getInt("underLine");
        Integer bold = params.getInt("bold");

        Bitmap original_bitmap = null;
        Bitmap gray_bitmap = null;
        Bitmap binary_bitmap = null;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(-16777216);
        paint.setAntiAlias(true);
        Typeface typeface = Typeface.createFromFile(path);
        Typeface typefaceBold = Typeface.create(typeface, bold);
        paint.setTypeface(typefaceBold);
        paint.setTextSize((float)fontSize);
        paint.setUnderlineText(underLine == 1);
        TextPaint textpaint = new TextPaint(paint);
        StaticLayout staticLayout = new StaticLayout(textToPrint, textpaint, 832, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
        int height = staticLayout.getHeight();
        int width = (int)Layout.getDesiredWidth(textToPrint, textpaint);
        if (height > 2378) {
            height = 2378;
        }

        try {
            original_bitmap = Bitmap.createBitmap(width + 8, height, Bitmap.Config.RGB_565);
            Canvas c = new Canvas(original_bitmap);
            c.drawColor(-1);
            c.translate(0.0F, 0.0F);
            staticLayout.draw(c);
        } catch (IllegalArgumentException var32) {
        } catch (OutOfMemoryError var33) {
        }

        gray_bitmap = this.bitmap2Gray(original_bitmap);
        binary_bitmap = this.gray2Binary(gray_bitmap);
        String x_axis = Integer.toString(x_coordinates);
        String y_axis = Integer.toString(y_coordinates);
        String picture_wdith = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
        String picture_height = Integer.toString(binary_bitmap.getHeight());
        String mode = Integer.toString(0);
        String command = "BITMAP " + x_axis + "," + y_axis + "," + picture_wdith + "," + picture_height + "," + mode + ",";
        byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
        int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
        int Width = binary_bitmap.getWidth();
        int Height = binary_bitmap.getHeight();

        int y;
        for(y = 0; y < Height * Width_bytes; ++y) {
            stream[y] = -1;
        }

        for(y = 0; y < Height; ++y) {
            for(int x = 0; x < Width; ++x) {
                int pixelColor = binary_bitmap.getPixel(x, y);
                int colorR = Color.red(pixelColor);
                int colorG = Color.green(pixelColor);
                int colorB = Color.blue(pixelColor);
                int total = (colorR + colorG + colorB) / 3;
                if (total == 0) {
                    stream[y * ((Width + 7) / 8) + x / 8] ^= (byte)(128 >> x % 8);
                }
            }
        }

        Printer.sendcommand(command);
        Printer.sendcommand(stream);
        Printer.sendcommand("\r\n");
        promise.resolve(true);
    }
}