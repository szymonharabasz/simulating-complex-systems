package com.szymonharabasz.complexsystems.moleculardynamics;

public record HarmonicOscillatorProperties(double m, double k, double r0, double v0) {
    double omega() { return Math.sqrt(k/m); }
    double amplitude() { return Math.hypot(r0, v0/omega()); }
    double phi() { return Math.acos(r0 / amplitude()); }
}
