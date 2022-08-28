package ispd.arquivo;

import ispd.escalonador.Carregar;
import ispd.escalonador.ManipularArquivos;

import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/* TODO: Extract superclass (PolicyManager?) from this and Alocadores */

/**
 * Manages storing, retrieving and compiling scheduling policies
 */
public class Escalonadores implements ManipularArquivos {

    private static final String NO_POLICY = "---";

    /**
     * guarda a lista de escalonadores implementados no iSPD, e que já estão
     * disponiveis para o usuario por padrão
     */
    public static final String[] ESCALONADORES = { Escalonadores.NO_POLICY,
            "RoundRobin", "Workqueue", "WQR",
            "DynamicFPLTF", "HOSEP", "OSEP", "EHOSEP" };
    private static final String DIRECTORY_PATH = "ispd/externo";
    /**
     * mantem o caminho do pacote escalonador
     */
    private static final File DIRECTORY =
            new File(Carregar.DIRETORIO_ISPD, Escalonadores.DIRECTORY_PATH);
    /**
     * guarda a lista de escalonadores disponiveis
     */
    private final ArrayList<String> policies = new ArrayList<>(0);
    private final List<String> addedPolicies = new ArrayList<>(0);
    private final List<String> removedPolicies = new ArrayList<>(0);

    /**
     * Atribui o caminho do pacote escalonador e os escalonadores (.class)
     * contidos nele
     */
    public Escalonadores() {
        if (Escalonadores.DIRECTORY.exists()) {
            this.findDotClassSchedulers();
        } else {

            try {
                Escalonadores.createDirectory(Escalonadores.DIRECTORY);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            if (Objects.requireNonNull(this.getClass().getResource(
                    "Escalonadores.class")).toString().startsWith("jar:")) {
                Escalonadores.executeFromJar();
            }
        }
    }

    private void findDotClassSchedulers() {
        final FilenameFilter filter = (b, name) -> name.endsWith(".class");
        final var dotClassFiles =
                Objects.requireNonNull(Escalonadores.DIRECTORY.list(filter));

        Arrays.stream(dotClassFiles)
                .map(Escalonadores::removeDotClassSuffix)
                .forEach(this.policies::add);
    }

    private static void createDirectory(final File dir) throws IOException {
        if (!dir.mkdirs()) {
            throw new IOException("Faile to create directory " + dir);
        }
    }

    private static void executeFromJar() {
        final File jar = new File(
                System.getProperty("java.class.path"));

        try {
            Escalonadores.extractDirFromJar("escalonador", jar);
            Escalonadores.extractDirFromJar("motor", jar);
        } catch (final IOException ex) {
            Logger.getLogger(Escalonadores.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private static String removeDotClassSuffix(final String s) {
        return s.substring(0, s.length() - ".class".length());
    }

    /**
     * extrai arquivos que são necessarios fora do jar
     */
    private static void extractDirFromJar(final String dir, final File file) throws IOException {
        try (final var jar = new JarFile(file)) {
            for (final var entry : new JarEntryIterable(jar)) {
                if (entry.getName().contains(dir)) {
                    Escalonadores.processZipEntry(entry, jar);
                }
            }
        }
    }

    private static void processZipEntry(
            final ZipEntry entry, final ZipFile zip) throws IOException {
        final var file = new File(entry.getName());

        if (entry.isDirectory() && !file.exists()) {
            Escalonadores.createDirectory(file);
            return;
        }

        if (!file.getParentFile().exists()) {
            Escalonadores.createDirectory(file.getParentFile());
        }

        // TODO: Discuss possibility of Files.copy()
        try (final var is = zip.getInputStream(entry);
             final var os = new FileOutputStream(file)) {
            is.transferTo(os);
        }
    }

    /**
     * @return conteudo básico para criar uma classe que implemente um
     * escalonador
     */
    public static String getEscalonadorJava(final String policyName) {
        return """
                package ispd.externo;
                import ispd.escalonador.Escalonador;
                import ispd.motor.filas.Tarefa;
                import ispd.motor.filas.servidores.CS_Processamento;
                import ispd.motor.filas.servidores.CentroServico;
                import java.util.ArrayList;
                import java.util.List;

                public class %s extends Escalonador{

                    @Override
                    public void iniciar() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public Tarefa escalonarTarefa() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public CS_Processamento escalonarRecurso() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public List<CentroServico> escalonarRota(CentroServico destino) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void escalonar() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                }""".formatted(policyName);
    }

    /**
     * Método responsável por listar os escalonadores existentes no simulador
     * ele retorna o nome de cada escalonador contido no pacote com arquivo
     * .class
     */
    @Override
    public ArrayList<String> listar() {
        return this.policies;
    }

    /**
     * @return diretório onde fica os arquivos dos escalonadores
     */
    @Override
    public File getDiretorio() {
        return Escalonadores.DIRECTORY;
    }

    /**
     * Este método sobrescreve o arquivo .java do escalonador informado com o
     * buffer
     */
    @Override
    public boolean escrever(final String nome, final String codigo) {
        try (final var fw = new FileWriter(
                new File(Escalonadores.DIRECTORY, nome + ".java"),
                StandardCharsets.UTF_8
        )) {
            fw.write(codigo);
            return true;
        } catch (final IOException ex) {
            Logger.getLogger(Escalonadores.class.getName())
                    .log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Compila o arquivo .java do escalonador informado caso ocorra algum erro
     * retorna o erro caso contrario retorna null
     *
     * @param nome nome do escalonador
     * @return erros da compilação
     */
    @Override
    public String compilar(final String nome) {
        final var target = new File(Escalonadores.DIRECTORY, nome + ".java");
        final var err = Escalonadores.compile(target);

        // TODO: What?
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException ex) {
            Logger.getLogger(Escalonadores.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        // Check if compilation worked, looking for a .class file
        if (new File(Escalonadores.DIRECTORY, nome + ".class").exists()) {
            this.insertPolicy(nome);
        }

        return err.isEmpty() ? null : err;
    }

    private static String compile(final File target) {
        final var compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler != null) {
            final var err = new ByteArrayOutputStream();
            compiler.run(null, null, err, target.getPath());
            return err.toString();
        } else {
            try {
                return Escalonadores.compileManually(target);
            } catch (final IOException ex) {
                Logger.getLogger(Escalonadores.class.getName())
                        .log(Level.SEVERE, null, ex);
                // TODO: More useful error messages
                return "Não foi possível compilar";
            }
        }
    }

    /**
     * recebe nome do escalonar e adiciona ele na lista de escalonadores
     */
    private void insertPolicy(final String policyName) {
        if (this.policies.contains(policyName)) {
            return;
        }

        this.policies.add(policyName);
        this.addedPolicies.add(policyName);
    }

    private static String compileManually(final File target) throws IOException {
        final var proc = Runtime.getRuntime().exec("javac " + target.getPath());

        // TODO: Extract commonalities with method ler()
        try (final var err = new BufferedReader(new InputStreamReader(
                proc.getErrorStream(), StandardCharsets.UTF_8))
        ) {
            return err.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * Realiza a leitura do arquivo .java do escalonador e retorna um String do
     * conteudo
     */
    @Override
    public String ler(final String escalonador) {
        try (final var br = new BufferedReader(
                new FileReader(
                        new File(Escalonadores.DIRECTORY, escalonador +
                                                          ".java"),
                        StandardCharsets.UTF_8)
        )) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (final IOException ex) {
            Logger.getLogger(Escalonadores.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Método responsável por remover um escalonador no simulador ele recebe o
     * nome do escalonador e remove do pacote a classe .java e .class
     */
    @Override
    public boolean remover(final String escalonador) {
        final var classFile = new File(
                Escalonadores.DIRECTORY, escalonador + ".class");

        final File javaFile = new File(
                Escalonadores.DIRECTORY, escalonador + ".java");

        boolean deleted = false;

        if (classFile.exists()) {
            if (classFile.delete()) {
                this.removePolicy(escalonador);
                deleted = true;
            }
        }

        if (javaFile.exists()) {
            if (javaFile.delete()) {
                deleted = true;
            }
        }

        return deleted;
    }

    /**
     * recebe nome do escalonar e remove ele da lista de escalonadores
     */
    private void removePolicy(final String policyName) {
        if (!this.policies.contains(policyName)) {
            return;
        }

        this.policies.remove(policyName);
        this.removedPolicies.add(policyName);
    }

    /**
     * Método responsável por adicionar um escalonador no simulador ele recebe
     * uma classe Java compila e adiciona ao pacote a classe .java e .class
     *
     * @return true se importar corretamente e false se ocorrer algum erro no
     * processo
     */
    @Override
    public boolean importarEscalonadorJava(final File arquivo) {
        // TODO: Merge this and method compilar() into one

        final var target = new File(Escalonadores.DIRECTORY, arquivo.getName());
        Escalonadores.copyFile(target, arquivo);

        final var err = Escalonadores.compile(target);

        if (!err.isEmpty()) {
            return false;
        }

        final var nome = arquivo.getName()
                .substring(0, arquivo.getName().length() - ".java".length());

        if (!new File(Escalonadores.DIRECTORY, nome + ".class").exists()) {
            return false;
        }

        this.insertPolicy(nome);

        return true;
    }

    private static void copyFile(final File dest, final File src) {
        if (dest.getPath().equals(src.getPath())) {
            return;
        }

        try (final var srcFs = new FileInputStream(src);
             final var destFs = new FileOutputStream(dest)) {
            srcFs.transferTo(destFs);
        } catch (final IOException ex) {
            Logger.getLogger(Escalonadores.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public List listarAdicionados() {
        return this.addedPolicies;
    }

    @Override
    public List listarRemovidos() {
        return this.removedPolicies;
    }

    private record JarEntryIterable(JarFile jar) implements Iterable<JarEntry> {
        @Override
        public Iterator<JarEntry> iterator() {
            return this.jar.entries().asIterator();
        }
    }
}