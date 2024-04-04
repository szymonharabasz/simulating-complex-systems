package com.szymonharabasz.complexsystems.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.helper.Series;
import com.szymonharabasz.complexsystems.moleculardynamics.gasinbox.GasInBoxService;
import com.szymonharabasz.complexsystems.moleculardynamics.gasinbox.Particle;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "gasinbox", layout = MainLayout.class)
@PageTitle("Simulation of Complex Systems | Gas in a Box")
public class GasInBox extends VerticalLayout {

    private static final double SIGMA0 = 1.0;
    private static final double EPSILON0 = 1.0;
    private static final double M0 = 1.0;

    private double sigma = SIGMA0;
    private double epsilon = EPSILON0;
    private double m = M0;
    private double v0 = Math.sqrt(2*epsilon / M0);
    private double dt = 0.001*sigma/v0;
    private double size = 100 * SIGMA0;
    private List<Double> kineticEnergy;
    private List<Double> potentialEnergy;
    private List<Double> totalEnergy;

    private static final Logger LOGGER = LoggerFactory.getLogger(GasInBox.class);

    private transient GasInBoxService gasInBoxService;

    private ApexCharts particleChart;
    private ApexCharts kineticEnergyChart;
    private ApexCharts potentialEnergyChart;
    private ApexCharts totalEnergyChart;

    private transient List<Particle> currentParticles;
    
    private final transient ScheduledExecutorService scheduler =
    Executors.newScheduledThreadPool(5);

    private Span span = new Span();

    public GasInBox(GasInBoxService gasInBoxService) {
        this.gasInBoxService = gasInBoxService;

        add(span);

        currentParticles = this.gasInBoxService.initialize(100, size, 2*v0, sigma);
        particleChart = new ParticleChart(0, size, 5*sigma).build();
        particleChart.setSeries(makeSeries("Particles", currentParticles));
        add(particleChart);

        HorizontalLayout plots = new HorizontalLayout();
        plots.setAlignItems(Alignment.STRETCH);
        kineticEnergyChart = new LineChart(-2.0, 2.0, "x(t) / T").build();
        kineticEnergyChart.setHeight(400f,  Unit.PIXELS);
        kineticEnergyChart.setWidth(400f,  Unit.PIXELS);
        plots.add(new Span(kineticEnergyChart));
        potentialEnergyChart = new LineChart(-2.0, 2.0, "x(t) / T").build();
        potentialEnergyChart.setHeight(400f,  Unit.PIXELS);
        potentialEnergyChart.setWidth(400f,  Unit.PIXELS);
        plots.add(new Span(potentialEnergyChart));
        totalEnergyChart = new LineChart(0, 2.0, "E / E0").build();
        totalEnergyChart.setHeight(400f,  Unit.PIXELS);
        totalEnergyChart.setWidth(400f,  Unit.PIXELS);
        plots.add(new Span(totalEnergyChart));
        add(plots);
    }

    private Series<Object[]> makeSeries(String label, List<Particle> particles) {
        var data = new ArrayList<Double[]>();
        for (var particle : particles) {
                data.add(new Double[] {particle.x(), particle.y()});
        }
        Object[][] arr = data.toArray(Object[][]::new);

        return new Series<>(label, arr);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        LOGGER.info("Component attached");
        super.onAttach(attachEvent);

        kineticEnergy = new ArrayList<>();
        potentialEnergy = new ArrayList<>();
        totalEnergy = new ArrayList<>();

        scheduler.scheduleAtFixedRate(new Runnable() {
            private int i = 0;

            @Override
            public void run() {
                currentParticles = gasInBoxService.propagate(currentParticles, m, epsilon, sigma, dt, size).stream().filter(p ->
                    Double.isFinite(p.x()) && Double.isFinite(p.y()) && Double.isFinite(p.vx()) && Double.isFinite(p.vy())
                ).toList();
                kineticEnergy.add(gasInBoxService.totalKineticEnergy(m, currentParticles));
                potentialEnergy.add(gasInBoxService.totalPotentialEnergy(epsilon, sigma, currentParticles));
                totalEnergy.add(kineticEnergy.get(kineticEnergy.size()-1) + potentialEnergy.get(potentialEnergy.size()-1));
                var newSeries = makeSeries("Particles", currentParticles);

                if (i % 10 == 0) {
                    getUI().ifPresent(ui -> ui.access(() -> {
                        span.setText("Particle #1:  " + currentParticles.get(1).x() + " " + currentParticles.get(1).y());
                        particleChart.updateSeries(newSeries);
                        ui.push();
                    }));
                }
                ++i;
            }
        }, 200, 10, TimeUnit.MILLISECONDS);
    }

}
