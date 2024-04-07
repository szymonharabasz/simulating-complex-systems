package com.szymonharabasz.complexsystems.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.config.xaxis.builder.TitleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.appreciated.apexcharts.ApexCharts;
import com.szymonharabasz.complexsystems.common.LabelledData;
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
    private static final int NSTEPS = 1000;
    private static final int NPARTICLES = 100;
    private static final String TITLE_X = "t * sigma / v0";

    private double sigma = SIGMA0;
    private double epsilon = EPSILON0;
    private double m = M0;
    private double v0 = Math.sqrt(2*epsilon / M0);
    private double dt = 0.001*sigma/v0;
    private double size = 100 * SIGMA0;
    private List<Double> xs;
    private List<Double> totalEnergy;
    private Double kineticEnergyInit;
    private Double potentialEnergyInit;
    private Double totalEnergyInit;
    

    private static final Logger LOGGER = LoggerFactory.getLogger(GasInBox.class);

    private transient GasInBoxService gasInBoxService;

    private ApexCharts particleChart;
    private ApexCharts energyChart;

    private transient List<Particle> currentParticles;
    
    private final transient ScheduledExecutorService scheduler =
    Executors.newScheduledThreadPool(5);

    private Span span = new Span();

    public GasInBox(GasInBoxService gasInBoxService) {
        this.gasInBoxService = gasInBoxService;

        add(span);

        currentParticles = this.gasInBoxService.initialize(NPARTICLES, size, 2*v0, sigma);
        particleChart = new ParticleChart(0, size, 5*sigma).build();
        particleChart.setSeries(SeriesTools.makeSeries("Particles", currentParticles));
        add(particleChart);

        HorizontalLayout plots = new HorizontalLayout();
        plots.setAlignItems(Alignment.STRETCH);
        energyChart = new LineChart(0, dt * NSTEPS * v0 / sigma, -10, 10, TITLE_X, "E / E0").build();
        energyChart.setHeight(400f,  Unit.PIXELS);
        energyChart.setWidth(400f,  Unit.PIXELS);
        plots.add(new Span(energyChart));
        add(plots);
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        LOGGER.info("Component attached");
        super.onAttach(attachEvent);
        List<List<Particle>> history = new ArrayList<>(gasInBoxService.leapfrog(currentParticles, size, m, epsilon, sigma, dt).limit(NSTEPS).toList());
        kineticEnergyInit = this.gasInBoxService.totalKineticEnergy(m, history.get(0));
        potentialEnergyInit = this.gasInBoxService.totalPotentialEnergy(epsilon, sigma, history.get(0));
        totalEnergyInit = kineticEnergyInit + potentialEnergyInit;

        var xs = new ArrayList<Double>(IntStream.range(0, NSTEPS).boxed().map(i -> i * dt * v0 / sigma).toList());
        var kineticEnergy = new ArrayList<Double>(history.stream().map(particles -> gasInBoxService.totalKineticEnergy(m, particles) / kineticEnergyInit).limit(NSTEPS).toList());
        var potentialEnergy = new ArrayList<Double>(history.stream().map(particles -> gasInBoxService.totalPotentialEnergy(epsilon, sigma, particles) / potentialEnergyInit).limit(NSTEPS).toList());
        var kineticEnergySeries = SeriesTools.makeSeries(xs.toArray(Double[]::new), new LabelledData("Kinetic energy", kineticEnergy.toArray(Double[]::new)));
        var potentialEnergySeries = SeriesTools.makeSeries(xs.toArray(Double[]::new), new LabelledData("Potential energy", potentialEnergy.toArray(Double[]::new)));

        energyChart.updateSeries(kineticEnergySeries, potentialEnergySeries);

        scheduler.scheduleAtFixedRate(new Runnable() {
            private int i = 0;
            @Override
            public void run() {
                if (i < NSTEPS) {
                    currentParticles = history.get(i);
                } else {
                    currentParticles = gasInBoxService.propagate(currentParticles, m, epsilon, sigma, dt, size);
                }
                var particleSeries = SeriesTools.makeSeries("Particles", currentParticles);

                var currentKineticEnergy = gasInBoxService.totalKineticEnergy(m, currentParticles) / kineticEnergyInit;
                var currentPotentialEnergy = gasInBoxService.totalPotentialEnergy(epsilon, sigma, currentParticles) / potentialEnergyInit;
                var currentTotalEnergy = currentKineticEnergy + currentPotentialEnergy;

                if (i > NSTEPS) {
                    kineticEnergy.remove(0);
                    kineticEnergy.add(currentKineticEnergy);
                    potentialEnergy.remove(0);
                    potentialEnergy.add(currentPotentialEnergy);
                }
                var newKineticEnergySeries = SeriesTools.makeSeries(xs.toArray(Double[]::new), new LabelledData("Kinetic energy", kineticEnergy.toArray(Double[]::new)));
                var newPpotentialEnergySeries = SeriesTools.makeSeries(xs.toArray(Double[]::new), new LabelledData("Potential energy", potentialEnergy.toArray(Double[]::new)));

                if (i % 10 == 0) {
                    getUI().ifPresent(ui -> ui.access(() -> {
                        span.setText("i: " + i + ", potential energy: " + currentPotentialEnergy + ", initial" + potentialEnergyInit + ", kinetic; " + currentKineticEnergy);
                        particleChart.updateSeries(particleSeries);
                        energyChart.updateSeries(newKineticEnergySeries, newPpotentialEnergySeries);
                        ui.push();
                    }));
                }
                ++i;
            }
        }, 200, 10, TimeUnit.MILLISECONDS);
    }

}
