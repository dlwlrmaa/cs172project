package org.ucr.cs172project_indexer;

public class CoordinateBoundingBox {
    private double lat1;
    private double long1;
    private double lat2;
    private double long2;

    public CoordinateBoundingBox(double lt1, double lg1, double lt2, double lg2) {
        lat1 = lt1;
        long1 = lg1;
        lat2 = lt2;
        long2 = lg2;
    }

    public double[] Center()
    {
        double[] center = {0, 0};

        center[0] = lat1 + lat2 / (double) 2;
        center[1] = long1 + long2 / (double) 2;

        return center;
    }
}
