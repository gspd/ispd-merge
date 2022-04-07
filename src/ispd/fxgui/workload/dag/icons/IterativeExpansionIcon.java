package ispd.fxgui.workload.dag.icons;

import ispd.fxgui.commons.Diagram;
import ispd.commons.ISPDType;
import ispd.fxgui.commons.Icon;
import ispd.fxgui.commons.IconEditor;
import ispd.fxgui.workload.dag.editor.IterativeEditor;
import ispd.fxgui.workload.dag.shapes.IterativeExpansionShape;
import javafx.util.Builder;

public class IterativeExpansionIcon extends ExpansionIcon {

    public static final ISPDType ITEARATIVE_TYPE = ISPDType.type(ExpansionIcon.EXPANSION_TYPE, "ITERATIVE_TYPE");

    public IterativeExpansionIcon(Diagram diagram, boolean selected, double centerX, double centerY) {
        super(IterativeExpansionShape::new, diagram, selected, centerX, centerY);
        setType(ITEARATIVE_TYPE);
    }

    public IterativeExpansionIcon(Diagram diagram, double centerX, double centerY) {
        this(diagram, false, centerX, centerY);
    }

    public IterativeExpansionIcon(Diagram diagram, boolean selected) {
        this(diagram, selected, 0.0, 0.0);
    }

    public IterativeExpansionIcon(Diagram diagram) {
        this(diagram, false, 0.0, 0.0);
    }

    public IterativeExpansionIcon(boolean selected, double centerX, double centerY) {
        this(null, selected, centerX, centerY);
    }

    public IterativeExpansionIcon(double centerX, double centerY) {
        this(null, false, centerX, centerY);
    }

    public IterativeExpansionIcon(boolean selected) {
        this(null, selected, 0.0, 0.0);
    }

    public IterativeExpansionIcon() {
        this(null, false, 0.0, 0.0);
    }

    ////////////////////////////////////////////
    ///////////// OVERRIDES ////////////////////
    ////////////////////////////////////////////

    private static final Builder<IterativeExpansionIcon> ITERATIVE_BUILDER = IterativeExpansionIcon::new;
    @Override
    public Builder<? extends Icon> iconBuilder() {
        return ITERATIVE_BUILDER;
    }

    private static final IterativeEditor ITERATIVE_EDITOR = new IterativeEditor();
    @Override
    protected IconEditor editor() {
        ITERATIVE_EDITOR.setIcon(this);
        return ITERATIVE_EDITOR;
    }
}
