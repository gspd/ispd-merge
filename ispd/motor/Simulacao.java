/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor;

import ispd.motor.filas.Cliente;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.metricas.Metricas;
import java.awt.Color;
import java.util.List;

/**
 *
 * @author denison
 */
public abstract class Simulacao {

    private RedeDeFilas redeDeFilas;
    private List<Tarefa> tarefas;
    private ProgressoSimulacao janela;
    
    public Simulacao(ProgressoSimulacao janela, RedeDeFilas redeDeFilas, List<Tarefa> tarefas){
        this.tarefas = tarefas;
        this.redeDeFilas = redeDeFilas;
        this.janela = janela;
    }

    public ProgressoSimulacao getJanela() {
        return janela;
    }

    public RedeDeFilas getRedeDeFilas() {
        return redeDeFilas;
    }

    public List<Tarefa> getTarefas() {
        return tarefas;
    }

    public abstract void simular();

    public abstract double getTime(Object origem);
    
    public abstract void addEventoFuturo(EventoFuturo ev);
    
    public abstract boolean removeEventoFuturo(int tipoEv, CentroServico servidorEv, Cliente clienteEv);

    public void addTarefa(Tarefa tarefa) {
        tarefas.add(tarefa);
    }

    public void iniciarEscalonadores() {
        for (CS_Processamento mst : redeDeFilas.getMestres()) {
            CS_Mestre mestre = (CS_Mestre) mst;
            //utilisa a classe de escalonamento diretamente 
            //pode ser modificado para gerar um evento 
            //mas deve ser o primeiro evento executado nos mestres
            mestre.getEscalonador().iniciar();
        }
    }

    public Metricas getMetricas() {
        janela.print("Getting Results.");
        janela.print(" -> ");
        Metricas metrica = new Metricas(redeDeFilas, getTime(null), tarefas);
        janela.incProgresso(5);
        janela.println("OK", Color.green);
        return metrica;
    }
}