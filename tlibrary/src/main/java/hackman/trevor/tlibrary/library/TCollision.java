package hackman.trevor.tlibrary.library;

// Library for geometry and collision calculations
public final class TCollision {
    private TCollision() {}

    public static double distanceBetweenLineAndPoint(double lineX1, double lineY1, double lineX2, double lineY2, double pointX, double pointY) {
        return Math.abs((lineY2 - lineY1) * pointX - (lineX2 - lineX1) * pointY + lineX2 * lineY1 - lineY2 * lineX1)
                / Math.pow(Math.pow(lineY2 - lineY1, 2) + Math.pow(lineX2 - lineX1, 2), 0.5);
    }

    public static boolean collisionBetweenCircleAndSquare(double circleX, double circleY, double radius, double blockX, double blockY, double blockSize) {
        // We want to get the circle's position relative to the square
        // Abs too b/c symmetry makes 1 check possible
        double y = Math.abs(circleY - blockY);
        double x = Math.abs(circleX - blockX);

        // Square in Circle - Weird True
        // This check is only necessary if circle is significantly larger than square, may be able to improve efficiency by that
        if (x * x + y * y <= radius * radius)
            return true;
        // Easy False
        if (y > blockSize / 2 + radius || x > blockSize / 2 + radius)
            return false;
        // Hard False
        return !(y > blockSize / 2) || !(x > blockSize / 2) || !((x - blockSize / 2) * (x - blockSize / 2) + (y - blockSize / 2) * (y - blockSize / 2) > radius * radius);
    }

    public static boolean collisionBetweenCircleAndRectangle(double circleX, double circleY, double radius, double blockX, double blockY, double blockWidth, double blockHeight) {
        // We want to get the circle's position relative to the square
        // Abs too b/c symmetry makes 1 check possible
        double y = Math.abs(circleY - blockY);
        double x = Math.abs(circleX - blockX);

        // Square in Circle - Weird True
        // This check is only necessary if circle is significantly larger than square, may be able to improve efficiency by that
        if (x * x + y * y <= radius * radius)
            return true;
        // Easy False
        if (y > blockHeight / 2 + radius || x > blockWidth / 2 + radius)
            return false;
        // Hard False
        return !(y > blockHeight / 2) || !(x > blockWidth / 2) || !((x - blockWidth / 2) * (x - blockWidth / 2) + (y - blockHeight / 2) * (y - blockHeight / 2) > radius * radius);
    }
}
