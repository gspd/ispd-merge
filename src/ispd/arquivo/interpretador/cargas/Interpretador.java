package ispd.arquivo.interpretador.cargas;

import ispd.motor.filas.Tarefa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class Interpretador {
    private static final char FILE_SEPARATOR = '\\';
    private static final char FILE_TYPE_SEPARATOR = '.';
    private final String path;
    private String type;
    private String exit;
    private int taskCount = 0;

    public Interpretador(final String path) {
        this.path = path;
        final int i = path.lastIndexOf(Interpretador.FILE_TYPE_SEPARATOR);
        this.exit = (path.substring(0, i) + ".wmsx");
        this.type = path.substring(i + 1).toUpperCase();
        System.out.printf("%s-%s-%s%n", this.path, this.exit, this.type);
    }

    public String getSaida() {
        return this.exit;
    }

    public String getTipo() {
        return this.type;
    }

    @Override
    public String toString() {
        // TODO: Remove
        final int i = this.exit.lastIndexOf(Interpretador.FILE_SEPARATOR);
        this.exit = this.exit.substring(i + 1);
        return """
                File %s was generated sucessfully:
                \t- Generated from the format: %s
                \t- File has a workload of %d tasks"""
                .formatted(this.exit, this.type, this.taskCount);

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
                    Interpretador.writeTask(out, task);
                }
            }

            out.write("""
                    </trace>
                    </system>""");

            this.taskCount = tasks.size();
            this.exit = this.path;

        } catch (final IOException ex) {
            System.out.println("ERROR");
        }
    }

    private static void writeTask(final Writer out, final Tarefa tarefa) throws IOException {
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