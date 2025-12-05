package com.example.serviciospublicos.utils;

import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class GeometryUtils {

    public static double distanceMeters(double lat1, double lng1,
                                        double lat2, double lng2) {
        double R = 6371000.0; // radio Tierra en metros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static boolean isPointInPolygon(double lat, double lng,
                                           List<GeoPoint> polygon) {
        if (polygon == null || polygon.size() < 3) return false;

        boolean inside = false;
        int n = polygon.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = polygon.get(i).getLatitude();
            double yi = polygon.get(i).getLongitude();
            double xj = polygon.get(j).getLatitude();
            double yj = polygon.get(j).getLongitude();

            boolean intersect = ((yi > lng) != (yj > lng)) &&
                    (lat < (xj - xi) * (lng - yi) / (yj - yi + 0.0) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    }
}
