package ispd.motor.workload;

import ispd.commons.ISPDType;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.util.distribution.Distribution;
import ispd.util.distribution.DistributionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RandomWorkloadGenerator extends SingleSchedulerWorkloadGenerator {

    public static final ISPDType RANDOM_WORKLOAD_TYPE = ISPDType.type(SingleSchedulerWorkloadGenerator.SINGLE_SCHEDULER_TYPE, "RANDOM_WORKLOAD_TYPE");

    //////////////////////////////////////
    /////////// CONSTRUCTOR //////////////
    //////////////////////////////////////

    public RandomWorkloadGenerator() {
        setType(RANDOM_WORKLOAD_TYPE);
    }

    ///////////////////////////////////////
    /////////// OVERRIDES /////////////////
    ///////////////////////////////////////

    @Override
    public List<Tarefa> generateTaskList() {
        List<Tarefa> taskList = new ArrayList<>();
        Distribution computationDist = DistributionBuilder.uniform(getMinComputation(), getMaxComputation()).build();
        Distribution communicationDist = DistributionBuilder.uniform(getMinCommunication(), getMaxCommunication()).build();
        Distribution arrivalDist = DistributionBuilder.exponential(getArrivalTime()).build();
        TaskBuilder builder = new TaskBuilder();
        builder.setOwner(getUser());
        List<CS_Processamento> masters = getQueueNetwork()
                .getMestres()
                .stream()
                .filter(cs -> cs.getId().equals(getScheduler()))
                .collect(Collectors.toList());
        if (masters.size() > 0) {
            builder.setSource(masters.get(0));
        }
        builder.setApplication("randomApp");
        builder.setReceiveFile(0.0009765625); // Don't know why this value ??
        for (int i = 0; i < getQuantity(); i++) {
            builder.setProcessingSize(computationDist.random());
            builder.setSendFile(communicationDist.random());
            builder.setCreationTime(arrivalDist.random());
            builder.setId(getIdSystem().nextId());
            Tarefa task = builder.build();
            getIdSystem().add(task);
            taskList.add(task);
        }
        return taskList;
    }

    @Override
    protected <G extends WorkloadGenerator> void setUpClone(G generator) {
        super.setUpClone(generator);
        RandomWorkloadGenerator randomGenerator = (RandomWorkloadGenerator) generator;
        randomGenerator.setMaxCommunication(getMaxCommunication());
        randomGenerator.setMaxComputation(getMaxComputation());
        randomGenerator.setMinCommunication(getMinCommunication());
        randomGenerator.setMinComputation(getMinComputation());
    }

    @Override
    public RandomWorkloadGenerator clone() {
        RandomWorkloadGenerator newGenerator = new RandomWorkloadGenerator();
        setUpClone(newGenerator);
        return newGenerator;
    }

    ///////////////////////////////////////
    ////////////// PROPERTIES /////////////
    ///////////////////////////////////////

    private double minComputation;
    public double getMinComputation() {
        return minComputation;
    }
    public void setMinComputation(double minComputation) {
        this.minComputation = minComputation;
    }

    private double maxComputation;
    public double getMaxComputation() {
        return maxComputation;
    }
    public void setMaxComputation(double maxComputation) {
        this.maxComputation = maxComputation;
    }

    private double minCommunication;
    public double getMinCommunication() {
        return minCommunication;
    }
    public void setMinCommunication(double minCommunication) {
        this.minCommunication = minCommunication;
    }

    private double maxCommunication;
    public double getMaxCommunication() {
        return maxCommunication;
    }
    public void setMaxCommunication(double maxCommunication) {
        this.maxCommunication = maxCommunication;
    }
}
