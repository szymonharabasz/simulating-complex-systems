package com.szymonharabasz.complexsystems.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.DoubleConsumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.helper.Series;
import com.szymonharabasz.complexsystems.common.LabelledData;
import com.szymonharabasz.complexsystems.moleculardynamics.HarmonicOscillatorProperties;
import com.szymonharabasz.complexsystems.moleculardynamics.HarmonicOscillatorService;
import com.szymonharabasz.complexsystems.moleculardynamics.PhaseSpacePoint;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends VerticalLayout {

    transient HarmonicOscillatorService harmonicOscillatorService;

    private double dtMilis = 15.0;
    private double m = 0.1;
    private double k = 5.0;
    private double x0 = 0.0;
    private double v0 = -10.0;
    private double b = 0.0;
    private ApexCharts trajectoryChart;
    private ApexCharts totalEnergyChart;
    private Span eulerReversabilityResult;
    private Span leapfrogReversabilityResult;

    public MainView(HarmonicOscillatorService harmonicOscillatorService) {
        this.harmonicOscillatorService = harmonicOscillatorService;

        // Use TextField for standard text input
        TextField textField = new TextField("Your name");
        textField.addThemeName("bordered");

        // Button click listeners can be defined as lambda expressions
        Button button = new Button("Say hello", e -> 
            add(new Paragraph("hello"))
        );

        // Theme variants give you predefined extra styles for components.
        // Example: Primary button is more prominent look.
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // You can specify keyboard shortcuts for buttons.
        // Example: Pressing enter in this view clicks the Button.
        button.addClickShortcut(Key.ENTER);

        // Use custom CSS classes to apply styling. This is defined in shared-styles.css.
        addClassName("centered-content");
        
        HorizontalLayout oscilatorSettings = new HorizontalLayout();
        oscilatorSettings.add(makeNumberField(x -> dtMilis = x, "Time Step", "ms", 0.1, 20.0, 0.1, dtMilis));
        oscilatorSettings.add(makeNumberField(x -> m = x, "Mass", "kg", 0.1, 10.0, 0.1, m));
        oscilatorSettings.add(makeNumberField(x -> k = x, "Rigidity", "N/m", 0.1, 10.0, 0.1, k));
        oscilatorSettings.add(makeNumberField(x -> x0 = x, "Initial position", "m", -10.0, 10.0, 0.1, x0));
        oscilatorSettings.add(makeNumberField(x -> v0 = x, "Initial velocity", "m/s", -10.0, 10.0, 0.1, v0));
        var dampingField = makeNumberField(x -> b = x, "Damping", "2sqrt(mk)", 0.0, 2.0, 0.01, b);
        dampingField.addValueChangeListener(event -> {
            if (event.getValue() <= 0.1) {
                event.getSource().setStep(0.01);
            } else {
                event.getSource().setStep(0.1);
            }
        });
        oscilatorSettings.add(dampingField);
        add(oscilatorSettings);

        HorizontalLayout plots = new HorizontalLayout();
        plots.setAlignItems(Alignment.STRETCH);
        trajectoryChart = new LineChart(-2.0, 2.0, "x(t) / T").build();
        trajectoryChart.setHeight(400f,  Unit.PIXELS);
        trajectoryChart.setWidth(400f,  Unit.PIXELS);
        plots.add(new Span(trajectoryChart));
        totalEnergyChart = new LineChart(0, 2.0, "E / E0").build();
        totalEnergyChart.setHeight(400f,  Unit.PIXELS);
        totalEnergyChart.setWidth(400f,  Unit.PIXELS);
        plots.add(new Span(totalEnergyChart));
        add(plots);

        eulerReversabilityResult = new Span();
        add(eulerReversabilityResult);
        leapfrogReversabilityResult = new Span();
        add(leapfrogReversabilityResult);

        updateChartData();
    }

    private void updateChartData() {

        var props = new HarmonicOscillatorProperties(m, k, b, x0, v0);
        double a = props.amplitude();
        double period = props.period();
        double tMax = 4 * period;
        double dt = dtMilis / 1000;
        var n = Math.round(tMax / dt);
        var analyticStream = harmonicOscillatorService.analytic(props, dt);
        var eulerStream = harmonicOscillatorService.euler(props, dt);
        var leapfrogStream = harmonicOscillatorService.leapfrog(props, dt);

        Double totE = harmonicOscillatorService.totalEnergy(props);
        Double[][] analytic = extractTrend(analyticStream, n, x -> x, e -> e / (totE / (a*a)), false);
        Double[][] euler = extractTrend(eulerStream, n, x -> x / a, e -> e / totE, false);
        Double[][] leapfrog = extractTrend(leapfrogStream, n, x -> x / a, e -> e / totE, false);

        var x0reverseEuler = euler[0][euler[0].length - 1];
        var v0reverseEuler = -euler[1][euler[1].length - 1];
        var propsReverseEuler = new HarmonicOscillatorProperties(m, k, b, x0reverseEuler, v0reverseEuler);
        var reverseEulerStream = harmonicOscillatorService.euler(propsReverseEuler, dt);
        Double[][] reverseEuler = extractTrend(reverseEulerStream, n, x -> x / propsReverseEuler.amplitude(), e -> e / totE, true);
        boolean isEulerReversible = harmonicOscillatorService.checkReversability(euler, reverseEuler);

        var x0reverseLeapfrog = euler[0][euler[0].length - 1];
        var v0reverseLeapfrog = -euler[1][euler[1].length - 1];
        var propsReverseLeapfrog = new HarmonicOscillatorProperties(m, k, b, x0reverseLeapfrog, v0reverseLeapfrog);
        var reverseLeapfrogStream = harmonicOscillatorService.leapfrog(propsReverseLeapfrog, dt);
        Double[][] reverseLeapfrog = extractTrend(reverseLeapfrogStream, n, x -> x / propsReverseLeapfrog.amplitude(), e -> e / totE, true);
        boolean isLeapfrogReversible = harmonicOscillatorService.checkReversability(leapfrog, reverseLeapfrog);

        if (eulerReversabilityResult != null) {
            eulerReversabilityResult.setText("Euler method is reversible: " + isEulerReversible);
        }
        if (leapfrogReversabilityResult != null) {
            leapfrogReversabilityResult.setText("Leapfrog method is reversible: " + isLeapfrogReversible);
        }

        // System.out.println("***");
        // for (var i = 0; i < analytic[0].length; ++i) {
        //     System.out.println("" + analytic[0][i] + " " + euler[0][i] + " " + leapfrog[0][i]);
        // }

        Double[] xs = harmonicOscillatorService.xs(dt)
            .limit(n)
            .map(t -> t / period)
            .toArray(Double[]::new);

        if (trajectoryChart != null) {
            trajectoryChart.updateSeries(
                makeSeries(xs, new LabelledData("Analytic", analytic[0])),
                makeSeries(xs, new LabelledData("Euler", euler[0])),
                makeSeries(xs, new LabelledData("Leap Frog", leapfrog[0])),
                makeSeries(xs, new LabelledData("Leap Frog reverse", reverseLeapfrog[0]))
            );
        }

        if (totalEnergyChart != null) {
            totalEnergyChart.updateSeries(
                makeSeries(xs, new LabelledData("Analytic", analytic[1])),
                makeSeries(xs, new LabelledData("Euler", euler[1])),
                makeSeries(xs, new LabelledData("Leap Frog", leapfrog[1]))
            );
        }

    }

    private Double[][] extractTrend(
        Stream<PhaseSpacePoint> stream, long length, UnaryOperator<Double> scaling1, UnaryOperator<Double> scaling2, boolean revert
    ) {
        return stream.limit(length).collect(Collectors.teeing(
            Collectors.mapping(PhaseSpacePoint::x, Collectors.mapping(scaling1, Collectors.toList())),
            Collectors.mapping(PhaseSpacePoint::energy, Collectors.mapping(scaling2, Collectors.toList())),
            (res1, res2) -> {
                if (revert) {
                    Collections.reverse(res1);
                    Collections.reverse(res2);
                }
                return new Double[][]{
                    res1.toArray(Double[]::new),
                    res2.toArray(Double[]::new)
                };
            }
        ));
    }

    private NumberField makeNumberField(
        DoubleConsumer fieldSetter, String label, String unit,
        double min, double max, double step, double initValue)
      {
        NumberField numberField = new NumberField(event -> {
            fieldSetter.accept(event.getValue());
            updateChartData();
        });
        numberField.setLabel(label);
        numberField.setMin(min);
        numberField.setMax(max);
        numberField.setStep(step);
        numberField.setValue(initValue);
        numberField.setStepButtonsVisible(true);
        numberField.setSuffixComponent(new Span(unit));
    
        return numberField;
    }

    private Series<Object[]> makeSeries(Double[] xs, LabelledData labelledData) {
        var data = new ArrayList<Double[]>();
        for (int i = 0; i < Math.min(xs.length, labelledData.data().length); ++i) {
                data.add(new Double[] {xs[i], labelledData.data()[i]});
        }
        Object[][] arr = data.toArray(Object[][]::new);

        return new Series<>(labelledData.label(), arr);
    }
}
