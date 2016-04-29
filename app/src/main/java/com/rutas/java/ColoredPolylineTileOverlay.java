package com.rutas.java;

/**
 * Created by jfernandez on 28/04/2016.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;

import java.io.ByteArrayOutputStream;
import java.util.List;


/**
 * Tile overlay used to display a colored polyline as a replacement for the non-existence of gradient
 * polylines for google maps
 */
public class ColoredPolylineTileOverlay<T extends ColoredPolylineTileOverlay.PointHolder> implements TileProvider {

    public static final double LOW_SPEED_CLAMP_KMpH = 0;
    public static final double LOW_SPEED_CLAMP_MpS = 0;
    // TODO: calculate speed as highest speed of pointsCollection
    public static final double HIGH_SPEED_CLAMP_KMpH = 2800;
    public static final double HIGH_SPEED_CLAMP_MpS = 2800;//HIGH_SPEED_CLAMP_KMpH * 1000 / (60 * 60);
    public static final int BASE_TILE_SIZE = 256;

    public static int[] getSpeedColors(Context context) {
        return new int[] {
                context.getResources().getColor(R.color.pathGreen),
                context.getResources().getColor(R.color.pathYellow),
                context.getResources().getColor(R.color.pathRed)
        };
    }

    public static float getSpeedProportion(double metersPerSecond) {
        return (float)(Math.max(Math.min(metersPerSecond, HIGH_SPEED_CLAMP_MpS), LOW_SPEED_CLAMP_MpS) / HIGH_SPEED_CLAMP_MpS);
    }

    public static int interpolateColor(int[] colors, float proportion) {
        int rTotal = 0, gTotal = 0, bTotal = 0;
        // We correct the ratio to colors.length - 1 so that
        // for i == colors.length - 1 and p == 1, then the final ratio is 1 (see below)
        float p = proportion * (colors.length - 1);

        for (int i = 0; i < colors.length; i++) {
            // The ratio mostly resides on the 1 - Math.abs(p - i) calculation :
            // Since for p == i, then the ratio is 1 and for p == i + 1 or p == i -1, then the ratio is 0
            // This calculation works BECAUSE p lies within [0, length - 1] and i lies within [0, length - 1] as well
            float iRatio = Math.max(1 - Math.abs(p - i), 0.0f);
            rTotal += (int)(Color.red(colors[i]) * iRatio);
            gTotal += (int)(Color.green(colors[i]) * iRatio);
            bTotal += (int)(Color.blue(colors[i]) * iRatio);
        }

        return Color.rgb(rTotal, gTotal, bTotal);
    }

    protected final Context context;
    protected final PointCollection<T> pointsCollection;
    protected final int[] speedColors;
    protected final float density;
    protected final int tileDimension;
    protected final SphericalMercatorProjection projection;

    // Caching calculation-related stuff
    protected LatLng[] trailLatLngs;
    protected Point[] projectedPts;
    protected Point[] projectedPtMids;
    protected double[] speeds;

    public ColoredPolylineTileOverlay(Context context, PointCollection pointsCollection) {
        super();

        this.context = context;
        this.pointsCollection = pointsCollection;
        speedColors = getSpeedColors(context);
        density = context.getResources().getDisplayMetrics().density;
        tileDimension = (int)(BASE_TILE_SIZE * density);
        projection = new SphericalMercatorProjection(BASE_TILE_SIZE);
        calculatePointsAndSpeeds();
    }
    public void calculatePointsAndSpeeds() {
        trailLatLngs = new LatLng[pointsCollection.getPoints().size()];
        projectedPts = new Point[pointsCollection.getPoints().size()];
       // projectedPtMids = new Point[Math.max(pointsCollection.getPoints().size() - 1, 0)];
        speeds = new double[Math.max(pointsCollection.getPoints().size() - 1, 0)];

        List<T> points = pointsCollection.getPoints();
        int size = points.size();
        for (int i = 0; i < size; i++) {
            T point = points.get(i);
            LatLng latLng = point.getLatLng();
            trailLatLngs[i] = latLng;
            projectedPts[i] = projection.toPoint(latLng);

            // Mids
            if (i > 0) {
              /*  LatLng previousLatLng = points.get(i - 1).getLatLng();
                LatLng latLngMid = SphericalUtil.interpolate(previousLatLng, latLng, 0.5);
                projectedPtMids[i - 1] = projection.toPoint(latLngMid);*/

                //T previousPoint = points.get(i - 1);
                //double speed = SphericalUtil.computeDistanceBetween(latLng, previousLatLng) / ((point.getTime() - previousPoint.getTime()) / 1000.0);
                double speed = point.getTime();
                speeds[i - 1] = speed;
            }
        }
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        // Because getTile can be called asynchronously by multiple threads, none of the info we keep in the class will be modified
        // (getTile is essentially side-effect-less) :
        // Instead, we create the bitmap, the canvas and the paints specifically for the call to getTile

        Bitmap bitmap = Bitmap.createBitmap(tileDimension, tileDimension, Bitmap.Config.ARGB_8888);

        // Normally, instead of the later calls for drawing being offset, we would offset them using scale() and translate() right here
        // However, there seems to be funky issues related to float imprecisions that happen at large scales when using this method, so instead
        // The points are offset properly when drawing
        Canvas canvas = new Canvas(bitmap);

        Matrix shaderMat = new Matrix();
        Paint gradientPaint = new Paint();
      /*  gradientPaint.setStyle(Paint.Style.STROKE);
        gradientPaint.setStrokeWidth(3f * density);
        gradientPaint.setStrokeCap(Paint.Cap.BUTT);
        //gradientPaint.setStrokeJoin(Paint.Join.ROUND);
        //gradientPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        gradientPaint.setShader(new LinearGradient(0, 0, 1, 0, speedColors, null, Shader.TileMode.CLAMP));
        gradientPaint.getShader().setLocalMatrix(shaderMat);*/

        Paint colorPaint = new Paint();
        colorPaint.setStyle(Paint.Style.STROKE);
        colorPaint.setStrokeWidth(3f * density);
        colorPaint.setStrokeCap(Paint.Cap.BUTT);
       // colorPaint.setStrokeJoin(Paint.Join.ROUND);
        //colorPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        // See https://developers.google.com/maps/documentation/android/views#zoom for handy info regarding what zoom is
        float scale = (float)(Math.pow(2, zoom) * density);

        renderTrail(canvas, shaderMat, gradientPaint, colorPaint, scale, x, y);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return new Tile(tileDimension, tileDimension, baos.toByteArray());
    }

    public void renderTrail(Canvas canvas, Matrix shaderMat, Paint gradientPaint, Paint colorPaint, float scale, int x, int y) {
        List<T> points = pointsCollection.getPoints();
        double speed1, speed2;
        MutPoint pt1 = new MutPoint(), pt2 = new MutPoint(), pt3 = new MutPoint(), pt1mid2 = new MutPoint(), pt2mid3 = new MutPoint();

        // Guard statement: if the trail is only 1 point, just render the point by itself as a speed of 0
        if (points.size() == 1) {
            pt1.set(projectedPts[0], scale, x, y, tileDimension);
            speed1 = 0;
            float speedProp = getSpeedProportion(speed1);

            colorPaint.setStyle(Paint.Style.FILL);
            colorPaint.setColor(interpolateColor(speedColors, speedProp));
            canvas.drawCircle((float) pt1.x, (float) pt1.y, colorPaint.getStrokeWidth() / 2f, colorPaint);
            colorPaint.setStyle(Paint.Style.STROKE);

            return;
        }

        // Guard statement: if the trail is exactly 2 points long, just render a line from A to B at d(A, B) / t speed
        if (points.size() == 2) {
            pt1.set(projectedPts[0], scale, x, y, tileDimension);
            pt2.set(projectedPts[1], scale, x, y, tileDimension);
            speed1 = speeds[0];
            float speedProp = getSpeedProportion(speed1);

            drawLine(canvas, colorPaint, pt1, pt2, speedProp);

            return;
        }
        int size = points.size();
        if (size > 1) {
            for (int i = 1; i < size; i++) {
                pt1.set(projectedPts[i - 1], scale, x, y, tileDimension);
                pt2.set(projectedPts[i], scale, x, y, tileDimension);
                if ((pt1.x == pt2.x) &&(pt1.y == pt2.y)){
                    Log.v("Equal","YES!");
                    continue;
                }
                speed1 = speeds[i-1];
                float speedProp = getSpeedProportion(speed1);

                drawLine(canvas, colorPaint, pt1, pt2, speedProp);
            }
            return;
        }
        // Because we want to be displaying speeds as color ratios, we need multiple points to do it properly:
        // Since we use calculate the speed using the distance and the time, we need at least 2 points to calculate the distance;
        // this means we know the speed for a segment, not a point.
        // Furthermore, since we want to be easing the color changes between every segment, we have to use 3 points to do the easing;
        // every line is split into two, and we ease over the corners
        // This also means the first and last corners need to be extended to include the first and last points respectively
        // Finally (you can see about that in getTile()) we need to offset the point projections based on the scale and x, y because
        // weird display behaviour occurs
       // int count = 0;
       /* for (int i = 2; i < points.size(); i++) {
            pt1.set(projectedPts[i - 2], scale, x, y, tileDimension);
            pt2.set(projectedPts[i - 1], scale, x, y, tileDimension);
            pt3.set(projectedPts[i], scale, x, y, tileDimension);

            // Because we want to split the lines in two to ease over the corners, we need the middle points
            pt1mid2.set(projectedPtMids[i - 2], scale, x, y, tileDimension);
            pt2mid3.set(projectedPtMids[i - 1], scale, x, y, tileDimension);

            // The speed is calculated in meters per second (same format as the speed clamps); because getTime() is in millis, we need to correct for that
            speed1 = speeds[i - 2];
            speed2 = speeds[i - 1];
            float speed1Prop = getSpeedProportion(speed1);
            float speed1to2Prop = getSpeedProportion((speed1 + speed2) / 2);
            float speed2Prop = getSpeedProportion(speed2);

            // Circle for the corner (removes the weird empty corners that occur otherwise)
            colorPaint.setStyle(Paint.Style.FILL);
            colorPaint.setColor(interpolateColor(speedColors, speed1to2Prop));
            canvas.drawCircle((float)pt2.x, (float)pt2.y, colorPaint.getStrokeWidth() / 2f, colorPaint);
            colorPaint.setStyle(Paint.Style.STROKE);

            // Corner
            // Note that since for the very first point and the very last point we don't split it in two, we used them instead.
            drawLine(canvas, shaderMat, gradientPaint, colorPaint, i - 2 == 0 ? pt1 : pt1mid2, pt2, speed1Prop, speed1to2Prop);
            drawLine(canvas, shaderMat, gradientPaint, colorPaint, pt2, i == points.size() - 1 ? pt3 : pt2mid3, speed1to2Prop, speed2Prop);
        }*/
       // Log.v("Item saved", ""+count);
    }

    /**
     * Note: it is assumed the shader is 0, 0, 1, 0 (horizontal) so that it lines up with the rotation
     * (rotations are usually setup so that the angle 0 points right)
     * Complex method
     */
    public void drawLine(Canvas canvas, Matrix shaderMat, Paint gradientPaint, Paint colorPaint, MutPoint pt1, MutPoint pt2, float ratio1, float ratio2) {
        // Degenerate case: both ratios are the same; we just handle it using the colorPaint (handling it using the shader is just messy and ineffective)
        if (ratio1 == ratio2) {
            drawLine(canvas, colorPaint, pt1, pt2, ratio1);
            return;
        }
        shaderMat.reset();

        // PS: don't ask me why this specfic orders for calls works but other orders will fuck up
        // Since every call is pre, this is essentially ordered as (or my understanding is that it is):
        // ratio translate -> ratio scale -> scale to pt length -> translate to pt start -> rotate
        // (my initial intuition was to use only post calls and to order as above, but it resulted in odd corruptions)

        // Setup based on points:
        // We translate the shader so that it is based on the first point, rotated towards the second and since the length of the
        // gradient is 1, then scaling to the length of the distance between the points makes it exactly as long as needed
        shaderMat.preRotate((float) Math.toDegrees(Math.atan2(pt2.y - pt1.y, pt2.x - pt1.x)), (float)pt1.x, (float)pt1.y);
        shaderMat.preTranslate((float)pt1.x, (float)pt1.y);
        float scale = (float)Math.sqrt(Math.pow(pt2.x - pt1.x, 2) + Math.pow(pt2.y - pt1.y, 2));
        shaderMat.preScale(scale, scale);

        // Setup based on ratio
        // By basing the shader to the first ratio, we ensure that the start of the gradient corresponds to it
        // The inverse scaling of the shader means that it takes the full length of the call to go to the second ratio
        // For instance; if d(ratio1, ratio2) is 0.5, then the shader needs to be twice as long so that an entire call (1)
        // Results in only half of the gradient being used
        shaderMat.preScale(1f / (ratio2 - ratio1), 1f / (ratio2 - ratio1));
        shaderMat.preTranslate(-ratio1, 0);

        gradientPaint.getShader().setLocalMatrix(shaderMat);

        canvas.drawLine(
                (float)pt1.x,
                (float)pt1.y,
                (float)pt2.x,
                (float)pt2.y,
                gradientPaint
        );
    }
    /*Simple method*/
    public void drawLine(Canvas canvas, Paint colorPaint, MutPoint pt1, MutPoint pt2, float ratio) {
        colorPaint.setColor(interpolateColor(speedColors, ratio));
        canvas.drawLine(
                (float)pt1.x,
                (float)pt1.y,
                (float)pt2.x,
                (float)pt2.y,
                colorPaint
        );
    }

    public interface PointCollection<T extends PointHolder> {
        List<T> getPoints();
    }
    public interface PointHolder {
        LatLng getLatLng();
        long getTime();
    }
    public static class MutPoint {
        public double x, y;

        public MutPoint set(Point point, float scale, int x, int y, int tileDimension) {
            this.x = point.x * scale - x * tileDimension;
            this.y = point.y * scale - y * tileDimension;
            return this;
        }
    }
}