package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.utils.WrappedElement;
import ispd.gui.iconico.Vertex;
import ispd.gui.iconico.grade.Link;

public class IconBuilder {
    public static Link aLink(
            final WrappedElement e,
            final Vertex origination, final Vertex destination) {
        final var link = new Link(
                origination, destination,
                e.iconId().local(), e.globalIconId()
        );

        link.setSelected(false);

        link.getId().setName(e.id());
        link.setBandwidth(e.bandwidth());
        link.setLoadFactor(e.load());
        link.setLatency(e.latency());

        return link;
    }
}
