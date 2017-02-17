package lk.ac.mrt.projectx.buildex.complex.filters;

import lk.ac.mrt.projectx.buildex.complex.CoordinateTransformer;
import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.complex.cordinates.PolarCoordinate;

import java.awt.image.BufferedImage;

/**
 * @author Chathura Widanage
 */
public class FishEyeJava {
    public void filter(BufferedImage in, BufferedImage out) {
        int width = in.getWidth();
        int height = in.getHeight();
        for (int i = 0; i < in.getWidth(); i++) {
            for (int j = 0; j < in.getHeight(); j++) {
                CartesianCoordinate cartesianCoordinate = new CartesianCoordinate(i, j);
                PolarCoordinate polarCoordinate = CoordinateTransformer.cartesian2Polar(width, height, cartesianCoordinate);

                double thetaNew = 1.0*polarCoordinate.getTheta()+0.0*(polarCoordinate.getR()/polarCoordinate.getTheta())+-0.027*(polarCoordinate.getTheta()/polarCoordinate.getTheta());

                double rNew = 0.472*polarCoordinate.getR()+0.001*Math.pow(polarCoordinate.getR(),2)+0.002*(polarCoordinate.getR()*polarCoordinate.getTheta());
                //thetaNew= MathUtils.normalizeAngle(thetaNew, FastMath.PI);
                PolarCoordinate newPola = new PolarCoordinate(thetaNew, rNew);

                CartesianCoordinate newCartCord = CoordinateTransformer.polar2Cartesian(width, height, newPola);
                if (clampPass(width, height, newCartCord))
                    out.setRGB(i, j, in.getRGB((int) newCartCord.getX(), (int) newCartCord.getY()));
            }
        }
    }

    public void filterCartesian(BufferedImage in, BufferedImage out) {
        int width = in.getWidth();
        int height = in.getHeight();
        for (int x = 0; x < in.getWidth(); x++) {
            for (int y = 0; y < in.getHeight(); y++) {
                double nx = ((2.0f * x) / (width)) - 1;
                double ny = ((2.0f * y) / (height)) - 1;
                double r = Math.hypot(nx, ny);
                double nr = (r + (1 - (Math.sqrt(1 - r * r)))) / (2.0f);
                double theta = Math.atan2(ny, nx);
                double nxn = nr * Math.cos(theta);
                double nyn = nr * Math.sin(theta);
                CartesianCoordinate outCart = new CartesianCoordinate(
                        ((nxn + 1) * width) / 2,
                        ((nyn + 1) * height) / 2
                );
                if (clampPass(in.getWidth(), in.getHeight(), outCart)) {
                    out.setRGB(x, y, in.getRGB((int) outCart.getX(), (int) outCart.getY()));
                }
            }
        }
    }

    private boolean clampPass(int width, int height, CartesianCoordinate cartesianCoordinate) {
        int x = (int) Math.round(cartesianCoordinate.getX());

        int y = (int) Math.round(cartesianCoordinate.getY());

        return x >= 0 && x <= width - 1 && y >= 0 && y <= height - 1;
    }
}
