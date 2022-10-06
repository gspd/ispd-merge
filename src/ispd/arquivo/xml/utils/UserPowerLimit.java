package ispd.arquivo.xml.utils;

import ispd.escalonador.Escalonador;
import ispd.escalonadorCloud.EscalonadorCloud;
import ispd.motor.metricas.MetricasUsuarios;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserPowerLimit {
    private final List<String> owners;
    private final List<Double> limits;

    public UserPowerLimit(final Map<String, Double> powerLimits) {
        // Note: Constructing from powerLimits.keySet() and .values() is
        // tempting, however those methods do NOT guarantee the same relative
        // order in the returned elements

        this.owners = new ArrayList<>(powerLimits.size());
        this.limits = new ArrayList<>(powerLimits.size());

        for (final var entry : powerLimits.entrySet()) {
            this.owners.add(entry.getKey());
            this.limits.add(entry.getValue());
        }
    }

    public void setSchedulerUserMetrics(final Escalonador scheduler) {
        scheduler.setMetricaUsuarios(this.makeUserMetrics());
    }

    private MetricasUsuarios makeUserMetrics() {
        final var metrics = new MetricasUsuarios();
        metrics.addAllUsuarios(this.owners, this.limits);
        return metrics;
    }

    public void setSchedulerUserMetrics(final EscalonadorCloud scheduler) {
        scheduler.setMetricaUsuarios(this.makeUserMetrics());
    }

    public List<String> getOwners() {
        return this.owners;
    }
}
