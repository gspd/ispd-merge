package ispd.fxgui.workload.dag.shapes;

import ispd.fxgui.commons.EdgeShape;
import ispd.fxgui.commons.PointedEdgeShape;
import javafx.scene.shape.Line;

public class PrecedenceShape extends EdgeShape {

    public PrecedenceShape() {
        super(new PointedEdgeShape(new Line()));
    }
}
