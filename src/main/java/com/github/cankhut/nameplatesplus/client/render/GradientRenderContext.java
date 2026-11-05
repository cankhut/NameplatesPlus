package com.github.cankhut.nameplatesplus.client.render;

import com.github.cankhut.nameplatesplus.util.Gradient;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class GradientRenderContext {

    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    private GradientRenderContext() {
    }

    public static Context current() {
        return CURRENT.get();
    }

    public static Scope push(
            Gradient gradient,
            Matrix4f matrix,
            float x,
            float y,
            float width
    ) {
        Context previous = CURRENT.get();

        Vector3f start = matrix.transformPosition(
                x, y, 0.0f, new Vector3f()
        );

        Vector3f end = matrix.transformPosition(
                x + width, y, 0.0f, new Vector3f()
        );

        CURRENT.set(new Context(gradient, start, end));

        return () -> {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        };
    }

    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }

    public static final class Context {

        private final Gradient gradient;

        private final float startX;
        private final float startY;
        private final float startZ;

        private final float axisX;
        private final float axisY;
        private final float axisZ;

        private final float inverseAxisLengthSquared;

        private Context(
                Gradient gradient,
                Vector3f start,
                Vector3f end
        ) {
            this.gradient = gradient;

            this.startX = start.x;
            this.startY = start.y;
            this.startZ = start.z;

            this.axisX = end.x - start.x;
            this.axisY = end.y - start.y;
            this.axisZ = end.z - start.z;

            float lengthSquared =
                    axisX * axisX +
                            axisY * axisY +
                            axisZ * axisZ;

            this.inverseAxisLengthSquared =
                    lengthSquared > 0.000001f
                            ? 1.0f / lengthSquared
                            : 0.0f;
        }

        public int colorAt(float x, float y, float z) {
            float t = 0.0f;

            if (inverseAxisLengthSquared != 0.0f) {
                float dx = x - startX;
                float dy = y - startY;
                float dz = z - startZ;

                t = (
                        dx * axisX +
                                dy * axisY +
                                dz * axisZ
                ) * inverseAxisLengthSquared;
            }

            t = Math.clamp(t, 0.0f, 1.0f);

            return gradient.colorAt(t);
        }
    }
}