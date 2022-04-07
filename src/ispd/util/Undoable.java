package ispd.util;

public interface Undoable {
    void undo();
    void takeSnapshot();
}
