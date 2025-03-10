package com.iafenvoy.resgen.util;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ParticleUtils {
    public static void highlight(ServerWorld world, BlockPos pos) {
        Runnable runnable = () -> block(world, ParticleTypes.FLAME, pos);
        runnable.run();
        Timeout.create(10, 20, runnable);
    }

    public static void block(ServerWorld world, ParticleEffect particle, BlockPos pos) {
        Vec3d a = Vec3d.of(pos), b = a.add(1, 0, 0), c = a.add(1, 0, 1), d = a.add(0, 0, 1);
        Vec3d a1 = a.add(0, 1, 0), b1 = a.add(1, 1, 0), c1 = a.add(1, 1, 1), d1 = a.add(0, 1, 1);
        int count = 6;

        line(world, particle, a, b, count);
        line(world, particle, b, c, count);
        line(world, particle, c, d, count);
        line(world, particle, d, a, count);

        line(world, particle, a, a1, count);
        line(world, particle, b, b1, count);
        line(world, particle, c, c1, count);
        line(world, particle, d, d1, count);

        line(world, particle, a1, b1, count);
        line(world, particle, b1, c1, count);
        line(world, particle, c1, d1, count);
        line(world, particle, d1, a1, count);
    }

    public static void line(ServerWorld world, ParticleEffect particle, Vec3d start, Vec3d end, int count) {
        Vec3d delta = end.subtract(start).multiply(1.0 / (count - 1));
        for (int i = 0; i < count; i++)
            point(world, particle, start.add(delta.multiply(i)));
    }

    public static void point(ServerWorld world, ParticleEffect particle, Vec3d pos) {
        world.spawnParticles(particle, pos.x, pos.y, pos.z, 0, 0, 0, 0, 0);
    }
}
