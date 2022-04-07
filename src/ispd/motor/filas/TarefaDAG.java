/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas;

import ispd.motor.filas.servidores.CentroServico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class defines a task that can be modelled in a DAG
 *
 * @author Luis Baldissera
 */
public class TarefaDAG extends Tarefa {

    private int dependencies;
    /**
     * The lock of the task
     * <p>
     * DAG tasks in the same lock could never happens at same time
     * <p>
     * If it is null, then it is assumed this task has no lock restrictions
     */
    private LockDAG lock;

    public TarefaDAG(int id, String proprietario, String aplicacao, CentroServico origem, double arquivoEnvio, double tamProcessamento, double tempoCriacao) {
        this(id, proprietario, aplicacao, origem, arquivoEnvio, 0, tamProcessamento, tempoCriacao);
    }

    public TarefaDAG(int id, String proprietario, String aplicacao, CentroServico origem, double arquivoEnvio, double arquivoRecebimento, double tamProcessamento, double tempoCriacao) {
        super(id, proprietario, aplicacao, origem, arquivoEnvio, arquivoRecebimento, tamProcessamento, tempoCriacao);
        dependencies = 0;
        nexts = new HashMap<>();
    }

    public static int SUCCESSOR = 0;
    public static int SUFFIX = 1;
    public static int CATCH = 2;
    private Map<TarefaDAG, Integer> nexts;
    public void addNext(TarefaDAG task, int depType) {
        nexts.put(task, depType);
        task.dependencies += 1;
    }

    public void addSuffix(TarefaDAG tarefa) {
        addNext(tarefa, SUFFIX);
    }

    public void addSuccessor(TarefaDAG tarefa) {
        addNext(tarefa, SUCCESSOR);
    }

    public void addCatch(TarefaDAG tarefa) {
        addNext(tarefa, CATCH);
    }

    public void setLock(LockDAG lock) {
        this.lock = lock;
    }

    public LockDAG getLock() {
        return lock;
    }

    public boolean hasLock() {
        return lock != null;
    }

    public void acquireLock() {
        lock.acquire();
    }

    public void releaseLock() {
        lock.release();
    }

    public boolean lockAvailable() {
        return lock.isAvailable();
    }

    public void freeSuffixes() {
        getTasksByDependenceStream(SUFFIX).forEach(task -> task.dependencies -= 1);
    }

    public void freeSuccessors() {
        getTasksByDependenceStream(SUCCESSOR).forEach(task -> task.dependencies -= 1);
    }

    public void freeCatches() {
        getTasksByDependenceStream(CATCH).forEach(task -> task.dependencies -= 1);
    }

    public boolean canExecute() {
        return dependencies == 0;
    }

    public int getDependencies() {
        return dependencies;
    }

    /**
     * Force that the number of dependencies be the given value
     *
     * <p>
     * <b>NOTE: </b> It is especially used to make activation tasks.
     * Use it with caution.
     *
     * @param value the number
     */
    public void forceDependencies(int value) {
        this.dependencies = value;
    }

    public Stream<TarefaDAG> getTasksByDependenceStream(int depType) {
        return nexts.entrySet()
                .stream()
                .filter(e -> e.getValue() == depType)
                .map(Map.Entry::getKey);
    }

    public List<TarefaDAG> getTaskByDependence(int depType) {
        return getTasksByDependenceStream(depType)
                .collect(Collectors.toList());
    }

    public List<TarefaDAG> getSuccessors() {
        return getTaskByDependence(SUCCESSOR);
    }

    public List<TarefaDAG> getSuffixes() {
        return getTaskByDependence(SUFFIX);
    }

    public List<TarefaDAG> getCatches() {
        return getTaskByDependence(CATCH);
    }

    @Override
    public String toString() {
        return "TaskDAG#" + getIdentificador() + "{proprietario=" + getProprietario() + "}";
    }

    /**
     * Make this task, and possibly some others free from its dependency
     * with the task.
     *
     * <p>
     * It is specifically used when you want to ignore some tasks occur,
     * e.g. the DAG switch chose a path and ignore the other ones
     *
     * <p>
     * <b>NOTE: </b> It is assumed the task parameter is a true dependency
     * of this task. IT IS NOT VERIFIED.
     *
     * @param task the dependency task
     */
    protected void makeFreeFrom(TarefaDAG task) {
        if (task.getSuffixes().contains(this) || task.getSuccessors().contains(this)) {
            dependencies -= 1;
            if (this.canExecute()) {
                getSuffixes().forEach(t -> t.makeFreeFrom(this));
                getSuccessors().forEach(t -> t.makeFreeFrom(this));
            }
        }
    }
}