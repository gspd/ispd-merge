package ispd.fxgui.workload.dag.icons;

import ispd.fxgui.commons.EdgeIcon;
import ispd.fxgui.commons.Icon;
import ispd.commons.ISPDType;
import ispd.fxgui.commons.IconEditor;
import ispd.fxgui.commons.NodeIcon;
import ispd.fxgui.workload.dag.editor.PrecedenceEditor;
import ispd.fxgui.workload.dag.shapes.PrecedenceShape;
import javafx.util.Builder;

public class PrecedenceIcon extends EdgeIcon {

    public static final ISPDType PRECEDENCE_TYPE = ISPDType.type(EDGE_TYPE, "PRECEDENCE_TYPE");

    /////////////////////////////////////
    ////////// CONSTRUCTOR //////////////
    /////////////////////////////////////

    public PrecedenceIcon(boolean selected, double startX, double startY, double endX, double endY) {
        super(PrecedenceShape::new, selected, startX, startY, endX, endY);
        setType(PRECEDENCE_TYPE);
    }

    public PrecedenceIcon(double startX, double startY, double endX, double endY) {
        this(false, startX, startY, endX, endY);
    }

    public PrecedenceIcon() {
        this(false, 0.0, 0.0, 0.0, 0.0);
    }

    public PrecedenceIcon(boolean selected, NodeIcon startIcon, NodeIcon endIcon) {
        super(PrecedenceShape::new, selected, startIcon, endIcon);
        setType(PRECEDENCE_TYPE);
    }

    public PrecedenceIcon(NodeIcon startIcon, NodeIcon endIcon) {
        this(false, startIcon, endIcon);
    }

    /////////////////////////////////////////
    ////////////// OVERRIDES ////////////////
    /////////////////////////////////////////

    private static final Builder<PrecedenceIcon> PRECEDENCE_BUILDER = PrecedenceIcon::new;
    @Override
    public Builder<? extends Icon> iconBuilder() {
        return PRECEDENCE_BUILDER;
    }

    private static final PrecedenceEditor PRECEDENCE_EDITOR = new PrecedenceEditor();
    @Override
    protected IconEditor editor() {
        PRECEDENCE_EDITOR.setIcon(this);
        return PRECEDENCE_EDITOR;
    }
}
