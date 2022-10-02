package ispd.gui.results;

import ispd.gui.auxiliar.SimulationResultChartMaker;
import ispd.gui.utils.Multipane;
import ispd.gui.utils.MultipaneButton;

import java.util.List;

/* package-private */ class ResultsComputingPowerPane extends Multipane {

    /**
     * Constructor which creates a pane that contains results of the computing
     * power usage through time for each machine, user and task in a bar chart.
     *
     * @param charts the simulation chart maker
     */
    public ResultsComputingPowerPane(final SimulationResultChartMaker charts) {
        super(List.of(
                new MultipaneButton("Per machine", charts.getComputingPowerPerMachineChart()),
                new MultipaneButton("Per user", charts.getComputingPowerPerUserChart()),
                new MultipaneButton("Per task", charts.getComputingPowerPerTaskChart())
        ));
    }
}
