package ispd.motor.workload;

import ispd.motor.filas.EsperaDAG;
import ispd.motor.filas.Tarefa;

public class DAGTimerBuilder extends DAGTaskBuilder {

    @Override
    public Tarefa build() {
        EsperaDAG timer = new EsperaDAG(
                getId(),
                getOwner(),
                getApplication(),
                getSource(),
                getSendFile(),
                getReceiveFile(),
                getProcessingSize(),
                getCreationTime()
        );
        timer.setLock(getLock());
        timer.setTime(getTime());
        return timer;
    }

    //////////////////////////////////////
    ////////// PROPERTIES ////////////////
    //////////////////////////////////////

    private double time;
    public double getTime() {
        return time;
    }
    public void setTime(double time) {
        this.time = time;
    }
}
