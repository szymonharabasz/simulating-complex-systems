package com.szymonharabasz.complexsystems.ui;

import java.math.BigDecimal;

import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.animations.Easing;
import com.github.appreciated.apexcharts.config.chart.animations.builder.DynamicAnimationBuilder;
import com.github.appreciated.apexcharts.config.chart.builder.AnimationsBuilder;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;

public class ParticleChart extends ApexChartsBuilder {
    public ParticleChart(double min, double max, double markerSize) {
        withChart(ChartBuilder.get()
                .withType(Type.SCATTER)
                .withAnimations(AnimationsBuilder.get()
                        .withEnabled(false)
                        .withEasing(Easing.LINEAR)
                        .withDynamicAnimation(DynamicAnimationBuilder.get()
                                .withSpeed(1000)
                                .build())
                        .build())
                .withZoom(ZoomBuilder.get()
                        .withEnabled(false)
                        .build())
                .build())
                .withStroke(StrokeBuilder.get()
                        .withCurve(Curve.STRAIGHT)
                        .build())
                // .withGrid(GridBuilder.get()
                //         .withRow(RowBuilder.get()
                //                 .withColors("#f3f3f3", "transparent")
                //                 .withOpacity(0.5).build()
                //         ).build())
                .withXaxis(XAxisBuilder.get()
                        // .withTitle(TitleBuilder.get()
                        //         .withText("t / T")
                        //         .build())
                        .withMin(min)
                        .withMax(max)
                        .withTickAmount(new BigDecimal(0))
                        .withType(XAxisType.NUMERIC)
                        .build())
                .withMarkers(MarkersBuilder.get()
                        .withSize(markerSize, markerSize)
                        .build())
                .withYaxis(YAxisBuilder.get()
                        .withMin(min)
                        .withMax(0.9*max)
                        // .withTitle(com.github.appreciated.apexcharts.config.yaxis.builder.TitleBuilder.get()
                        //         .withText(titleY)
                        //         .build())
                        .withForceNiceScale(true)
                        .withDecimalsInFloat(2.5)
                        .build());

    }
}