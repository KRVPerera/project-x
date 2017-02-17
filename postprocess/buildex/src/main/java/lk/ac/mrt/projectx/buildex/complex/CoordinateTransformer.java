package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.complex.cordinates.PolarCoordinate;
import lk.ac.mrt.projectx.buildex.models.Pair;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

import java.util.List;

/**
 * @author Chathura Widanage
 */
public class CoordinateTransformer {

    public static void cartesianToCenter(int width, int height, CartesianCoordinate cartesianCoordinate) {
        cartesianCoordinate.setX(cartesianCoordinate.getX() - (width / 2));
        cartesianCoordinate.setY(cartesianCoordinate.getY() - (height / 2));
    }


    public static void cartesianToCenter(int width, int height, List<Pair<CartesianCoordinate, CartesianCoordinate>> pairs) {
        for (Pair<CartesianCoordinate, CartesianCoordinate> p : pairs) {
            cartesianToCenter(width, height, p.first);
            cartesianToCenter(width, height, p.second);
        }
    }


    public static void cartesianToCorner(int width, int height, CartesianCoordinate cartesianCoordinate) {
        cartesianCoordinate.setX(cartesianCoordinate.getX() + (width / 2));
        cartesianCoordinate.setY(cartesianCoordinate.getY() + (height / 2));
    }

    public static PolarCoordinate cartesian2Polar(int width, int height, CartesianCoordinate cartesianCoordinate) {
        CartesianCoordinate centered = new CartesianCoordinate(
                cartesianCoordinate.getX() - (width / 2),
                cartesianCoordinate.getY() - (height / 2)
        );
        PolarCoordinate polarCoordinate = cartesian2Polar(centered);
        return polarCoordinate;
    }

    public static PolarCoordinate cartesian2Polar(CartesianCoordinate cartesianCoordinate) {
        return new PolarCoordinate(
                atan2(cartesianCoordinate.getY(), cartesianCoordinate.getX()),
                Math.hypot(cartesianCoordinate.getX(), cartesianCoordinate.getY()));
    }


    public static CartesianCoordinate polar2Cartesian(int width, int height, PolarCoordinate polarCoordinate) {
        double x = polarCoordinate.getR() * Math.cos(polarCoordinate.getTheta());
        double y = polarCoordinate.getR() * Math.sin(polarCoordinate.getTheta());
        return new CartesianCoordinate(x + (width / 2), y + (height / 2));
    }

    public static CartesianCoordinate polar2Cartesian(PolarCoordinate polarCoordinate) {
        double x = polarCoordinate.getR() * Math.cos(polarCoordinate.getTheta());
        double y = polarCoordinate.getR() * Math.sin(polarCoordinate.getTheta());
        return new CartesianCoordinate(x, y);
    }

    public static double atan2(double y, double x) {
        double theta = Math.atan2(y, x);
        return theta < 0 ? theta + (Math.PI * 2) : theta;
    }

}
