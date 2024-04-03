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
import com.vaadin.flow.component.html.Span;
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
    private double dt = 0.01*sigma/v0;
    private double size = 100 * SIGMA0;


    private static final Logger LOGGER = LoggerFactory.getLogger(GasInBox.class);

    private transient GasInBoxService gasInBoxService;

    private ApexCharts particleChart;

    private transient List<Particle> currentParticles;
    
    private final transient ScheduledExecutorService scheduler =
    Executors.newScheduledThreadPool(5);

    private Span span = new Span();

    public GasInBox(GasInBoxService gasInBoxService) {
        this.gasInBoxService = gasInBoxService;

        add(span);

        currentParticles = this.gasInBoxService.initialize(100, 100*sigma, 2*v0, sigma);
        particleChart = new ParticleChart(0, size).build();
        particleChart.setSeries(makeSeries("Particles", currentParticles));
        add(particleChart);


    }

    private void updateChart(List<Particle> particles) {

        particleChart.updateSeries(makeSeries("Particles", particles));
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
        super.onAttach(attachEvent);

        scheduler.scheduleAtFixedRate(() -> {
            var newParticles = gasInBoxService.propagate(currentParticles, m, epsilon, sigma, dt, size);
            updateChart(newParticles);
            currentParticles = newParticles;
            var newSeries = makeSeries("Particles", newParticles);

            getUI().ifPresent(ui -> ui.access(() -> {
                span.setText("Particle #1:  " + currentParticles.get(1).x() + " " + currentParticles.get(1).y());
                particleChart.setSeries(newSeries);
                ui.push();
            }));
            
        }, 2, 1, TimeUnit.SECONDS);
    }

}
