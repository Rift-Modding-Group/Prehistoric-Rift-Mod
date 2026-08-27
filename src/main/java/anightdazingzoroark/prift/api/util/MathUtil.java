package anightdazingzoroark.prift.api.util;

import java.util.Random;

public class MathUtil {
    public static double slopeResult(int x, boolean clamped, double xMin, double xMax, double yMin, double yMax) {
        return slopeResult((double) x, clamped, xMin, xMax, yMin, yMax);
    }

    public static double slopeResult(double x, boolean clamped, double xMin, double xMax, double yMin, double yMax) {
        double slope = (yMax - yMin)/(xMax - xMin);
        if (clamped) {
            if (yMin <= yMax) return Math.clamp(slope * (x - xMin) + yMin, yMin, yMax);
            else return Math.clamp(slope * (x - xMin) + yMin, yMax, yMin);
        }
        return slope * (x - xMin) + yMin;
    }

    public static float slopeResult(int x, boolean clamped, float xMin, float xMax, float yMin, float yMax) {
        float slope = (yMax - yMin)/(xMax - xMin);
        if (clamped) {
            if (yMin <= yMax) return Math.clamp(slope * (x - xMin) + yMin, yMin, yMax);
            else return Math.clamp(slope * (x - xMin) + yMin, yMax, yMin);
        }
        return slope * (x - xMin) + yMin;
    }

    @Deprecated //new java 25 rand features replaces this
    public static int randomInRange(Random random, int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static double randomInRange(Random random, double min, double max) {
        return min + random.nextDouble() * (max - min);
    }
}
