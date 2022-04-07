package ispd.motor.workload;

import ispd.motor.filas.LockDAG;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.TarefaDAG;

public class DAGTaskBuilder extends TaskBuilder {

    @Override
    public Tarefa build() {
        TarefaDAG task = new TarefaDAG(
                getId(),
                getOwner(),
                getApplication(),
                getSource(),
                getSendFile(),
                getReceiveFile(),
                getProcessingSize(),
                getCreationTime()
        );
        task.setLock(getLock());
        return task;
    }

    /////////////////////////////////////////////////////
    /////////////////// PROPERTIES //////////////////////
    /////////////////////////////////////////////////////

    /**
     * The task lock
     */
    private LockDAG lock;
    public LockDAG getLock() {
        return lock;
    }
    public void setLock(LockDAG lock) {
        this.lock = lock;
    }
}
