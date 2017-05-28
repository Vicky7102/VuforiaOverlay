package com.maryamaj.overlay.utils;

import android.content.Context;

import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.models.SketchPoint;

import java.util.List;

import io.realm.RealmList;

public class GeometryUtils {

    public static boolean contains(List<Point2D> areaPoints, Point2D point)
    {
        boolean oddTransitions = false;
        for( int i = 0, j = areaPoints.size() - 1; i < areaPoints.size(); j = i++ )
        {
            if( ( areaPoints.get(i).y < point.y && areaPoints.get(j).y >= point.y ) || ( areaPoints.get(j).y < point.y && areaPoints.get(i).y >= point.y ) )
            {
                if( areaPoints.get(i).x + ( point.y - areaPoints.get(i).y ) / ( areaPoints.get(j).y - areaPoints.get(i).y ) * ( areaPoints.get(j).x - areaPoints.get(i).x ) < point.x )
                {
                    oddTransitions = !oddTransitions;
                }
            }
        }
        return oddTransitions;
    }

    public static Point2D translate(Point2D point, Point2D translation) {
        return new Point2D(point.x + translation.x, point.y + translation.y);
    }

    public static Point2D scale(Point2D point, float scale) {
        return new Point2D(point.x * scale, point.y * scale);
    }

    public static RealmList<SketchPoint> scaleSketchPoints(List<SketchPoint> points, float scale) {
        RealmList<SketchPoint> newPos = new RealmList<>();
        for (SketchPoint point: points) {
            SketchPoint translated = new SketchPoint(point.x * scale, point.y * scale, point.isInitial());
            newPos.add(translated);
        }
        return newPos;
    }

    public static RealmList<Point2D> translateArray(List<Point2D> points, Point2D translation) {
        RealmList<Point2D> newPos = new RealmList<>();
        for (Point2D point: points) {
            Point2D translated = translate(point, translation);
            newPos.add(translated);
        }
        return newPos;
    }

    public static RealmList<SketchPoint> translateSketchPoints(List<SketchPoint> points, Point2D translation) {
        RealmList<SketchPoint> newPos = new RealmList<>();
        for (SketchPoint point: points) {
            SketchPoint translated = new SketchPoint(point.x + translation.x, point.y + translation.y, point.isInitial());
            newPos.add(translated);
        }
        return newPos;
    }

    public static double distance(Point2D p1, Point2D p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    public static int dpToPx(Context context, int sizeDp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (sizeDp * scale + 0.5f);
    }
}
