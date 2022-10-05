package ispd.arquivo.xml.utils;

import ispd.escalonador.Escalonador;
import ispd.motor.metricas.MetricasUsuarios;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserPowerLimit {
    private final List<String> owners;
    private final List<Double> limits;

    public UserPowerLimit(final Map<String, Double> powerLimits) {
        this.owners = new ArrayList<>(powerLimits.keySet());
        this.limits = new ArrayList<>(powerLimits.values());
    }

    public void setSchedulerUserMetrics(final Escalonador scheduler) {
        scheduler.setMetricaUsuarios(this.makeUserMetrics());
    }

    private MetricasUsuarios makeUserMetrics() {
        final var metrics = new MetricasUsuarios();
        metrics.addAllUsuarios(this.owners, this.limits);
        return metrics;
    }

    public List<String> getOwners() {
        return this.owners;
    }
}
