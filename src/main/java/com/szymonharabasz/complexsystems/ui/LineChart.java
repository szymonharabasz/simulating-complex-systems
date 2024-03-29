package com.szymonharabasz.complexsystems.ui;

import java.math.BigDecimal;
import java.util.ArrayList;

import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.grid.builder.RowBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.xaxis.TickPlacement;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.config.xaxis.builder.TitleBuilder;
import com.github.appreciated.apexcharts.helper.Series;

public class LineChart extends ApexChartsBuilder {
    public LineChart(Double[] xs, Double[] ys) {
        withChart(ChartBuilder.get()
                .withType(Type.SCATTER)
                .withZoom(ZoomBuilder.get()
                        .withEnabled(false)
                        .build())
                .build())
                .withStroke(StrokeBuilder.get()
                        .withCurve(Curve.STRAIGHT)
                        .build())
                .withGrid(GridBuilder.get()
                        .withRow(RowBuilder.get()
                                .withColors("#f3f3f3", "transparent")
                                .withOpacity(0.5).build()
                        ).build())
                .withXaxis(XAxisBuilder.get()
                        .withTitle(TitleBuilder.get()
                                .withText("t / T")
                                .build())
                        .withTickPlacement(TickPlacement.ON)
                        .withMin(0.0)
                        .withMax(4.0)
                        .withTickAmount(new BigDecimal(4))
                        .build())
                .withYaxis(YAxisBuilder.get()
                        .withMin(-2)
                        .withMax(2)
                        .withTitle(com.github.appreciated.apexcharts.config.yaxis.builder.TitleBuilder.get()
                                .withText("x(t) / A")
                                .build())
                        .withForceNiceScale(true)
                        .withDecimalsInFloat(2.5)
                        .build())
                .withSeries(makeSeries("analytic", xs, ys));

                
    }

    private Series<Object[]> makeSeries(String name, Double[] xs, Double[] ys) {
        var data = new ArrayList<Double[]>();
        for (int i = 0; i < Math.min(xs.length, ys.length); ++i) {
                data.add(new Double[] {xs[i], ys[i]});
        }
        Object[][] arr = data.toArray(Object[][]::new);
        return new Series<>(name, arr);
    }
}