package ispd.fxgui.workload.dag.shapes;

import ispd.fxgui.commons.EdgeShape;
import ispd.fxgui.commons.PointedEdgeShape;
import ispd.fxgui.commons.XedEdgeShape;
import javafx.scene.shape.Line;

public class FailShape extends EdgeShape {

    public FailShape() {
        super(new XedEdgeShape(new PointedEdgeShape(new Line())));
    }
}
