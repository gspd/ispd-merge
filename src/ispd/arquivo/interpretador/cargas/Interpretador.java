package ispd.arquivo.interpretador.cargas;

import ispd.motor.filas.Tarefa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class Interpretador {
    private static final char FILE_TYPE_SEPARATOR = '.';
    private final String path;
    private String type;
    private String exit;

    public Interpretador(final String path) {
        this.path = path;
        final int i = path.lastIndexOf(Interpretador.FILE_TYPE_SEPARATOR);
        this.exit = (path.substring(0, i) + ".wmsx");
        this.type = path.substring(i + 1).toUpperCase();
        // TODO: Remove
        System.out.printf("%s-%s-%s%n", this.path, this.exit, this.type);
    }

    public void geraTraceSim(final Collection<? extends Tarefa> tasks) {

        this.type = "iSPD";

        try (final var out = new BufferedWriter(
                new FileWriter(this.path, StandardCharsets.UTF_8))) {

            out.write("""
                    <?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>
                    <!DOCTYPE system SYSTEM "iSPDcarga.dtd">
                    <system>
                    <trace>
                    <format kind="%s" />
                    """.formatted(this.type));

            for (final var task : tasks) {
                if (!task.isCopy()) {
                    Interpretador.writeTaskAsTag(out, task);
                }
            }

            out.write("""
                    </trace>
                    </system>""");

            this.exit = this.path;

        } catch (final IOException ex) {
            // TODO: Move exception handling up, remove debug print
            System.out.println("ERROR");
        }
    }

    private static void writeTaskAsTag(final Writer out, final Tarefa tarefa) throws IOException {
        // TODO: Move to task?
        out.write("""
                <task id="%d" arr="%s" sts="1" cpsz ="%s" cmsz="%s" usr="%s" />
                """.formatted(
                tarefa.getIdentificador(),
                tarefa.getTimeCriacao(),
                tarefa.getTamProcessamento(),
                tarefa.getArquivoEnvio(),
                tarefa.getProprietario()
        ));
    }
}