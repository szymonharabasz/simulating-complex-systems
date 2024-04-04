package com.szymonharabasz.complexsystems.ui;

import java.math.BigDecimal;

import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.grid.builder.RowBuilder;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.config.xaxis.builder.TitleBuilder;

public class LineChart extends ApexChartsBuilder {
    public LineChart(double minY, double maxY, String titleY) {
        withChart(ChartBuilder.get()
                .withType(Type.LINE)
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
                        .withMin(0.0)
                        .withMax(4.0)
                        .withTickAmount(new BigDecimal(8))
                        .withType(XAxisType.NUMERIC)
                        .build())
                .withYaxis(YAxisBuilder.get()
                        .withMin(minY)
                        .withMax(maxY)
                        .withTitle(com.github.appreciated.apexcharts.config.yaxis.builder.TitleBuilder.get()
                                .withText(titleY)
                                .build())
                        .withForceNiceScale(true)
                        .withDecimalsInFloat(2.5)
                        .build());

    }


}