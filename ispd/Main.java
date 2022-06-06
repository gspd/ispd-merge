/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd;

import ispd.gui.JPrincipal;
import ispd.gui.LogExceptions;
import ispd.gui.SplashWindow;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author denison_usuario
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Locale.setDefault(new Locale("en", "US"));
        if (args.length > 0) {
            Terminal tel = new Terminal(args);
            tel.executar();
            System.exit(0);
        } else {
            BufferedImage image = null;
            try {
                image = ImageIO.read(Main.class.getResourceAsStream("gui/imagens/Splash2.png"));
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            SplashWindow window = new SplashWindow(image);
            window.setVisible(true);
            //Exibir e armazenar erros durante execução:
            LogExceptions logExceptions = new LogExceptions(null);
            Thread.setDefaultUncaughtExceptionHandler(logExceptions);
            // cria os novos fluxos de saida para arquivo
            FileOutputStream fosErr = null;
            FileOutputStream fosOut = null;
            try {
                fosErr = new FileOutputStream("Erros/Erros_Simulador");
                fosOut = new FileOutputStream("Erros/Saida_Simulador");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            // define a impresso sobre os fluxos acima
            PrintStream psErr = new PrintStream(fosErr);
            PrintStream psOut = new PrintStream(fosOut);
            // redefine os fluxos na classe System
            //System.setErr(psErr);
            //System.setOut(psOut);
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            JPrincipal gui = new JPrincipal();
            gui.setLocationRelativeTo(null);
            logExceptions.setParentComponent(gui);
            window.dispose();
            gui.setVisible(true);
        }
    }
}