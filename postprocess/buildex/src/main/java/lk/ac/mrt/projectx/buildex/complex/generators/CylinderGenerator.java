package lk.ac.mrt.projectx.buildex.complex.generators;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.models.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chathura Widanage
 */
public class CylinderGenerator extends Generator {
    public List<Pair<CartesianCoordinate, CartesianCoordinate>> generate(int width, int height) {
        List<Pair<CartesianCoordinate, CartesianCoordinate>> examples = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double centerX = width / 2;
                double centerY = height / 2;
                double R = Math.sqrt(Math.pow(centerX, 2) + Math.pow(centerY, 2));

                double dx = x - centerX;
                double dy = y - centerY;

                double distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
                double xout = Math.atan2(dy, dx) * centerX / Math.PI;
                double yout = distance * height / R;

                int tx = (int) (xout + centerX);
                int ty = (int) (yout);

                if (tx >= 0 && tx < width && ty >= 0 && ty < height) {
                    CartesianCoordinate cIn = new CartesianCoordinate(tx, ty);
                    CartesianCoordinate cOut = new CartesianCoordinate(x, y);
                    examples.add(new Pair<>(cIn, cOut));
                }
            }
        }
        return examples;
    }
}
