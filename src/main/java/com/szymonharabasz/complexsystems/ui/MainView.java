package com.szymonharabasz.complexsystems.ui;

import com.github.appreciated.apexcharts.config.builder.PlotOptionsBuilder;
import com.github.appreciated.apexcharts.config.builder.XAxisBuilder;
import com.szymonharabasz.complexsystems.moleculardynamics.HarmonicOscillatorProperties;
import com.szymonharabasz.complexsystems.moleculardynamics.HarmonicOscillatorService;
import com.szymonharabasz.complexsystems.moleculardynamics.PhaseSpacePoint;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * The main view contains a button and a click listener.
 */
@Route("")
public class MainView extends VerticalLayout {

    transient HarmonicOscillatorService harmonicOscillatorService;

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

        double m = 0.1;
        double k = 5.0;
        double x0 = 0.1;
        double v0 = 0.0;
        var props = new HarmonicOscillatorProperties(m, k, x0, v0);
        double a = props.amplitude();
        double period = props.period();
        double tMax = 4 * period;
        double dt = 0.02;
        var n = Math.round(tMax / dt);
        Double[] ys = harmonicOscillatorService.analytic(m, k, x0, v0, dt)
            .limit(n)
            .map(PhaseSpacePoint::x)
            .map(x -> x / a)
            .toArray(Double[]::new);
        Double[] xs = harmonicOscillatorService.xs(dt)
            .limit(n)
            .map(t -> t / period)
            .toArray(Double[]::new);

        var chart = new LineChart(xs, ys).build();
        add(chart);
        chart.render();
        // chart.setPlotOptions(PlotOptionsBuilder.get()
        //     .withXaxis(XAxisBuilder.get()));
    }
}
