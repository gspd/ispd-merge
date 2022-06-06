/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor.carga;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import java.util.List;
import java.util.Vector;

/**
 * Descreve forma de criar tarefas durante a simulação
 * @author denison_usuario
 */
public abstract class GerarCarga {
    public static final int NULL = -1;
    public static final int RANDOM = 0;
    public static final int FORNODE = 1;
    public static final int TRACE = 2;

    /**
     * Deve ser implementado nas classes que herdam os métodos da GerarCarga
     * @param entrada
     * @return
     */
    public static GerarCarga newGerarCarga(String entrada){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public abstract Vector toVector();

    public abstract List<Tarefa> toTarefaList(RedeDeFilas rdf);

    @Override
    public abstract String toString();

    public abstract int getTipo();
}
