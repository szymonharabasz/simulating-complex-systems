package com.szymonharabasz.complexsystems.moleculardynamics.gasinbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jakarta.enterprise.context.Dependent;
import oshi.util.tuples.Pair;

@Dependent
public class GasInBoxService {

    private static final Random RANDOM = new Random();

    List<Particle> initialize(int n, int l, double v0, double sigma) {
        List<Particle> particles = new ArrayList<>();

        while (particles.size() < n) {
            var x = RANDOM.nextDouble() * l;
            var y = RANDOM.nextDouble() * l;
            var theta = RANDOM.nextDouble() * 2 * Math.PI;
            boolean collision = false;
            for (var particle : particles) {
                if (Math.hypot(x - particle.x(), y - particle.y()) < sigma) {
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

    double force (double epsilon, double sigma, double r) {
        return 4 * epsilon * ( 12 * Math.pow(sigma/r, 12) / r - 6 * Math.pow(sigma/r, 6) / r);
    }

    public Stream<List<Particle>> leapfrog(int n, int l, double v0, double m, double epsilon, double sigma, double dt) {
        return Stream.iterate(initialize(n, l, v0, sigma), particles -> propagate(particles, m, epsilon, sigma, dt));
    }

    List<Particle> propagate(List<Particle> previous, double m, double epsilon, double sigma, double dt) {
        var xMid = previous.stream().map(p -> new Particle(
            p.x() + p.vx() * dt / 2,
            p.y() + p.vy() * dt / 2, 
            p.vx(), p.vy())).toList();
        var f = xMid.stream().map(p -> {
            double fx = 0.0;
            double fy = 0.0;
            for (var other : xMid) {
                if (other != p) {
                    var dx = other.x() - p.x();
                    var dy = other.y() - p.y();
                    var dist = Math.hypot(dx, dy);
                    var forceValue = force(epsilon, sigma, dist);
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
                return new Particle(x, y, vx, vy);
            });
        return result.toList();
    }
}
