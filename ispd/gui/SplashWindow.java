/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.gui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;
import javax.swing.JWindow;

/**
 *
 * Janela de carregamento do iSPD, chamada durante a inicialização do programa
 *
 * @author denison_usuario
 */

public class SplashWindow extends JWindow {

    private BufferedImage splash = null;

    public SplashWindow(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int extra = 14;

        setSize(new Dimension(
                width + extra, height + extra));
        setLocationRelativeTo(null);
        Rectangle windowRect = getBounds();

        splash = new BufferedImage(
                width + extra, height + extra,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) splash.getGraphics();

        try {
            Robot robot = new Robot(
                    getGraphicsConfiguration().getDevice());
            BufferedImage capture = robot.createScreenCapture(
                    new Rectangle(windowRect.x, windowRect.y,
                    windowRect.width + extra,
                    windowRect.height + extra));
            g2.drawImage(capture, null, 0, 0);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }

        BufferedImage shadow = new BufferedImage(
                width + extra, height + extra,
                BufferedImage.TYPE_INT_ARGB);
        Graphics shadowGraphics = shadow.getGraphics();
        shadowGraphics.setColor(
                new Color(0.0f, 0.0f, 0.0f, 0.3f));
        shadowGraphics.fillRoundRect(
                6, 6, width, height, 12, 12);
        shadowGraphics.dispose();

        float[] data = new float[49];
        Arrays.fill(data, 1 / (float) (49));
        g2.drawImage(shadow,
                new ConvolveOp(new Kernel(7, 7, data)),
                0, 0);
        g2.drawImage(image, 0, 0, this);
        g2.dispose();
    }

    @Override
    public void paint(Graphics g) {
        if (splash != null) {
            g.drawImage(splash, 0, 0, null);
        }
    }
}