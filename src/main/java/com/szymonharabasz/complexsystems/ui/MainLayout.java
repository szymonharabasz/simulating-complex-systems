package com.szymonharabasz.complexsystems.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Simulation of Complex Systems");
        logo.addClassNames("text-l", "m-m");

        HorizontalLayout header = new HorizontalLayout(
            new DrawerToggle(),
            logo
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER); 
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header); 
    }

    private void createDrawer() {
        RouterLink oscillatorLink = new RouterLink("Harmonic Oscilator", HarmonicOscillator.class); 
        oscillatorLink.setHighlightCondition(HighlightConditions.sameLocation()); 

        RouterLink gasLink = new RouterLink("Gas in a Box", GasInBox.class); 
        gasLink.setHighlightCondition(HighlightConditions.sameLocation()); 

        addToDrawer(new VerticalLayout( 
            oscillatorLink,
            gasLink
        ));
    }
}
