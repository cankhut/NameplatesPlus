package com.github.cankhut.nameplatesplus.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

public final class GradientVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;
    private final GradientRenderContext.Context context;

    private float x;
    private float y;
    private float z;

    public GradientVertexConsumer(
            VertexConsumer delegate,
            GradientRenderContext.Context context
    ) {
        this.delegate = delegate;
        this.context = context;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;

        delegate.addVertex(x, y, z);

        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        int color = context.colorAt(x, y, z);

        delegate.setColor(
                (color >> 16) & 0xFF,
                (color >> 8) & 0xFF,
                color & 0xFF,
                a
        );

        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        delegate.setNormal(x, y, z);
        return this;
    }
}