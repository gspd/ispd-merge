package ispd.gui.results;

import ispd.motor.metricas.Metricas;
import ispd.motor.metricas.MetricasComunicacao;
import ispd.motor.metricas.MetricasProcessamento;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Arrays;

/* package-private */ class ResultsResourcePane extends JScrollPane {

    /**
     * Constructor which creates a pane in which the performed results in total
     * for each machine and each network link are shown in a tabular view.
     *
     * @param metrics the simulation metrics
     */
    public ResultsResourcePane(final Metricas metrics) {
        final var table = new JTable();
        final var columns = new Object[] {
                "Label", "Owner", "Processing performed", "Communication performed"
        };

        this.setPreferredSize(ResultsDialog.CHART_PREFERRED_SIZE);
        this.setViewportView(table);

        table.setModel(new DefaultTableModel(this.makeResourceTable(metrics), columns));
    }

    /* Private Methods */
    /* Utility Resources Pane Methods */


    /**
     * It creates the resources table. The resources table contains results of
     * performed computation for each machine adn performed communication for
     * each network link.
     *
     * @param metrics the simulation metrics
     *
     * @return a table containing information about performed computation for
     *         each machine and performed communication for each network link.
     */
    private Object[][] makeResourceTable(final Metricas metrics) {
        final var table = new ArrayList<Object[]>();

        /* Add table entries for processing metrics */
        if (metrics.getMetricasProcessamento() != null) {
            for (final MetricasProcessamento processingMetrics
                    : metrics.getMetricasProcessamento().values()) {
                final String name;

                if (processingMetrics.getnumeroMaquina() == 0)
                    name = processingMetrics.getId();
                else
                    name = processingMetrics.getId() + " node " + processingMetrics.getnumeroMaquina();

                table.add(Arrays.asList(
                        name,
                        processingMetrics.getProprietario(),
                        processingMetrics.getSegundosDeProcessamento(),
                        0.0d
                ).toArray());
            }
        }

        /* Add table entries for communication metrics */
        if (metrics.getMetricasComunicacao() != null) {
            for (final MetricasComunicacao communicationMetrics
                    : metrics.getMetricasComunicacao().values()) {

                table.add(Arrays.asList(
                        communicationMetrics.getId(),
                        "---",
                        0.0d,
                        communicationMetrics.getSegundosDeTransmissao()
                ).toArray());
            }
        }

        final var tableArray = new Object[table.size()][4];
        for (int i = 0; i < table.size(); i++)
            tableArray[i] = table.get(i);

        return tableArray;
    }
}
