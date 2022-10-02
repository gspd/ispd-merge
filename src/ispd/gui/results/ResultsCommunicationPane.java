package ispd.gui.results;

import ispd.gui.auxiliar.SimulationResultChartMaker;
import ispd.gui.utils.Multipane;
import ispd.gui.utils.MultipaneButton;

import java.util.List;

/* package-private */ class ResultsCommunicationPane extends Multipane {

    /**
     * Constructor which creates a pane that contains results of communication
     * performed for each network link being shown in a bar and pie chart.
     *
     * @param charts the simulation chart maker
     */
    public ResultsCommunicationPane(final SimulationResultChartMaker charts) {
        super(List.of(
                new MultipaneButton("Bar Chart", charts.getCommunicationBarChart()),
                new MultipaneButton("Pie Chart", charts.getCommunicationPieChart())
        ));
    }
}
