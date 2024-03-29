package com.szymonharabasz.complexsystems.moleculardynamics;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class HarmonicOscillatorService {
    Stream<PhaseSpacePoint> analytic(
        double m, double k, double r0, double v0, double dt, double tmax
    ) {
        var n = Math.round(tmax / dt);
        var oscillator = new HarmonicOscillatorProperties(m, k, r0, v0);
        var a = oscillator.amplitude();
        var omega = oscillator.omega();
        var phi = oscillator.phi();
        return LongStream.range(0, n)
            .boxed()
            .map(i -> i * dt)
            .map(t -> new PhaseSpacePoint(
                a * Math.cos(omega * t + phi),
                -omega * a * Math.sin(omega * t + phi)));
    }

    Stream<PhaseSpacePoint> euler(
        double m, double k, double r0, double v0, double dt, double tmax
    ) {
        var n = Math.round(tmax / dt);
        return Stream
            .iterate(
                new PhaseSpacePoint(r0, v0), 
                p -> new PhaseSpacePoint(
                    p.x() + p.v() * dt,
                    p.v() + -k * p.x() / m * dt))
            .limit(n);
    }


    Stream<PhaseSpacePoint> leapfrog(
        double m, double k, double r0, double v0, double dt, double tmax
    ) {
        var n = Math.round(tmax / dt);
        return Stream
            .iterate(
                new PhaseSpacePoint(r0, v0), 
                p -> {
                    var xMid = p.x() + p.v() * dt / 2;
                    var v = p.v() + -k * xMid / m * dt;
                    var x = xMid + v * dt / 2;
                    return new PhaseSpacePoint(x, v);
                })
            .limit(n);
    }

    Stream<Double> totalEnergy(double m, double k, Stream<PhaseSpacePoint> trajectory) {
        return trajectory
            .map(p -> k * p.x() * p.x() / 2 + m * p.v() * p.v() / 2);
    }

}
