package com.szymonharabasz.complexsystems.moleculardynamics;

public record HarmonicOscillatorProperties(double m, double k, double r0, double v0) {
    public double omega() { return Math.sqrt(k/m); }
    public double amplitude() { return Math.hypot(r0, v0/omega()); }
    public double phi() { return Math.acos(r0 / amplitude()); }
    public double period() { return 2 * Math.PI / omega(); }
}
