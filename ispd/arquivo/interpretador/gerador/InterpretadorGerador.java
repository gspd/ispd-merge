/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.arquivo.interpretador.gerador;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author denison_usuario
 */
public class InterpretadorGerador {

    private InputStream istream;
    private Interpretador parser = null;
    
    public InterpretadorGerador(String codigo){
        try {
            istream = new ByteArrayInputStream(codigo.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(InterpretadorGerador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public InterpretadorGerador(InputStream istream) {
        this.istream = istream;
    }

    public boolean executarParse() {
        try {
            parser = new Interpretador(istream);
            parser.verbose = false;
            parser.printv("Modo verbose ligado");
            parser.Escalonador();
            return parser.erroEncontrado;
        } catch (ParseException ex) {
            parser.erroEncontrado = true;
            JOptionPane.showMessageDialog(null, "Foram encontrados os seguintes erros:\n" + ex.getMessage(), "Erros Encontrados", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(InterpretadorGerador.class.getName()).log(Level.SEVERE, null, ex);
            return parser.erroEncontrado;
        }
    }

    public String getNome() {
        return parser.getArquivoNome();
    }
    
    public String getCodigo() {
        return parser.getCodigo();
    }
}
