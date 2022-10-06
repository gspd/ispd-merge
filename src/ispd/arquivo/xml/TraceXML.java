package ispd.arquivo.xml;

import ispd.motor.filas.Tarefa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author Diogo Tavares
 */
public class TraceXML {
    private static final char PATH_SPLIT_DELIMITER = '.';
    private static final Pattern TABS = Pattern.compile("\t");
    private static final Pattern WHITE_SPACE = Pattern.compile("\\s\\s++");
    private final String path;
    private String type;
    private String output;
    private int taskCount = 0;

    public TraceXML(final String path) {
        this.path = path;

        final var split = TraceXML.splitPath(path);

        this.output = split[0] + ".wmsx";
        this.type = split[1].toUpperCase();
    }

    private static String[] splitPath(final String filePath) {
        final int i = filePath.lastIndexOf(TraceXML.PATH_SPLIT_DELIMITER);
        return new String[] {
                filePath.substring(0, i),
                filePath.substring(i + 1) // Skip delimiter
        };
    }

    public String getSaida() {
        return this.output;
    }

    public String getTipo() {
        return this.type;
    }

    public int getNum_Tasks() {
        return this.taskCount;
    }

    public void convert() {
        try (final var in = new BufferedReader(
                new FileReader(this.path, StandardCharsets.UTF_8));
             final var out = new BufferedWriter(
                     new FileWriter(this.output, StandardCharsets.UTF_8))) {

            out.write("""
                    <?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>
                    <!DOCTYPE system SYSTEM " iSPDcarga.dtd">
                    <system>
                    <trace>
                    <format kind="%s" />%s
                    </trace>
                    </system>""".formatted(this.type, this.joinTasksTags(in)));


        } catch (final IOException ignored) {
        }
    }

    private String joinTasksTags(final BufferedReader in) throws IOException {
        final var sb = new StringBuilder(0);

        var firstTaskArrival = Optional.<Integer>empty();

        for (this.taskCount = 0; in.ready(); this.taskCount++) {
            final var line = in.readLine();

            if (TraceXML.shouldSkipLine(line)) {
                continue;
            }

            final String str;

            if ("SWF".equals(this.type)) {
                str = TraceXML.makeSwfTaskTag(line);
            } else if ("GWF".equals(this.type)) {
                if (firstTaskArrival.isEmpty()) {
                    firstTaskArrival =
                            Optional.of(TraceXML.parseArrivalTime(line));
                }
                str = TraceXML.makeGwfTaskTag(line, firstTaskArrival.get());
            } else {
                str = "";
            }

            sb.append(str);
        }

        return sb.toString();
    }

    private static boolean shouldSkipLine(final String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }

        return str.charAt(0) == ';' || str.charAt(0) == '#';
    }

    private static String makeSwfTaskTag(final String str) {
        final var fields =
                TraceXML.WHITE_SPACE.matcher(str).replaceAll(" ")
                        .trim()
                        .split(" ");

        return """

                <task id="%s" arr="%s" sts="%s" cpsz ="%s" cmsz="-1" usr="user%s" />"""
                .formatted(fields[0],
                        fields[1],
                        fields[10], fields[3], fields[11]);
    }

    private static int parseArrivalTime(final String line) {
        final var fields =
                TraceXML.TABS.matcher(line).replaceAll(" ")
                        .trim()
                        .split(" ");

        return Integer.parseInt(fields[1]);
    }

    private static String makeGwfTaskTag(
            final String line, final int firstTaskArrival) {

        final var fields =
                TraceXML.TABS.matcher(line).replaceAll(" ")
                        .trim()
                        .split(" ");

        if ("-1".equals(fields[3])) {
            return "";
        }

        return """

                <task id="%s" arr="%d" sts="%s" cpsz ="%s" cmsz="%s" usr="%s" />"""
                .formatted(fields[0],
                        Integer.parseInt(fields[1]) - firstTaskArrival,
                        fields[10], fields[3], fields[20], fields[11]);
    }

    @Override
    public String toString() {
        final int i = this.output.lastIndexOf('\\');
        this.output = this.output.substring(i + 1);
        return ("File " + this.output + " was generated sucessfully:\n"
                + "\t- Generated from the format: " + this.type
                + "\n\t- File has a workload of " + this.taskCount + " tasks");

    }

    public String LerCargaWMS() {
        try {
            final BufferedReader in =
                    new BufferedReader(new FileReader(this.path,
                            StandardCharsets.UTF_8));
            String texto = "";
            final int j = this.path.lastIndexOf('\\');
            //pega o nome do arquivo
            final String nome;
            nome = this.path.substring(j + 1);
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
                    this.type = campos[1];
                }
                i++;
            }
            //desconta as 7 linhas de tags que não são tarefas..
            i -= 7;
            this.taskCount = i;
            texto = texto + "\t- File has a workload of " + i + " tasks";
            return (texto);

        } catch (final IOException ex) {
            Logger.getLogger(TraceXML.class.getName()).log(Level.SEVERE, null
                    , ex);
        }
        return ("File has no correct format");
    }

    public void geraTraceSim(final List<Tarefa> tarefas) {
        try {
            this.type = "iSPD";
            final FileWriter fp = new FileWriter(this.path,
                    StandardCharsets.UTF_8);
            final BufferedWriter out = new BufferedWriter(fp);
            out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" " +
                      "standalone=\"no\"?>\n"
                      + "<!DOCTYPE system SYSTEM \"iSPDcarga.dtd\">");
            out.write("\n<system>");
            out.write("\n<trace>");
            out.write("\n<format kind=\"" + this.type + "\" />\n");
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

            this.taskCount = i;
            this.output = this.path;
            out.close();
            fp.close();
        } catch (final IOException ignored) {
        }
    }
}


