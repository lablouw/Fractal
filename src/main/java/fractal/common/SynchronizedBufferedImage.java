/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fractal.common;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 *
 * @author lloyd
 */
public class SynchronizedBufferedImage 
{
    private BufferedImage image;
    
    public SynchronizedBufferedImage(int width, int height)
    {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public SynchronizedBufferedImage(BufferedImage image)
    {
        this.image = image;
    }

    public synchronized BufferedImage getBufferedImage() {
        return image.getSubimage(0, 0, image.getWidth(), image.getHeight());
    }
    
    public synchronized void setColor(int x, int y, Color color)
    {
        image.setRGB(x, y, color.getRGB());
    }

}
