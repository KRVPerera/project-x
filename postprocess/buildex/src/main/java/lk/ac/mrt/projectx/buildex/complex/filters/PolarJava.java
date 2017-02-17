package lk.ac.mrt.projectx.buildex.complex.filters;

import lk.ac.mrt.projectx.buildex.complex.CoordinateTransformer;
import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.complex.cordinates.PolarCoordinate;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

import java.awt.image.BufferedImage;

/**
 * @author Chathura Widanage
 */
public class PolarJava {
    public void filter(BufferedImage in, BufferedImage out) {
        int width = in.getWidth();
        int height = in.getHeight();

        for (int i = 0; i < in.getWidth(); i++) {
            for (int j = 0; j < in.getHeight(); j++) {
                CartesianCoordinate cartesianCoordinate = new CartesianCoordinate(i, j);
                PolarCoordinate polarCoordinate = CoordinateTransformer.cartesian2Polar(width, height, cartesianCoordinate);


                double thetaNew = Math.atan(0.0188*(polarCoordinate.getR()/polarCoordinate.getTheta()));
                double rNew = Math.sqrt(1.439*Math.pow(polarCoordinate.getR(),2)+4052.847*Math.pow(polarCoordinate.getTheta(),2));

                thetaNew = MathUtils.normalizeAngle(thetaNew, FastMath.PI);
                PolarCoordinate newPola = new PolarCoordinate(thetaNew, rNew);

                CartesianCoordinate newCartCord = CoordinateTransformer.polar2Cartesian(width, height, newPola);
                if (clampPass(width, height, newCartCord)) {
                    out.setRGB(i, j, in.getRGB((int) newCartCord.getX(), (int) newCartCord.getY()));
                }
            }
        }
    }

    public void filterCartesian(BufferedImage in, BufferedImage out) {
        int width = in.getWidth();
        int height = in.getHeight();
        double maxR = Math.hypot(width / 2, height / 2);
        for (int i = 0; i < in.getWidth(); i++) {
            for (int j = 0; j < in.getHeight(); j++) {
                CartesianCoordinate cartesianCoordinate = new CartesianCoordinate(i, j);
                PolarCoordinate polarCoordinate = CoordinateTransformer.cartesian2Polar(width, height, cartesianCoordinate);

                CartesianCoordinate newCartCord = new CartesianCoordinate(polarCoordinate.getTheta() * width / (Math.PI * 4), polarCoordinate.getR() * height / maxR);
                CoordinateTransformer.cartesianToCorner(width, height, newCartCord);
                if (clampPass(width, height, newCartCord))
                    out.setRGB(i, j, in.getRGB((int) newCartCord.getX(), (int) newCartCord.getY()));
            }
        }
    }

    private boolean clampPass(int width, int height, CartesianCoordinate cartesianCoordinate) {
        int x = (int) Math.round(cartesianCoordinate.getX());

        int y = (int) Math.round(cartesianCoordinate.getY());

        return x >= 0 && x <= width - 1 && y >= 0 && y <= height - 1;
    }
}
