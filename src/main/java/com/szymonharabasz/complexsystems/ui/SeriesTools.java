package com.szymonharabasz.complexsystems.ui;

import java.util.ArrayList;
import java.util.List;

import com.github.appreciated.apexcharts.helper.Series;
import com.szymonharabasz.complexsystems.common.LabelledData;
import com.szymonharabasz.complexsystems.moleculardynamics.gasinbox.Particle;

public class SeriesTools {
    private SeriesTools() { }

    public static Series<Object[]> makeSeries(Double[] xs, LabelledData labelledData) {
        var data = new ArrayList<Double[]>();
        for (int i = 0; i < Math.min(xs.length, labelledData.data().length); ++i) {
                data.add(new Double[] {xs[i], labelledData.data()[i]});
        }
        Object[][] arr = data.toArray(Object[][]::new);

        return new Series<>(labelledData.label(), arr);
    }

    public static Series<Object[]> makeSeries(String label, List<Particle> particles) {
        var data = new ArrayList<Double[]>();
        for (var particle : particles) {
                data.add(new Double[] {particle.x(), particle.y()});
        }
        Object[][] arr = data.toArray(Object[][]::new);

        return new Series<>(label, arr);
    }
}
