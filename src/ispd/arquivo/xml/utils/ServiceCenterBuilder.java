package ispd.arquivo.xml.utils;

import ispd.arquivo.xml.WrappedElement;
import ispd.motor.filas.servidores.implementacao.CS_Internet;
import ispd.motor.filas.servidores.implementacao.CS_Link;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.CS_VMM;

public class ServiceCenterBuilder {
    public static CS_Mestre aMaster(final WrappedElement e) {
        return new CS_Mestre(e.id(), e.owner(), e.power(), e.load(),
                e.master().scheduler(), e.energy());
    }

    public static CS_Maquina aMachine(final WrappedElement e) {
        return new CS_Maquina(e.id(), e.owner(), e.power(), 1,
                e.load(), e.energy());
    }

    public static CS_Mestre aMasterWithNoLoad(final WrappedElement e) {
        return new CS_Mestre(e.id(), e.owner(), e.power(), 0.0,
                e.scheduler(), e.energy());
    }

    public static CS_Switch aSwitch(final WrappedElement e) {
        return new CS_Switch(e.id(), e.bandwidth(), 0.0, e.latency());
    }

    public static CS_Maquina aMachineWithId(
            final WrappedElement e, final int id) {
        return new CS_Maquina(e.id(), e.owner(), e.power(),
                1, 0.0, id + 1, e.energy());
    }

    public static CS_Internet anInternet(final WrappedElement e) {
        return new CS_Internet(e.id(), e.bandwidth(), e.load(), e.latency());
    }

    public static CS_Link aLink(final WrappedElement e) {
        return new CS_Link(e.id(), e.bandwidth(), e.load(), e.latency());
    }

    public static CS_MaquinaCloud aCloudMachine(final WrappedElement e) {
        return new CS_MaquinaCloud(
                e.id(),
                e.owner(),
                e.characteristics().processor().power(),
                e.characteristics().processor().number(),
                e.load(),
                e.characteristics().memory().size(),
                e.characteristics().hardDisk().size(),
                e.characteristics().costs().costProcessing(),
                e.characteristics().costs().costMemory(),
                e.characteristics().costs().costDisk()
        );
    }

    public static CS_VMM aVirtualMachineMaster(final WrappedElement e) {
        return new CS_VMM(
                e.id(),
                e.owner(),
                e.characteristics().processor().power(),
                e.characteristics().memory().size(),
                e.characteristics().hardDisk().size(),
                e.load(),
                e.master().scheduler(),
                e.master().vmAlloc()
        );
    }
}
