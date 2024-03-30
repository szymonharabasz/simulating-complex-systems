package com.szymonharabasz.complexsystems.moleculardynamics;

import java.util.stream.Stream;

import jakarta.enterprise.context.Dependent;

@Dependent
public class HarmonicOscillatorService {
    public Stream<PhaseSpacePoint> analytic(
        double m, double k, double r0, double v0, double dt
    ) {
        var oscillator = new HarmonicOscillatorProperties(m, k, r0, v0);
        var a = oscillator.amplitude();
        var omega = oscillator.omega();
        var phi = oscillator.phi();
        return xs(dt).map(t -> new PhaseSpacePoint(
                a * Math.cos(omega * t - phi),
                -omega * a * Math.sin(omega * t - phi)));
    }

    public Stream<PhaseSpacePoint> euler(
        double m, double k, double r0, double v0, double dt
    ) {
        return Stream
            .iterate(
                new PhaseSpacePoint(r0, v0), 
                p -> new PhaseSpacePoint(
                    p.x() + p.v() * dt,
                    p.v() + -k * p.x() / m * dt));
    }


    public Stream<PhaseSpacePoint> leapfrog(
        double m, double k, double r0, double v0, double dt
    ) {
        return Stream
            .iterate(
                new PhaseSpacePoint(r0, v0), 
                p -> {
                    var xMid = p.x() + p.v() * dt / 2;
                    var v = p.v() + -k * xMid / m * dt;
                    var x = xMid + v * dt / 2;
                    return new PhaseSpacePoint(x, v);
                });
    }

    public Stream<Double> totalEnergy(double m, double k, Stream<PhaseSpacePoint> trajectory) {
        return trajectory
            .map(p -> k * p.x() * p.x() / 2 + m * p.v() * p.v() / 2);
    }

    public Stream<Double> xs(double dt) {
        return Stream.iterate(0.0, t -> t + dt);
    }

}
