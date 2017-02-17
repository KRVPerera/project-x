package lk.ac.mrt.projectx.buildex.complex.generators;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Chathura Widanage
 */
public class DistinctRGBGenerator {
    private Set<Integer> usedColors = new HashSet<>();

    public BufferedImage generate(BufferedImage in) {
        BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), in.getType());
        int total = in.getHeight() * in.getWidth();
        System.out.println(total);
        int count = 0;
        for (int i = 0; i < in.getWidth(); i++) {
            for (int j = 0; j < in.getHeight(); j++) {
                Color c = new Color(in.getRGB(i, j));
                if (usedColors.contains(c.getRGB())) {
                    c = getNextColor(c);
                }
                out.setRGB(i, j, c.getRGB());
                usedColors.add(c.getRGB());//replace or put
                System.out.println(count++);
            }
        }
        return out;
    }

    private Color getNextColor(Color color) {
        /*int reds[] = getPossibleVals(color.getRed());
        int green[] = getPossibleVals(color.getGreen());
        int blues[] = getPossibleVals(color.getBlue());
*/
        int initialRed = color.getRed();
        int initialGreen = color.getGreen();
        int initialBlue = color.getBlue();

        int index = 0;

        while (true) {
            Color c1 = new Color(Math.min(255, initialRed + index), Math.min(255, initialGreen + index), Math.min(255, initialBlue + index));
            Color c2 = new Color(Math.min(255, initialRed + index), Math.min(255, initialGreen + index), Math.max(0, initialBlue - index));
            Color c3 = new Color(Math.min(255, initialRed + index), Math.max(0, initialGreen - index), Math.min(255, initialBlue + index));
            Color c4 = new Color(Math.min(255, initialRed + index), Math.max(0, initialGreen - index), Math.max(0, initialBlue - index));
            Color c5 = new Color(Math.max(0, initialRed - index), Math.min(255, initialGreen + index), Math.min(255, initialBlue + index));
            Color c6 = new Color(Math.max(0, initialRed - index), Math.min(255, initialGreen + index), Math.max(0, initialBlue - index));
            Color c7 = new Color(Math.max(0, initialRed - index), Math.max(0, initialGreen - index), Math.min(255, initialBlue + index));
            Color c8 = new Color(Math.max(0, initialRed - index), Math.max(0, initialGreen - index), Math.max(0, initialBlue - index));


            index++;

            Color[] colors = {c1, c2, c3, c4, c5, c6, c7, c8};

       /*     switch (changeC) {
                case 0:
                    c1 = new Color(Math.min(255, initialRed + redIndex), Math.min(255, initialGreen + redIndex), Math.min(255, initialBlue + redIndex));
                    c1 = new Color(Math.min(255, initialRed + redIndex), Math.min(255, initialGreen + redIndex), Math.min(255, initialBlue - redIndex));
                    c1 = new Color(Math.min(255, initialRed + redIndex), Math.min(255, initialGreen - redIndex), Math.min(255, initialBlue + redIndex));
                    c1 = new Color(Math.min(255, initialRed + redIndex), Math.min(255, initialGreen - redIndex), Math.min(255, initialBlue - redIndex));
                    c2 = new Color(Math.max(0, initialRed - redIndex), c.getGreen(), c.getBlue());
                    redIndex++;
                    break;
                case 1:
                    c1 = new Color(Math.min(255, initialRed + redIndex), Math.min(255, initialGreen + redIndex), c.getBlue());
                    c2 = new Color(c.getRed(), Math.max(0, initialGreen - redIndex), c.getBlue());
                    redIndex++;
                    break;
                case 2:
                    c1 = new Color(c.getRed(), c.getGreen(), Math.min(255, initialBlue + redIndex));
                    c2 = new Color(c.getRed(), c.getGreen(), Math.max(0, initialBlue - redIndex));
                    redIndex++;
                    break;
            }*/

            for (Color clr : colors) {
                if (!usedColors.contains(clr.getRGB())) {
                    return clr;
                }
            }

            if (index == 256 && index == 256 && index == 256) {
                System.out.println("Loop");
                System.exit(0);

            }
        }
    }

    public int[] getPossibleVals(int curr) {//generate possible colors (closest first)
        int vals[] = new int[254];
        int index = 0;
        for (int i = 1; i < 255; i += 2) {
            if (curr + 1 < 255) {
                vals[index++] = curr + 1;
            }
            if (curr - 1 >= 0) {
                vals[index++] = curr - 1;
            }
        }
        return vals;
    }
}
