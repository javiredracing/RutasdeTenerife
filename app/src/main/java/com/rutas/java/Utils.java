package com.rutas.java;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by jfernandez on 14/07/2015.
 */
public class Utils {
    private final static int TYPE_GR = 3;
    private final static int TYPE_PR = 2;
    private final static int TYPE_SL = 1;
    private final static int TYPE_REGULAR = 0;

    protected static final int PURCHASE_CODE_REQUEST = 10001;
    protected static final String SKU_PREMIUM = "android.test.purchased";

    protected static final int REQUEST_INVITE = 10002;

    protected static Bitmap exportBitmap(Context ctx, Bitmap bitmap, String text){
        bitmap = Utils.resizeBitmap(bitmap);
        Resources res = ctx.getResources();
        Bitmap icon = BitmapFactory.decodeResource(res, R.drawable.logo_title);

        float scale = res.getDisplayMetrics().scaledDensity;
        Bitmap.Config config = bitmap.getConfig();
        if (config == null)
            config = Bitmap.Config.ARGB_8888;
        bitmap = bitmap.copy(config,true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize((int) (10 * scale));

        canvas.drawBitmap(icon, 0, 0, paint);

        //String title = res.getString(R.string.app_name);

        /*Rect boundsTitle = new Rect();
        paint.getTextBounds(title,0,title.length(), boundsTitle);
        canvas.drawText(title, icon.getWidth() + 5 , boundsTitle.height()+ (icon.getHeight()/2), paint);*/

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        canvas.drawText(text, 5, icon.getHeight() + bounds.height(), paint);

        return Utils.bitmapToJpg(bitmap);
    }

    private static Bitmap bitmapToJpg(Bitmap item){
        //Bitmap resizedItem = Utils.resizeBitmap(item);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        item.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        return item;
    }

    private static Bitmap resizeBitmap(Bitmap bitmap){
        final int MAX_SIZE = 640;

        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();
        int outHeight = inHeight;
        int outWidth = inWidth;
        if (inWidth > MAX_SIZE || inHeight > MAX_SIZE) {
            if (inWidth > inHeight) {
                outWidth = MAX_SIZE;
                outHeight = Math.round((inHeight * MAX_SIZE) / inWidth);
            } else {
                outHeight = MAX_SIZE;
                outWidth = Math.round((inWidth * MAX_SIZE) / inHeight);
            }
        }
       /* Matrix matrix = new Matrix();
        matrix.postScale(outWidth,outHeight);
        return Bitmap.createBitmap(bitmap,  0, 0, inWidth, inHeight,matrix,false);*/
        return Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
    }

    protected static LatLngBounds centerOnPath(List<LatLng> pointList){
        //List<LatLng> pointList = pathShowed.getPoints();
        int size = pointList.size();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (int i = 0; i < size; i= i+10){
            boundsBuilder.include(pointList.get(i));
        }
        boundsBuilder.include(pointList.get(size - 1));
        return boundsBuilder.build();
    }

    protected static int getIconBigger(Route route){
        int drawable = route.approved();
        switch (drawable){
            case TYPE_GR:
                drawable = R.drawable.marker_sign_24_red;
                break;
            case TYPE_PR:
                drawable = R.drawable.marker_sign_24_yellow;
                break;
            case TYPE_SL:
                drawable = R.drawable.marker_sign_24_green;
                break;
            case TYPE_REGULAR:
                drawable = R.drawable.marker_sign_24_normal;
                break;
            default:
                drawable = R.drawable.marker_sign_24_normal;
        }
        return drawable;
    }

    protected static int selectColor(int type, Context ctx){
        int color = Color.BLUE;
        switch (type){
            case TYPE_GR:
                color = ctx.getResources().getColor(R.color.pathRed);
                break;
            case TYPE_PR:
                color = ctx.getResources().getColor(R.color.pathYellow);
                break;
            case TYPE_SL:
                color = ctx.getResources().getColor(R.color.pathGreen);
                break;
            case TYPE_REGULAR:
                color = ctx.getResources().getColor(R.color.pathBrown);
                break;
        }
        return color;
    }
        /*private boolean isInRange(int azimuth, int angle){
        int azimuthInverse = (360 - azimuth);
        final int RANGE = 30;	//30 degrees in each side = 60

        int from = azimuthInverse - RANGE;
        if (from < 0)
            from = 360 - from;
        int to = (azimuthInverse + RANGE) % 360;

        if(from > to){
            return ((angle > from) || ( angle < to));
        } else if ( to > from){
            return ((angle < to) && ( angle > from));
        } else // to == from
            return (angle == to);
        return true;
    }*/

    protected static File assetsToStorage(String fileName,  Context ctx){
        File toPath = Environment.getExternalStoragePublicDirectory(ctx.getString(R.string.app_name).replace(" ", ""));
        //File toPath = new File(Environment.getExternalStorageDirectory() +File.separator + ctx.getString(R.string.app_name).replace(" ", ""));
      /*  try {
            toPath.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        if (!toPath.exists()) {
            toPath.mkdir();
        }
        File toFile = new File(toPath,fileName);
        Log.v("File "+ toFile.getAbsolutePath()+" exist", ""+toFile.exists());
        if (!toFile.exists()){

            try {
                InputStream inputStream = ctx.getAssets().open(fileName);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                copyAssetFile(bufferedReader, toFile);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return toFile;
    }

    private static void copyAssetFile(BufferedReader br, File toFile) throws IOException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(toFile));

            int in;
            while ((in = br.read()) != -1) {
                bw.write(in);
            }
        } finally {
            if (bw != null) {
                bw.close();
            }
            br.close();
        }
    }
}
