package ispd.arquivo.exportador.gridsim; 

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author denison
 */
public class main2 {
    public static void main(String[] args) throws FileNotFoundException {
        Interpretador parser = null;//classe gerada pelo .jj
        InputStream istream = new FileInputStream("arquivo.txt");//arquivo para teste
        try {
            parser = new Interpretador(istream);
            parser.verbose = true;
            parser.printv("Modo verbose ligado");
            parser.Modelo();//Método principal do parse
            parser.resuladoParser();      
        } catch (ParseException ex) {
            parser.erroEncontrado = true;
            JOptionPane.showMessageDialog(null, "Foram encontrados os seguintes erros:\n" + ex.getMessage(), "Erros Encontrados", JOptionPane.ERROR_MESSAGE);

        }
    }
}
