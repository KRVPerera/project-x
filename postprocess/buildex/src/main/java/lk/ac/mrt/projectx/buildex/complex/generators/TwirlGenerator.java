package lk.ac.mrt.projectx.buildex.complex.generators;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.models.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chathura Widanage
 */
public class TwirlGenerator extends Generator {
    public List<Pair<CartesianCoordinate, CartesianCoordinate>> generate(int width, int height) {
        List<Pair<CartesianCoordinate, CartesianCoordinate>> examples = new ArrayList<>();
        double x0 = 0.5 * (width);
        double y0 = 0.5 * (height);
        for (int sx = 0; sx < width; sx++) {
            for (int sy = 0; sy < height; sy++) {
                double dx = sx - x0;
                double dy = sy - y0;
                double r = Math.sqrt(dx * dx + dy * dy);
                double angle = Math.PI / 256 * r;
                int tx = (int) (+dx * Math.cos(angle) - dy * Math.sin(angle) + x0);
                int ty = (int) (+dx * Math.sin(angle) + dy * Math.cos(angle) + y0);

                if (tx >= 0 && tx < width && ty >= 0 && ty < height) {
                    CartesianCoordinate source = new CartesianCoordinate(sx, sy);
                    CartesianCoordinate dest = new CartesianCoordinate(tx, ty);
                    examples.add(new Pair<>(source, dest));
                }
            }
        }
        return examples;
    }
}
