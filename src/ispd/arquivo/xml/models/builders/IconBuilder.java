package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.utils.WrappedElement;
import ispd.gui.iconico.Vertex;
import ispd.gui.iconico.grade.Cluster;
import ispd.gui.iconico.grade.GridItem;
import ispd.gui.iconico.grade.Internet;
import ispd.gui.iconico.grade.Link;
import ispd.gui.iconico.grade.Machine;

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

    static Cluster aCluster(WrappedElement e) {
        final var info = IconInfo.fromElement(e);

        final var cluster = new Cluster(
                info.x(), info.y(),
                info.localId(), info.globalId(),
                e.power() // TODO: Supposed to be .energy()?
        );

        cluster.getId().setName(e.id());
        cluster.setComputationalPower(e.power());
        setProcessingCenterCharacteristics(cluster, e);
        cluster.setSlaveCount(e.nodes());
        cluster.setBandwidth(e.bandwidth());
        cluster.setLatency(e.latency());
        cluster.setSchedulingAlgorithm(e.scheduler());
        cluster.setVmmAllocationPolicy(e.vmAlloc());
        cluster.setOwner(e.owner());
        cluster.setMaster(e.isMaster());
        return cluster;
    }

    public static void setProcessingCenterCharacteristics(
            final GridItem item, final WrappedElement e) {
        if (!e.hasCharacteristicAttribute()) {
            return;
        }

        final var characteristic = e.characteristics();

        if (item instanceof Cluster cluster) {
            cluster.setComputationalPower(characteristic.processor().power());
            cluster.setCoreCount(characteristic.processor().number());
            cluster.setRam(characteristic.memory().size());
            cluster.setHardDisk(characteristic.hardDisk().size());

            if (!characteristic.hasCostAttribute()) {
                return;
            }

            final var co = characteristic.costs();

            cluster.setCostPerProcessing(co.costProcessing());
            cluster.setCostPerMemory(co.costMemory());
            cluster.setCostPerDisk(co.costDisk());

        } else if (item instanceof Machine machine) {
            machine.setComputationalPower(characteristic.processor().power());
            machine.setCoreCount(characteristic.processor().number());
            machine.setRam(characteristic.memory().size());
            machine.setHardDisk(characteristic.hardDisk().size());

            if (!characteristic.hasCostAttribute()) {
                return;
            }

            final var co = characteristic.costs();

            machine.setCostPerProcessing(co.costProcessing());
            machine.setCostPerMemory(co.costMemory());
            machine.setCostPerDisk(co.costDisk());
        }
    }

    static Internet anInternet(WrappedElement e) {
        final var info = IconInfo.fromElement(e);

        final Internet net = new Internet(
                info.x(), info.y(),
                info.localId(), info.globalId()
        );

        net.getId().setName(e.id());

        net.setBandwidth(e.bandwidth());
        net.setLoadFactor(e.load());
        net.setLatency(e.latency());
        return net;
    }

    public record IconInfo(int x, int y, int globalId, int localId) {
        public static IconInfo fromElement(final WrappedElement e) {
            final var position = e.position();
            final var iconId = e.iconId();

            return new IconInfo(
                    position.x(), position.y(),
                    iconId.global(), iconId.local()
            );
        }
    }
}
