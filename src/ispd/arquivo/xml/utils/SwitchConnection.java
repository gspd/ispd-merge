package ispd.arquivo.xml.utils;

import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.CS_VMM;

public class SwitchConnection {
    public static void toMaster(
            final CS_Switch theSwitch, final CS_Mestre master) {
        master.addConexoesEntrada(theSwitch);
        master.addConexoesSaida(theSwitch);
        SwitchConnection.connectSwitchToServiceCenter(theSwitch, master);
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
            final CS_Switch theSwitch, final CS_MaquinaCloud maq) {
        maq.addConexoesSaida(theSwitch);
        maq.addConexoesEntrada(theSwitch);
        theSwitch.addConexoesEntrada(maq);
        theSwitch.addConexoesSaida(maq);
    }

    public static void toVirtualMachineMaster(
            final CS_Switch theSwitch, final CS_VMM vmm) {
        vmm.addConexoesEntrada(theSwitch);
        vmm.addConexoesSaida(theSwitch);
        theSwitch.addConexoesEntrada(vmm);
        theSwitch.addConexoesSaida(vmm);
    }
}