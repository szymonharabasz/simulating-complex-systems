package com.szymonharabasz.complexsystems.moleculardynamics;

public record HarmonicOscillatorProperties(double m, double k, double b, double r0, double v0) {
    public double omega() { return Math.sqrt(k/m - Math.pow(b2m(), 2)); }
    public double amplitude() { 
        if (Double.isNaN(omega())) {
            return r0; 
        }
        return Math.hypot(r0, (v0 + b2m() *r0)/omega()); 
    }
    public double phi() { return Math.signum(v0) * Math.acos(r0 / amplitude()); }
    public double period() { 
        if (Double.isNaN(omega())) {
            return 2 * Math.PI / Math.sqrt(k/m); 
        }
        return 2 * Math.PI / omega(); 
    }
    public double bVal() { return b * 2 * Math.sqrt(k * m); }
    public double b2m() { return bVal()/(2*m); }
}
