package ispd.arquivo.xml;

import ispd.escalonador.Carregar;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

// TODO: Generalize attribute read/write (perhaps with proxy objects)
// TODO: File name and path should be configurable as well

/**
 * Responsible for maintaining the software's configuration file
 */
public class ConfiguracaoISPD {
    // TODO: ENUM; also, why byte?
    public static final byte DEFAULT = 0;
    public static final byte OPTIMISTIC = 1;
    public static final byte GRAPHICAL = 2;
    private static final String FILENAME = "configuration.xml";
    private final File configurationFile = new File(
            Carregar.DIRETORIO_ISPD,
            ConfiguracaoISPD.FILENAME
    );
    private byte simulationMode = ConfiguracaoISPD.DEFAULT;
    private Integer threadCount = 1;
    private Integer simulationCount = 1;
    private Boolean shouldChartProc = true;
    private Boolean shouldChartComms = true;
    private Boolean shouldChartUserTime = true;
    private Boolean shouldChartMachineTime = false;
    private Boolean shouldChartTaskTime = false;
    private File lastModelOpen = Carregar.DIRETORIO_ISPD;

    /**
     * If the configuration file exists, reads configuration from it.
     * Otherwise, 'default' values are used
     */
    public ConfiguracaoISPD() {
        try {
            final var doc = ManipuladorXML.ler(
                    this.configurationFile,
                    "configurationFile.dtd"
            );

            this.readConfigFromDoc(doc);
        } catch (final IOException |
                       ParserConfigurationException |
                       SAXException ignored) {
        }
    }

    private void readConfigFromDoc(final Document doc) {
        final var ispd =
                (Element) doc.getElementsByTagName("ispd").item(0);

        this.readGeneralConfig(ispd);
        this.readChartCreationConfig(ispd);
        this.readLastOpenModelConfig(ispd);
    }

    private void readGeneralConfig(final Element ispd) {
        this.simulationMode = switch (ispd.getAttribute("simulation_mode")) {
            case "default" -> ConfiguracaoISPD.DEFAULT;
            case "optimistic" -> ConfiguracaoISPD.OPTIMISTIC;
            default -> ConfiguracaoISPD.GRAPHICAL;
        };

        this.threadCount = ConfiguracaoISPD.parseAttr(
                ispd, "number_threads", Integer::valueOf);
        this.simulationCount = ConfiguracaoISPD.parseAttr(
                ispd, "number_simulations", Integer::valueOf);
    }

    private void readChartCreationConfig(final Element ispd) {
        final var chart =
                (Element) ispd.getElementsByTagName("chart_create").item(0);

        this.shouldChartProc = ConfiguracaoISPD.parseAttr(
                chart, "processing", Boolean::valueOf);
        this.shouldChartComms = ConfiguracaoISPD.parseAttr(
                chart, "communication", Boolean::valueOf);
        this.shouldChartUserTime = ConfiguracaoISPD.parseAttr(
                chart, "user_time", Boolean::valueOf);
        this.shouldChartMachineTime = ConfiguracaoISPD.parseAttr(
                chart, "machine_time", Boolean::valueOf);
        this.shouldChartTaskTime = ConfiguracaoISPD.parseAttr(
                chart, "task_time", Boolean::valueOf);
    }

    private void readLastOpenModelConfig(final Element ispd) {
        final var files =
                (Element) ispd.getElementsByTagName("model_open").item(0);

        final var lastFile = files.getAttribute("last_file");
        if (!lastFile.isEmpty()) {
            this.lastModelOpen = new File(lastFile);
        }
    }

    private static <T> T parseAttr(final Element elem,
                                   final String attr,
                                   final Function<? super String, T> cast) {
        return cast.apply(elem.getAttribute(attr));
    }

    /**
     * Returns which simulation mode is being used<br>
     * {@literal 0}: <b>Default</b> simulation mode<br>
     * {@literal 1}: <b>Optimisitc</b><br>
     * {@literal 2}: <b>Graphical</b>
     */
    public int getSimulationMode() {
        return this.simulationMode;
    }

    public void setSimulationMode(final byte simulationMode) {
        this.simulationMode = simulationMode;
    }

    /**
     * Saves current configurations to the file
     */
    public void saveCurrentConfig() {
        final var doc = Objects.requireNonNull(ManipuladorXML.novoDocumento());

        final var ispd = this.saveGeneralConfig(doc);
        ispd.appendChild(this.saveChartConfig(doc));
        ispd.appendChild(this.saveLastOpenModelConfig(doc));

        doc.appendChild(ispd);

        ManipuladorXML.escrever(doc,
                this.configurationFile,
                "configurationFile.dtd",
                false
        );
    }

    private Element saveGeneralConfig(final Document doc) {
        final var ispd = doc.createElement("ispd");

        ispd.setAttribute("simulation_mode", switch (this.simulationMode) {
            case ConfiguracaoISPD.DEFAULT -> "default";
            case ConfiguracaoISPD.OPTIMISTIC -> "optimistic";
            case ConfiguracaoISPD.GRAPHICAL -> "graphical";
            default -> throw new RuntimeException("Invalid Simulation Mode");
        });

        ispd.setAttribute(
                "number_simulations", this.simulationCount.toString());
        ispd.setAttribute(
                "number_threads", this.threadCount.toString());

        return ispd;
    }

    private Element saveChartConfig(final Document doc) {
        final var c = doc.createElement("chart_create");
        c.setAttribute("processing", this.shouldChartProc.toString());
        c.setAttribute("communication", this.shouldChartComms.toString());
        c.setAttribute("user_time", this.shouldChartUserTime.toString());
        c.setAttribute("machine_time", this.shouldChartMachineTime.toString());
        c.setAttribute("task_time", this.shouldChartTaskTime.toString());
        return c;
    }

    private Element saveLastOpenModelConfig(final Document doc) {
        final var files = doc.createElement("model_open");

        if (this.lastModelOpen != null) {
            files.setAttribute(
                    "last_file", this.lastModelOpen.getAbsolutePath());
        }

        return files;
    }

    public Integer getNumberOfThreads() {
        return this.threadCount;
    }

    public void setNumberOfThreads(final Integer numberOfThreads) {
        this.threadCount = numberOfThreads;
    }

    public Integer getNumberOfSimulations() {
        return this.simulationCount;
    }

    public void setNumberOfSimulations(final Integer numberOfSimulations) {
        this.simulationCount = numberOfSimulations;
    }

    public Boolean getCreateProcessingChart() {
        return this.shouldChartProc;
    }

    public void setCreateProcessingChart(final Boolean b) {
        this.shouldChartProc = b;
    }

    public Boolean getCreateCommunicationChart() {
        return this.shouldChartComms;
    }

    public void setCreateCommunicationChart(final Boolean b) {
        this.shouldChartComms = b;
    }

    public Boolean getCreateUserThroughTimeChart() {
        return this.shouldChartUserTime;
    }

    public void setCreateUserThroughTimeChart(final Boolean b) {
        this.shouldChartUserTime = b;
    }

    public Boolean getCreateMachineThroughTimeChart() {
        return this.shouldChartMachineTime;
    }

    public void setCreateMachineThroughTimeChart(final Boolean b) {
        this.shouldChartMachineTime = b;
    }

    public Boolean getCreateTaskThroughTimeChart() {
        return this.shouldChartTaskTime;
    }

    public void setCreateTaskThroughTimeChart(final Boolean b) {
        this.shouldChartTaskTime = b;
    }

    public File getLastFile() {
        return this.lastModelOpen;
    }

    public void setLastFile(final File lastDir) {
        if (lastDir == null) {
            return;
        }

        this.lastModelOpen = lastDir;
    }
}
