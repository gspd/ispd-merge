package ispd.arquivo.xml.utils;

import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Link;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.Vertice;

public class Connection {
    public static void connectClusterAndSwitch(
            final CS_Mestre cluster, final CS_Switch theSwitch) {
        cluster.addConexoesEntrada(theSwitch);
        cluster.addConexoesSaida(theSwitch);
        Connection.connectSwitchAndServiceCenter(theSwitch, cluster);
    }

    private static void connectSwitchAndServiceCenter(
            final CS_Switch theSwitch, final CentroServico serviceCenter) {
        theSwitch.addConexoesEntrada(serviceCenter);
        theSwitch.addConexoesSaida(serviceCenter);
    }

    public static void connectMachineAndSwitch(
            final CS_Maquina machine, final CS_Switch theSwitch) {
        machine.addConexoesSaida(theSwitch);
        machine.addConexoesEntrada(theSwitch);
        Connection.connectSwitchAndServiceCenter(theSwitch, machine);
    }

    public static void connectLinkAndVertices(
            final CS_Link link,
            final Vertice origination, final Vertice destination) {
        link.setConexoesEntrada((CentroServico) origination);
        link.setConexoesSaida((CentroServico) destination);
        origination.addConexoesSaida(link);
        destination.addConexoesEntrada(link);
    }
}
