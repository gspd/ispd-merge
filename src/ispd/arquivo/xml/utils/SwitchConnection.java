package ispd.arquivo.xml.utils;

import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.filas.servidores.implementacao.CS_Switch;

public class SwitchConnection {
    public static void toCluster(
            final CS_Switch theSwitch, final CS_Mestre cluster) {
        cluster.addConexoesEntrada(theSwitch);
        cluster.addConexoesSaida(theSwitch);
        SwitchConnection.connectSwitchToServiceCenter(theSwitch, cluster);
    }

    private static void connectSwitchToServiceCenter(
            final CS_Switch theSwitch, final CentroServico serviceCenter) {
        theSwitch.addConexoesEntrada(serviceCenter);
        theSwitch.addConexoesSaida(serviceCenter);
    }

    public static void toMachine(
            final CS_Switch theSwitch, final CS_Maquina machine) {
        machine.addConexoesSaida(theSwitch);
        machine.addConexoesEntrada(theSwitch);
        SwitchConnection.connectSwitchToServiceCenter(theSwitch, machine);
    }

    public static void toCloudMachine(
            final CS_MaquinaCloud maq, final CS_Switch theSwitch) {
        maq.addConexoesSaida(theSwitch);
        maq.addConexoesEntrada(theSwitch);
        theSwitch.addConexoesEntrada(maq);
        theSwitch.addConexoesSaida(maq);
    }
}