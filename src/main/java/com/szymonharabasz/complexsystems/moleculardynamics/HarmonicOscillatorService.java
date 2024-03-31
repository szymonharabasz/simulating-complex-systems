package com.szymonharabasz.complexsystems.moleculardynamics;

import java.util.stream.Stream;

import jakarta.enterprise.context.Dependent;

@Dependent
public class HarmonicOscillatorService {

    private static final double SMALL = 1e-6;

    public Stream<PhaseSpacePoint> analytic(
        HarmonicOscillatorProperties oscillator, double dt
    ) {
        var omega = oscillator.omega();
        var phi = oscillator.phi();
        var b2m = oscillator.b2m();
        return xs(dt).map(t -> {
            if (Math.abs(oscillator.b() - 1.0) < SMALL ) {
                var a1 = 1.;//oscillator.r0();
                var a2 = (oscillator.v0() + b2m * oscillator.r0())/oscillator.r0();
                var x = (a1 + a2*t) * Math.exp(-b2m * t);
                var v = (a2 - b2m * (a1 + a2*t)) * Math.exp(-b2m * t);
                return new PhaseSpacePoint(x, v, totalEnergy(oscillator.m(), oscillator.k(), x, v));
            } else if (oscillator.b() < 1.0) {
                var x = Math.exp(-b2m * t) * Math.cos(omega * t - phi);
                var v = -Math.exp(-b2m * t) * (b2m * Math.cos(omega * t - phi) + omega * Math.sin(omega * t - phi));
                return new PhaseSpacePoint(x, v, totalEnergy(oscillator.m(), oscillator.k(), x, v));
            } else {
                var delta = b2m * b2m - oscillator.k() / oscillator.m();
                var l1 = -b2m + Math.sqrt(delta);
                var l2 = -b2m - Math.sqrt(delta);
                var a1 = 0.5 * (1.0 + (oscillator.v0() + b2m * oscillator.r0())/Math.sqrt(delta)/oscillator.r0());
                var a2 = 0.5 * (1.0 - (oscillator.v0() + b2m * oscillator.r0())/Math.sqrt(delta)/oscillator.r0());
                var x = a1 * Math.exp(l1 * t) + a2 * Math.exp(l2 * t);
                var v = l1 * a1 * Math.exp(l1 * t) + l2 * a2 * Math.exp(l2 * t);
                return new PhaseSpacePoint(x, v, totalEnergy(oscillator.m(), oscillator.k(), x, v));

            }
        });
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
