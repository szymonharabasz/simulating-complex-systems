package com.szymonharabasz.complexsystems.moleculardynamics;

import java.util.stream.Stream;

import jakarta.enterprise.context.Dependent;

@Dependent
public class HarmonicOscillatorService {
    public Stream<PhaseSpacePoint> analytic(
        HarmonicOscillatorProperties oscillator, double dt
    ) {
        var a = oscillator.amplitude();
        var omega = oscillator.omega();
        var phi = oscillator.phi();
        return xs(dt).map(t -> new PhaseSpacePoint(
                a * Math.cos(omega * t - phi),
                -omega * a * Math.sin(omega * t - phi), 
                totalEnergy(oscillator)));
    }

    public Stream<PhaseSpacePoint> euler(
        HarmonicOscillatorProperties oscillator, double dt
    ) {
        return Stream
            .iterate(
                new PhaseSpacePoint(oscillator.r0(), oscillator.v0(), totalEnergy(oscillator)), 
                p -> {
                    var f = -oscillator.k() * p.x() - oscillator.bVal() * p.v(); 
                    var x = p.x() + p.v() * dt;
                    var v = p.v() + f / oscillator.m() * dt;
                    return new PhaseSpacePoint(x, v, totalEnergy(oscillator.m(), oscillator.k(), x, v));
                });
    }


    public Stream<PhaseSpacePoint> leapfrog(
        HarmonicOscillatorProperties oscillator, double dt
    ) {
        return Stream
            .iterate(
                new PhaseSpacePoint(oscillator.r0(), oscillator.v0(), totalEnergy(oscillator  )), 
                p -> {
                    var xMid = p.x() + p.v() * dt / 2;
                    var f = -oscillator.k() * xMid - oscillator.bVal() * p.v();
                    var v = p.v() + f / oscillator.m() * dt;
                    var x = xMid + v * dt / 2;
                    return new PhaseSpacePoint(x, v, totalEnergy(oscillator.m(), oscillator.k(), x, v));
                });
    }

    public double totalEnergy(HarmonicOscillatorProperties oscillator)
    {
        return totalEnergy(oscillator.m(), oscillator.k(), oscillator.r0(), oscillator.v0());
    }

    public double totalEnergy(double m, double k, double x, double v) {
        return k * x * x / 2 + m * v * v /2;
    }

    public Stream<Double> xs(double dt) {
        return Stream.iterate(0.0, t -> t + dt);
    }

}
