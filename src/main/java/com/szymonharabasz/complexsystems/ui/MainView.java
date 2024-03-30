package com.szymonharabasz.complexsystems.ui;

import java.util.ArrayList;
import java.util.function.DoubleConsumer;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.helper.Series;
import com.szymonharabasz.complexsystems.common.LabelledData;
import com.szymonharabasz.complexsystems.moleculardynamics.HarmonicOscillatorProperties;
import com.szymonharabasz.complexsystems.moleculardynamics.HarmonicOscillatorService;
import com.szymonharabasz.complexsystems.moleculardynamics.PhaseSpacePoint;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
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

    private double dtMilis = 1.0;
    private double m = 0.1;
    private double k = 5.0;
    private double x0 = 0.1;
    private double v0 = 0.0;
    private ApexCharts chart;

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
        
        addNumberField(x -> dtMilis = x, "Time Step", "ms", 0.1, 20.0, 0.1, dtMilis);
        addNumberField(x -> m = x, "Mass", "kg", 0.1, 10.0, 0.1, m);
        addNumberField(x -> k = x, "Rigidity", "N/m", 0.1, 10.0, 0.1, k);
        addNumberField(x -> x0 = x, "Initial position", "m", -10.0, 10.0, 0.1, x0);
        addNumberField(x -> v0 = x, "Initial velocity", "m/s", -10.0, 10.0, 0.1, v0);

        chart = new LineChart().build();
        add(chart);

        updateChartData();
    }

    private void updateChartData() {

        var props = new HarmonicOscillatorProperties(m, k, x0, v0);
        double a = props.amplitude();
        double period = props.period();
        double tMax = 4 * period;
        double dt = dtMilis / 1000;
        var n = Math.round(tMax / dt);
        Double[] analytic = harmonicOscillatorService.analytic(m, k, x0, v0, dt)
            .limit(n)
            .map(PhaseSpacePoint::x)
            .map(x -> x / a)
            .toArray(Double[]::new);
        Double[] xs = harmonicOscillatorService.xs(dt)
            .limit(n)
            .map(t -> t / period)
            .toArray(Double[]::new);
        Double[] euler = harmonicOscillatorService.euler(m, k, x0, v0, dt)
            .limit(n)
            .map(PhaseSpacePoint::x)
            .map(x -> x / a)
            .toArray(Double[]::new);
        Double[] leapfrog = harmonicOscillatorService.leapfrog(m, k, x0, v0, dt)
            .limit(n)
            .map(PhaseSpacePoint::x)
            .map(x -> x / a)
            .toArray(Double[]::new);

        if (chart != null) {
            chart.updateSeries(
                makeSeries(xs, new LabelledData("Analytic", analytic)),
                makeSeries(xs, new LabelledData("Euler", euler)),
                makeSeries(xs, new LabelledData("Leap Frog", leapfrog))
            );
        }

    }

    private void addNumberField(
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
    
        add(numberField);
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
