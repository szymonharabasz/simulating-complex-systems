package com.szymonharabasz.complexsystems.moleculardynamics.gasinbox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import oshi.util.tuples.Pair;

@Dependent
public class GasInBoxService {

    private static final Random RANDOM = new Random();
    private static final Logger LOGGER = LoggerFactory.getLogger(GasInBoxService.class);

    public List<Particle> initialize(int n, double l, double v0, double sigma) {
        LOGGER.info("Initializing particles");
        List<Particle> particles = new ArrayList<>();

        while (particles.size() < n) {
            var x = RANDOM.nextDouble() * l;
            var y = RANDOM.nextDouble() * l;
            var theta = RANDOM.nextDouble() * 2 * Math.PI;
            boolean collision = false;
            for (var particle : particles) {
                if (Math.hypot(x - particle.x(), y - particle.y()) < 2*sigma) {
                    collision = true;
                    break;
                }
            }
            if (!collision) {
                particles.add(new Particle(x, y, 2 * v0 * Math.cos(theta), 2 * v0 * Math.sin(theta)));
            }
        }
        return particles;
    }

    double force(double epsilon, double sigma, double r) {
        return 4 * epsilon * ( 12 * Math.pow(sigma/r, 12) / r - 6 * Math.pow(sigma/r, 6) / r);
    }

    double force(double epsilon, double sigma, Particle particle1, Particle particle2) {
        return force(epsilon, sigma, dist(particle1, particle2));
    }

    double potentialEnergy (double epsilon, double sigma, double r) {
        BigDecimal ratio = BigDecimal.valueOf(sigma).divide(BigDecimal.valueOf(r), RoundingMode.HALF_UP);
        BigDecimal power12 = ratio.pow(12);
        BigDecimal power6 = ratio.pow(6);
        BigDecimal result = power12.subtract(power6).multiply(BigDecimal.valueOf(4 * epsilon));
        return result.doubleValue();
    }

    double potentialEnergy(double epsilon, double sigma, Particle particle1, Particle particle2) {
        return potentialEnergy(epsilon, sigma, dist(particle1, particle2));
    }

    double kineticEnergy(double m, Particle particle) {
        return m * (Math.pow(particle.vx(), 2) + Math.pow(particle.vy(), 2)) / 2;
    }

    public double totalPotentialEnergy(double epsilon, double sigma, List<Particle> particles) {
        double result = 0.0;
        for (int i = 0; i < particles.size(); ++i) {
            for (int j = 0; j < particles.size(); ++j) {
                if (i != j) {
                    result += 0.5 * potentialEnergy(epsilon, sigma, particles.get(i), particles.get(j));
                }
            }
        }
        return result;
    }

    public double totalKineticEnergy(double m, List<Particle> particles) {
        double result = 0.0;
        for (int i = 0; i < particles.size(); ++i) {
            result += kineticEnergy(m, particles.get(i));
        }
        return result;
    }

    double dist(Particle particle1, Particle particle2) {
        var dx = particle1.x() - particle2.x();
        var dy = particle1.y() - particle2.y();
        return Math.hypot(dx, dy);
    }

    public Stream<List<Particle>> leapfrog(int n, double l, double v0, double m, double epsilon, double sigma, double dt) {
        return leapfrog(initialize(n, l, v0, sigma), l, m, epsilon, sigma, dt);
    }

    public Stream<List<Particle>> leapfrog(List<Particle> initialCondition, double l, double m, double epsilon, double sigma, double dt) {
        return Stream.iterate(initialCondition, particles -> propagate(particles, m, epsilon, sigma, dt, l));
    }

    public List<Particle> propagate(List<Particle> previous, double m, double epsilon, double sigma, double dt, double l) {
        var xMid = previous.stream().map(p -> new Particle(
            p.x() + p.vx() * dt / 2,
            p.y() + p.vy() * dt / 2, 
            p.vx(), p.vy())).toList();
        var f = xMid.stream().map(p -> {
            double fx = 0.0;
            double fy = 0.0;
            for (var other : xMid) {
                if (other != p) {
                    var dx = p.x() - other.x();
                    var dy = p.y() - other.y();
                    var dist = dist(p, other);
                    var forceValue = force(epsilon, sigma, other, p);
                    fx += forceValue * dx / dist;
                    fy += forceValue * dy / dist;
                }
            }
            return new Pair<>(fx, fy);
        }).toList();
        var result = IntStream.range(0, Math.min(xMid.size(), f.size()))
            .mapToObj(i -> {
                var vx = xMid.get(i).vx() + f.get(i).getA() / m * dt;
                var vy = xMid.get(i).vy() + f.get(i).getB() / m * dt;
                var x = xMid.get(i).x() + vx * dt / 2;
                var y = xMid.get(i).y() + vy * dt / 2;
                if (checkLowerBoundary(x)) {
                    x = -x;
                    vx = -vx;
                }
                if (checkUpperBoundary(x, l)) {
                    x = l - x;
                    vx = -vx;
                }
                if (checkLowerBoundary(y)) {
                    y = -y;
                    vy = -vy;
                }
                if (checkUpperBoundary(y, l)) {
                    y = l - y;
                    vy = -vy;
                }
                
                return new Particle(x, y, vx, vy);
            });
        return result.toList();
    }

    boolean checkLowerBoundary(double x) {
        return x < 0;
    }
    boolean checkUpperBoundary(double x, double l) {
        return x > l;
    }
}
