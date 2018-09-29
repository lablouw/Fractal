package fractal.common;

import java.awt.Color;
import java.util.List;

/**
 * Use methods provided in this class for linear interpolation over multiple
 * argb colors, e.g. to create gradients. <br>
 * For better results try converting to a different colorspace before
 * interpolating (e.g. CIELAB
 * {@link <a href="http://www.cs.rit.edu/~ncs/color/t_convert.html">Color
 * Conversion Algorithms</a>} ).
 */
public final class ColorInterpolator {

    /**
     * Returns an interpolated color between multiple colors.
     * <p>
     * Example steps and returns when using the colors red, green and blue:
     * <ul>
     * <li>0.0f - red</li>
     * <li>0.25f - yellow</li>
     * <li>0.5f - green</li>
     * <li>0.75f - cyan</li>
     * <li>1.0f - blue</li>
     * </ul>
     *
     * @param step the interpolation step in range from 0.0f to 1.0f
     * @param colors multiple (at least one) colors to interpolate over
     *
     * @return color corresponding to the step
     */
    public static Color interpolate(float step, List<Color> colors) {
        // Cutoff to range between 0.0f and 1.0f
//        step = Math.max(Math.min(step, 1.0f), 0.0f);
        while (step < 0) {
            step++;
        }
        step = step % 1;
        
        colors.add(colors.get(0));

        switch (colors.size()) {
            case 0:
                throw new IllegalArgumentException("At least one color required.");

            case 1:
                return colors.get(0);

            case 2:
                return interpolateTwoColors(step, colors.get(0), colors.get(1));

            default:
                // Find local colors to interpolate between:

                // Index of first color, because cast from float to int rounds down
                int firstColorIndex = (int) (step * (colors.size() - 1));

                // Special case: last color (step >= 1.0f)
                if (firstColorIndex == colors.size() - 1) {
                    return colors.get(colors.size() - 1);
                }

                // Calculate localStep between local colors:
                // stepAtFirstColorIndex will be a bit smaller than step
                float stepAtFirstColorIndex = (float) firstColorIndex
                        / (colors.size() - 1);

                // multiply to increase values to range between 0.0f and 1.0f
                float localStep = (step - stepAtFirstColorIndex)
                        * (colors.size() - 1);

                return interpolateTwoColors(localStep, colors.get(firstColorIndex),
                        colors.get(firstColorIndex + 1));
        }

    }

    /**
     * Returns an interpolated color between two colors.
     *
     * @param step interpolation step in range from 0.0f to 1.0f
     * @param color1 the first color
     * @param color2 the second color
     *
     * @return interpolated color which may lie between the two colors
     */
    private static Color interpolateTwoColors(float step, Color color1, Color color2) {
        // Cutoff to range between 0.0f and 1.0f
        step = Math.max(Math.min(step, 1.0f), 0.0f);

        // Calculate difference between alpha, red, green and blue channels
        int deltaAlpha = color2.getAlpha() - color1.getAlpha();
        int deltaRed = color2.getRed() - color1.getRed();
        int deltaGreen = color2.getGreen() - color1.getGreen();
        int deltaBlue = color2.getBlue() - color1.getBlue();

        // Result channel lies between first and second colors channel
        int resultAlpha = (int) (color1.getAlpha() + (deltaAlpha * step));
        int resultRed = (int) (color1.getRed() + (deltaRed * step));
        int resultGreen = (int) (color1.getGreen() + (deltaGreen * step));
        int resultBlue = (int) (color1.getBlue() + (deltaBlue * step));

        // Cutoff to ranges between 0 and 255
        resultAlpha = Math.max(Math.min(resultAlpha, 255), 0);
        resultRed = Math.max(Math.min(resultRed, 255), 0);
        resultGreen = Math.max(Math.min(resultGreen, 255), 0);
        resultBlue = Math.max(Math.min(resultBlue, 255), 0);

        // Combine results
        return new Color(resultRed, resultGreen, resultBlue, resultAlpha);
    }

}
