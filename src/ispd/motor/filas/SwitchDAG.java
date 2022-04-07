package ispd.motor.filas;

import ispd.motor.filas.servidores.CentroServico;
import ispd.util.distribution.Distribution;
import ispd.util.distribution.DistributionBuilder;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;

public class SwitchDAG extends TarefaDAG {

    private double[] accumulated;
    private Distribution uniform;
    public SwitchDAG(int id, String proprietario, String aplicacao, CentroServico origem, double arquivoEnvio, double arquivoRecebimento, double tamProcessamento, double tempoCriacao, double[] accumulated) {
        super(id, proprietario, aplicacao, origem, arquivoEnvio, arquivoRecebimento, tamProcessamento, tempoCriacao);
        this.accumulated = Arrays.copyOf(accumulated, accumulated.length);
        this.uniform = DistributionBuilder.uniform(0, accumulated[accumulated.length -1]).build();
    }

    @Override
    public List<TarefaDAG> getSuccessors() {
        return nextChosen();
    }

    @Override
    public List<TarefaDAG> getSuffixes() {
        throw new UnsupportedOperationException("Only successors allowed in SwitchDAG");
    }

    private List<TarefaDAG> nextChosen() {
        List<TarefaDAG> theList = new ArrayList<>();
        theList.add(chooseDAGTask());
        return theList;
    }

    private TarefaDAG chooseDAGTask() {
        TarefaDAG chosen;
        double rand = uniform.random();
        int i = 0;
        // free the non-chosen tasks
        while (i < accumulated.length-1 && rand > accumulated[i]) {
            super.getSuccessors().get(i).makeFreeFrom(this);
            i++;
        }
        // choose one task accordingly to the probability
        chosen = super.getSuccessors().get(i);
        // free the rest of non-chosen tasks
        i++;
        while (i < accumulated.length && rand > accumulated[i]) {
            super.getSuccessors().get(i).makeFreeFrom(this);
            i++;
        }
        return chosen;
    }
}
