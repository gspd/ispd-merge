package ispd.motor.filas;

import java.util.HashMap;
import java.util.Map;

public class LockContainer {

    private Map<String, LockDAG> lockMap;

    public LockContainer() {
        lockMap = new HashMap<>();
    }

    public LockDAG getLock(String lockId) {
        if (!lockMap.containsKey(lockId)) {
            lockMap.put(lockId, new LockDAG());
        }
        return lockMap.get(lockId);
    }
}
