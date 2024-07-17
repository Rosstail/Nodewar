package fr.rosstail.nodewar;

public class PointInPolygon {

    public static void main(String[] args) {
        // Les coordonnées du point que vous souhaitez tester
        double testX = 2.5;
        double testY = 3.5;

        // Les coordonnées des sommets du polygone
        double[] polygonX = {1.0, 4.0, 4.0, 1.0};
        double[] polygonY = {1.0, 1.0, 4.0, 4.0};

        boolean isInside = isPointInsidePolygon(testX, testY, polygonX, polygonY);
        //System.out.println("Le point est à l'intérieur du polygone : " + isInside);
    }

    public static boolean isPointInsidePolygon(double x, double y, double[] polygonX, double[] polygonY) {
        int numVertices = polygonX.length;
        int intersections = 0;

        for (int i = 0; i < numVertices; i++) {
            int j = (i + 1) % numVertices;
            double x1 = polygonX[i];
            double y1 = polygonY[i];
            double x2 = polygonX[j];
            double y2 = polygonY[j];

            if (y1 < y && y2 >= y || y2 < y && y1 >= y) {
                if (x1 + (y - y1) / (y2 - y1) * (x2 - x1) < x) {
                    intersections++;
                }
            }
        }

        return intersections % 2 == 1;
    }
}