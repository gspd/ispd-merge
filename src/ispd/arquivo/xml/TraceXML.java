package ispd.arquivo.xml;

import ispd.motor.filas.Tarefa;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Diogo Tavares
 */
public class TraceXML {

    private final String caminho;
    private String tipo;
    private String saida;
    private int num_tasks;

    public TraceXML(final String caminho) {
        this.caminho = caminho;
        String aux;
        final int i = caminho.lastIndexOf('.');
        this.saida = (caminho.substring(0, i) + ".wmsx");
        this.tipo = caminho.substring(i + 1).toUpperCase();
        System.out.println(this.caminho + "-" + this.saida + "-" + this.tipo);
        
    }

    public String getSaida() {
        return this.saida;
    }
    
    public String getTipo(){
        return this.tipo;
    }
    public int getNum_Tasks(){
        return this.num_tasks;
    }
    
    public void convert() {
        // TODO code application logic here
        try {
            final BufferedReader in = new BufferedReader(new FileReader(this.caminho, StandardCharsets.UTF_8));
            final BufferedWriter out = new BufferedWriter(new FileWriter(this.saida, StandardCharsets.UTF_8));
            
            String str;
            int task1_arrive = 0;
            boolean flag = false;
            //iniciando a escrita do arquivo
            out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n"
                    + "<!DOCTYPE system SYSTEM \"iSPDcarga.dtd\">");
            out.write("\n<system>");
            out.write("\n<trace>");
            out.write("\n<format kind=\"" + this.tipo + "\" />");
            int i = 0;
            while (in.ready()) {
                str = in.readLine();
                if ("".equals(str) || str.charAt(0) == ';' || str.charAt(0) == '#') {/*System.out.println("linha em branco");*/

                } else if ("SWF".equals(this.tipo)) {
                    TraceXML.p_tasksSWF(str, out);
                    i++;
                } else if ("GWF".equals(this.tipo)) {
                    if (!flag) {
                        task1_arrive = this.acha1task(str);
                        flag = true;
                        TraceXML.p_tasksGWF(str, out, task1_arrive);
                        i++;
                    } else {
                        TraceXML.p_tasksGWF(str, out, task1_arrive);
                        i++;
                    }
                }
            }
            out.write("\n</trace>");
            out.write("\n</system>");

            //FECHANDO ARQUIVOS
            this.num_tasks = i;
            in.close();
            out.close();
        } catch (final IOException e) {
        }
    }

    private static void p_tasksGWF(String str, final BufferedWriter out, final int first_task) throws IOException {
        //System.out.println(str);
        str = str.replaceAll("\t", " ");//elimina espaços em branco desnecessários
        str = str.trim();
        //System.out.println(str);

        final String[] campos = str.split(" ");
        if ("-1".equals(campos[3])) {
        } else {
            out.write("\n<task ");
            out.write("id=" + "\"" + campos[0]
                    + "\" arr=\"" + (Integer.parseInt(campos[1]) - first_task)
                    + "\" sts=\"" + campos[10]
                    + "\" cpsz =\"" + campos[3]
                    + "\" cmsz=\"" + campos[20]
                    + "\" usr=\"" + campos[11]);
            out.write("\" />");
        }
    }

    private static void p_tasksSWF(String str, final BufferedWriter out) throws IOException {
        str = str.replaceAll("\\s\\s++", " ");//elimina espaços em branco desnecessários
        str = str.trim();
        out.write("\n<task ");
        final String[] campos = str.split(" ");
        out.write("id=" + "\"" + campos[0]
                + "\" arr=\"" + campos[1]
                + "\" sts=\"" + campos[10]
                + "\" cpsz =\"" + campos[3]
                + "\" cmsz=\"-1"
                + "\" usr=\"" + "user" + campos[11]);
        out.write("\" />");
    }

    private int acha1task(String str) {
        str = str.replaceAll("\t", " ");//elimina tabs desnecessários
        str = str.trim();
        //System.out.println(str);
        final String[] campos = str.split(" ");
        final int a = Integer.parseInt(campos[1]);
        System.out.println(a);
        return a;
    }

    @Override
    public String toString() {
        final int i = this.saida.lastIndexOf('\\');
        this.saida = this.saida.substring(i + 1);
        return ("File " + this.saida + " was generated sucessfully:\n"
                + "\t- Generated from the format: " + this.tipo
                + "\n\t- File has a workload of " + this.num_tasks + " tasks");

    }

    public String LerCargaWMS() {
        try {
            final BufferedReader in = new BufferedReader(new FileReader(this.caminho, StandardCharsets.UTF_8));
            String texto = "";
            final int j = this.caminho.lastIndexOf('\\');
            //pega o nome do arquivo
            final String nome;
            nome = this.caminho.substring(j + 1);
            texto = texto + "File " + nome + " was opened sucessfully:\n";
            String aux;
            int i = 0;
            while (in.ready()) {
                aux = in.readLine();
                if (i == 4) {
                    String[] campos = aux.split(" ");
                    campos = campos[1].split("\"");
                    texto = texto + "\t- File was extracted of trace in the " +
                            "format: " + campos[1] + "\n";
                    this.tipo = campos[1];
                }
                i++;
            }
            //desconta as 7 linhas de tags que não são tarefas..
            i -= 7;
            this.num_tasks =i;
            texto = texto + "\t- File has a workload of " + i + " tasks";
            return (texto);

        } catch (final IOException ex) {
            Logger.getLogger(TraceXML.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ("File has no correct format");
    }

    public void geraTraceSim(final List<Tarefa> tarefas) {
        try {
            this.tipo = "iSPD";
            final FileWriter fp = new FileWriter(this.caminho, StandardCharsets.UTF_8);
            final BufferedWriter out = new BufferedWriter(fp);
            out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n"
                    + "<!DOCTYPE system SYSTEM \"iSPDcarga.dtd\">");
            out.write("\n<system>");
            out.write("\n<trace>");
            out.write("\n<format kind=\"" + this.tipo + "\" />\n");
            int i = 0;
            for (final Tarefa tarefa : tarefas) {
                if (!tarefa.isCopy()) {
                    out.write("<task " + "id=\"" + tarefa.getIdentificador()
                            + "\" arr=\"" + tarefa.getTimeCriacao()
                            + "\" sts=\"" + "1"
                            + "\" cpsz =\"" + tarefa.getTamProcessamento()
                            + "\" cmsz=\"" + tarefa.getArquivoEnvio()
                            + "\" usr=\"" + tarefa.getProprietario());
                    out.write("\" />\n");
                    i++;
                }
            }
            out.write("</trace>");
            out.write("\n</system>");

            this.num_tasks = i;
            this.saida = this.caminho;
            out.close();
            fp.close();
        } catch (final IOException ex) {
            System.out.println("ERROR");
        }
    }
}


