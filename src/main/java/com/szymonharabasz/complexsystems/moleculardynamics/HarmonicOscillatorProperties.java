package com.szymonharabasz.complexsystems.moleculardynamics;

public record HarmonicOscillatorProperties(double m, double k, double b, double r0, double v0) {
    public double omega() { return Math.sqrt(k/m); }
    public double amplitude() { return Math.hypot(r0, v0/omega()); }
    public double phi() { return Math.signum(v0) * Math.acos(r0 / amplitude()); }
    public double period() { return 2 * Math.PI / omega(); }
    public double bVal() { return b * 2 * Math.sqrt(k * m); }
}
