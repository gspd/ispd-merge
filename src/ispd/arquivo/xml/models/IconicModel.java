package ispd.arquivo.xml.models;

import ispd.gui.iconico.Edge;
import ispd.gui.iconico.Vertex;

import java.util.Collection;

public record IconicModel(
        Collection<Vertex> vertices,
        Collection<Edge> edges) {
}