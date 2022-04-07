package ispd.motor.workload;

import ispd.motor.filas.SwitchDAG;
import ispd.motor.filas.Tarefa;

import java.util.Arrays;

public class DAGSwitchBuilder extends DAGTaskBuilder {

    @Override
    public Tarefa build() {
        double[] accumulated = calculateAccumulated(getProbabilities());
        return new SwitchDAG(
                getId(),
                getOwner(),
                getApplication(),
                getSource(),
                getSendFile(),
                getReceiveFile(),
                getProcessingSize(),
                getCreationTime(),
                accumulated
        );
    }

    private double[] calculateAccumulated(double[] array) {
        double[] accumulated = Arrays.copyOf(array, array.length);
        for (int i = 1; i < accumulated.length; i++) {
            accumulated[i] = accumulated[i-1] + accumulated[i];
        }
        return accumulated;
    }

    /////////////////////////////////////////
    ///////////// PROPERTIES ////////////////
    /////////////////////////////////////////

    private double[] probabilities;

    public void setProbabilities(double[] probabilities) {
        this.probabilities = probabilities;
    }

    public double[] getProbabilities() {
        return probabilities;
    }
}
