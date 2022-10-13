package ispd.arquivo;

import ispd.escalonadorCloud.ManipularArquivosCloud;

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

/**
 * Manages storing, retrieving and compiling cloud scheduling policies
 */
public class EscalonadoresCloud implements ManipularArquivosCloud {

    private static final String NO_POLICY = "---";
    public static final String[] ESCALONADORES = { EscalonadoresCloud.NO_POLICY,
            "RoundRobin" };
    private static final String DIRECTORY_PATH = "ispd.externo.cloudSchedulers";
    private static final File DIRECTORY =
            new File(EscalonadoresCloud.DIRECTORY_PATH);
    private final ArrayList<String> policies = new ArrayList<>(0);
    private final List<String> addedPolicies = new ArrayList<>(0);
    private final List<String> removedPolicies = new ArrayList<>(0);

    public EscalonadoresCloud() {
        if (EscalonadoresCloud.DIRECTORY.exists()) {
            this.findDotClassAllocators();
        } else {

            try {
                EscalonadoresCloud.createDirectory(EscalonadoresCloud.DIRECTORY);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            if (Objects.requireNonNull(this.getClass().getResource(
                    "EscalonadoresCloud.class")).toString().startsWith("jar:")) {
                EscalonadoresCloud.executeFromJar();
            }
        }
    }

    private void findDotClassAllocators() {
        final FilenameFilter filter = (b, name) -> name.endsWith(".class");
        final var dotClassFiles =
                Objects.requireNonNull(EscalonadoresCloud.DIRECTORY.list(filter));

        Arrays.stream(dotClassFiles)
                .map(EscalonadoresCloud::removeDotClassSuffix)
                .forEach(this.policies::add);
    }

    private static void createDirectory(final File dir) throws IOException {
        if (!dir.mkdirs()) {
            throw new IOException("Failed to create directory " + dir);
        }
    }

    private static void executeFromJar() {
        final var jar = new File(
                System.getProperty("java.class.path"));

        try {
            EscalonadoresCloud.extractDirFromJar("alocacaoVM", jar);
            EscalonadoresCloud.extractDirFromJar("motor", jar);
        } catch (final IOException ex) {
            Logger.getLogger(EscalonadoresCloud.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    private static String removeDotClassSuffix(final String s) {
        return s.substring(0, s.length() - ".class".length());
    }

    private static void extractDirFromJar(final String dir, final File file) throws IOException {
        try (final var jar = new JarFile(file)) {
            for (final var entry : new JarEntryIterable(jar)) {
                if (entry.getName().contains(dir)) {
                    EscalonadoresCloud.processZipEntry(entry, jar);
                }
            }
        }
    }

    private static void processZipEntry(
            final ZipEntry entry, final ZipFile zip) throws IOException {
        final var file = new File(entry.getName());

        if (entry.isDirectory() && !file.exists()) {
            EscalonadoresCloud.createDirectory(file);
            return;
        }

        if (!file.getParentFile().exists()) {
            EscalonadoresCloud.createDirectory(file.getParentFile());
        }

        try (final var is = zip.getInputStream(entry);
             final var os = new FileOutputStream(file)) {
            is.transferTo(os);
        }
    }

    public static String getEscalonadorJava(final String escalonador) {
        return """
                package ispd.externo;
                import ispd.escalonadorCloud.EscalonadorCloud;
                import ispd.motor.filas.Tarefa;
                import ispd.motor.filas.servidores.CS_Processamento;
                import ispd.motor.filas.servidores.CentroServico;
                import java.util.ArrayList;
                import java.util.List;

                public class %s extends EscalonadorCloud{

                    @Override
                    public void iniciar() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public Tarefa escalonarTarefa() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public CS_Processamento escalonarRecurso(String usuario) {
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

                }""".formatted(escalonador);
    }

    @Override
    public ArrayList<String> listar() {
        return this.policies;
    }

    @Override
    public File getDiretorio() {
        return EscalonadoresCloud.DIRECTORY;
    }

    @Override
    public boolean escrever(final String nome, final String codigo) {
        try (final var fw = new FileWriter(
                new File(EscalonadoresCloud.DIRECTORY, nome + ".java"),
                StandardCharsets.UTF_8
        )) {
            fw.write(codigo);
            return true;
        } catch (final IOException ex) {
            Logger.getLogger(EscalonadoresCloud.class.getName())
                    .log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public String compilar(final String nome) {
        final var target = new File(EscalonadoresCloud.DIRECTORY, nome +
                                                                  ".java");
        final var err = EscalonadoresCloud.compile(target);

        try {
            Thread.sleep(1000);
        } catch (final InterruptedException ex) {
            Logger.getLogger(EscalonadoresCloud.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        // Check if compilation worked, looking for a .class file
        if (new File(EscalonadoresCloud.DIRECTORY, nome + ".class").exists()) {
            this.addPolicy(nome);
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
                return EscalonadoresCloud.compileManually(target);
            } catch (final IOException ex) {
                Logger.getLogger(EscalonadoresCloud.class.getName())
                        .log(Level.SEVERE, null, ex);
                return "Não foi possível compilar";
            }
        }
    }

    private void addPolicy(final String policyName) {
        if (this.policies.contains(policyName)) {
            return;
        }

        this.policies.add(policyName);
        this.addedPolicies.add(policyName);
    }

    private static String compileManually(final File target) throws IOException {
        final var proc = Runtime.getRuntime().exec("javac " + target.getPath());

        try (final var err = new BufferedReader(new InputStreamReader(
                proc.getErrorStream(), StandardCharsets.UTF_8))
        ) {
            return err.lines().collect(Collectors.joining("\n"));
        }
    }

    @Override
    public String ler(final String escalonador) {
        try (final var br = new BufferedReader(
                new FileReader(
                        new File(EscalonadoresCloud.DIRECTORY,
                                escalonador + ".java"),
                        StandardCharsets.UTF_8)
        )) {
            return br.lines().collect(Collectors.joining("\n"));
        } catch (final IOException ex) {
            Logger.getLogger(EscalonadoresCloud.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public boolean remover(final String escalonador) {
        final var classFile = new File(
                EscalonadoresCloud.DIRECTORY, escalonador + ".class");

        final File javaFile = new File(
                EscalonadoresCloud.DIRECTORY, escalonador + ".java");

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

    private void removePolicy(final String policyName) {
        if (!this.policies.contains(policyName)) {
            return;
        }

        this.policies.remove(policyName);
        this.removedPolicies.add(policyName);
    }

    @Override
    public boolean importarEscalonadorJava(final File arquivo) {
        final var target = new File(EscalonadoresCloud.DIRECTORY,
                arquivo.getName());
        EscalonadoresCloud.copyFile(target, arquivo);

        final var err = EscalonadoresCloud.compile(target);

        if (!err.isEmpty()) {
            return false;
        }

        final var nome = arquivo.getName()
                .substring(0, arquivo.getName().length() - ".java".length());

        if (!new File(EscalonadoresCloud.DIRECTORY, nome + ".class").exists()) {
            return false;
        }

        this.addPolicy(nome);

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
            Logger.getLogger(EscalonadoresCloud.class.getName())
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