package ispd.motor.workload;

import ispd.commons.ISPDType;
import ispd.fxgui.commons.Icon;
import ispd.fxgui.workload.dag.DAG;
import ispd.fxgui.workload.dag.icons.*;
import ispd.motor.filas.*;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.util.distribution.Distribution;
import ispd.util.distribution.DistributionBuilder;
import javafx.scene.Node;

import java.util.*;
import java.util.stream.Collectors;

public class DAGWorkloadGenerator extends SingleSchedulerWorkloadGenerator {

    public static final ISPDType DAG_WORKLOAD_TYPE = ISPDType.type(SingleSchedulerWorkloadGenerator.SINGLE_SCHEDULER_TYPE, "DAG_WORKLOAD");

    ////////////////////////////////////////
    //////////// CONSTRUCTOR ///////////////
    ////////////////////////////////////////

    private LockContainer locks;
    private DAGTaskBuilder taskBuilder;
    private DAGSwitchBuilder switchBuilder;
    public DAGWorkloadGenerator() {
        iconMap = new HashMap<>();

        setType(DAG_WORKLOAD_TYPE);
    }

    ////////////////////////////////////////
    ///////////// OVERRIDES ////////////////
    ////////////////////////////////////////

    @Override
    public List<Tarefa> generateTaskList() {
        List<Tarefa> taskList = new ArrayList<>();
        resetStuff();
        Distribution arrivalDist = DistributionBuilder.exponential(getArrivalTime()).build();
        for (int i = 0; i < getQuantity(); i++) {
            TarefaDAG head = createTaskChain(arrivalDist.random());
            taskList.add(head);
        }
        return taskList;
    }

    private void resetStuff() {
        locks = new LockContainer();
        taskBuilder = new DAGTaskBuilder();
        timerBuilder = new DAGTimerBuilder();
        switchBuilder = new DAGSwitchBuilder();
        iconMap.clear();
        String user = getUser();
        String app = "dag:" + getDag().getName();
        CS_Processamento master = null;
        List<CS_Processamento> masters = getQueueNetwork()
            .getMestres()
            .stream()
            .filter(cs -> cs.getId().equals(getScheduler()))
            .collect(Collectors.toList());
        if (masters.size() > 0) {
            master = masters.get(0);
        }
        // set global stuff tasks
        taskBuilder.setOwner(user);
        taskBuilder.setApplication(app);
        taskBuilder.setSource(master);
        // set global stuff timers
        timerBuilder.setOwner(user);
        timerBuilder.setApplication(app);
        timerBuilder.setSource(master);
        // set global stuff switches
        switchBuilder.setOwner(user);
        switchBuilder.setApplication(app);
        switchBuilder.setSource(master);
    }

    private Map<Icon, TarefaDAG> iconMap;
    /**
     * Retrieves an empty task used as "head" of the dag task chain
     * @return an empty head task
     */
    private TarefaDAG createTaskChain(double creationTime) {
        // create the head empty task
        TarefaDAG head = createHead(creationTime);
        // generate all tasks
        getDag().getIconsByType(TaskIcon.TASK_TYPE).forEach(this::generateTaskFrom);
        // generate all timers
        getDag().getIconsByType(TimerIcon.TIMER_TYPE).forEach(this::generateTimerFrom);
        // generate all synchronization points
        getDag().getIconsByType(SynchronizeIcon.SYNCHRONIZE_TYPE).forEach(this::generateSynchronizeOrActivationFrom);
        // generate all activation points
        getDag().getIconsByType(ActivationIcon.ACTIVATION_TYPE).forEach(this::generateSynchronizeOrActivationFrom);
        // generate all switch tasks
        getDag().getIconsByType(SwitchIcon.SWITCH_TYPE).forEach(this::generateSwitchFrom);
        // chain the tasks according to its dependencies
        getDag().getIconsByType(PrecedenceIcon.PRECEDENCE_TYPE).forEach(icon -> {
            PrecedenceIcon precedence = (PrecedenceIcon) icon;
            TarefaDAG t1 = iconMap.get(precedence.getStartIcon());
            TarefaDAG t2 = iconMap.get(precedence.getEndIcon());
            t1.addSuccessor(t2);
        });
        getDag().getIconsByType(PrefixIcon.PREFIX_TYPE).forEach(icon -> {
            PrefixIcon prefix = (PrefixIcon) icon;
            TarefaDAG t1 = iconMap.get(prefix.getStartIcon());
            TarefaDAG t2 = iconMap.get(prefix.getEndIcon());
            t1.addSuffix(t2);
        });
        getDag().getIconsByType(FailIcon.FAIL_TYPE).forEach(icon -> {
            FailIcon fail = (FailIcon) icon;
            TarefaDAG t1 = iconMap.get(fail.getStartIcon());
            TarefaDAG t2 = iconMap.get(fail.getEndIcon());
            t1.addCatch(t2);
        });
        // .... config more things
        // adds the init tasks to the head task
        iconMap.values().stream().filter(TarefaDAG::canExecute).forEach(head::addSuccessor);
        return head;
    }

    private TarefaDAG createHead(double creationTime) {
        taskBuilder.setSendFile(0.0);
        taskBuilder.setReceiveFile(0.0);
        taskBuilder.setProcessingSize(0.0);
        taskBuilder.setLock(null);
        taskBuilder.setCreationTime(creationTime);
        taskBuilder.setId(getIdSystem().nextId());
        TarefaDAG head = (TarefaDAG) taskBuilder.build();
        getIdSystem().add(head);
        return head;
    }

    private void generateSynchronizeOrActivationFrom(Node node) {
        SynchronizeIcon synchronize = (SynchronizeIcon) node;
        taskBuilder.setId(getIdSystem().nextId());
        taskBuilder.setLock(null);
        taskBuilder.setProcessingSize(0.0);
        taskBuilder.setReceiveFile(0.0);
        taskBuilder.setSendFile(0.0);
        TarefaDAG sync = (TarefaDAG) taskBuilder.build();
        getIdSystem().add(sync);
        iconMap.put(synchronize, sync);
    }

    private void generateTaskFrom(Node icon) {
        TaskIcon taskIcon = (TaskIcon) icon;
        taskBuilder.setId(getIdSystem().nextId());
        taskBuilder.setLock(locks.getLock(taskIcon.getLock()));
        taskBuilder.setProcessingSize(taskIcon.getComputingSize());
        TarefaDAG task = (TarefaDAG) taskBuilder.build();
        getIdSystem().add(task);
        iconMap.put(taskIcon, task);
    }

    private DAGTimerBuilder timerBuilder;
    private void generateTimerFrom(Node icon) {
        TimerIcon timerIcon = (TimerIcon) icon;
        timerBuilder.setId(getIdSystem().nextId());
        timerBuilder.setTime(timerIcon.getTime());
        EsperaDAG timer = (EsperaDAG) timerBuilder.build();
        getIdSystem().add(timer);
        iconMap.put(timerIcon, timer);
    }

    private void generateSwitchFrom(Node node) {
        SwitchIcon sw = (SwitchIcon) node;
        switchBuilder.setId(getIdSystem().nextId());
        switchBuilder.setLock(null);
        switchBuilder.setReceiveFile(0.0);
        switchBuilder.setSendFile(0.0);
        switchBuilder.setProcessingSize(0.0);
        double[] probabilities = new double[sw.getDistributionMap().size()];
        int i = 0;
        for (double value : sw.getDistributionMap().values()) {
            probabilities[i] = value;
            i++;
        }
        switchBuilder.setProbabilities(probabilities);
        SwitchDAG task = (SwitchDAG) switchBuilder.build();
        getIdSystem().add(task);
    }

    @Override
    public SingleSchedulerWorkloadGenerator clone() {
        return null;
    }

    /////////////////////////////////////////
    ///////////// PROPERTIES ////////////////
    /////////////////////////////////////////

    private DAG dag;
    public DAG getDag() {
        return dag;
    }
    public void setDag(DAG dag) {
        this.dag = dag;
    }
}
