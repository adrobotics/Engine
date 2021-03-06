package com.blackboxgaming.engine.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.utils.Disposable;
import com.blackboxgaming.engine.components.Model;
import com.blackboxgaming.engine.components.Transform;
import com.blackboxgaming.engine.Engine;
import com.blackboxgaming.engine.Entity;
import com.blackboxgaming.engine.components.Health;
import com.blackboxgaming.engine.components.Shadow;
import com.blackboxgaming.engine.factories.ModelFactory;
import com.blackboxgaming.engine.util.Global;
import java.util.ArrayList;
import java.util.List;

public class ModelRendererSystem implements ISystem, Disposable {

    private final List<Entity> entities = new ArrayList();
    private final List<RenderableProvider> renderables = new ArrayList();
    private final List<RenderableProvider> shadows = new ArrayList();
    private final List<com.badlogic.gdx.graphics.g3d.Model> toBeDisposedModels = new ArrayList();
    public static final ModelBatch modelBatch = new ModelBatch();
    private final ModelBatch shadowBatch = new ModelBatch(new DepthShaderProvider());
    private final ModelBuilder modelBuilder = new ModelBuilder();
    private final BoundingBox bounds = new BoundingBox();
    private final Vector3 center = new Vector3();
    private final Vector3 dimensions = new Vector3();
    private final float rotationArrowLength = 2;
    private DebugDrawer debugDrawer;
    private Box2DDebugRenderer debugDrawer2D;
    private ModelInstance frustrumSphere;
    private ModelInstance arrowModel;
    private Matrix4 transform;
    private ModelInstance modelInstance;
    private float radius;
    private boolean castShadow;

    @Override
    public void add(Entity entity) {
        if (!entities.contains(entity)) {
            entities.add(entity);
            if(entity.has(Health.class)){
                if(Engine.systemManager.has(HealthBarRendererSystem.class)){
                    Engine.systemManager.get(HealthBarRendererSystem.class).add(entity);
                }
            }
        }
    }

    @Override
    public void remove(Entity entity) {
        entities.remove(entity);
    }

    @Override
    public void update(float delta) {
        clear();

        for (Entity entity : entities) {
            transform = entity.get(Transform.class).transform;
            modelInstance = entity.get(Model.class).modelInstance;
            castShadow = (Global.SHADOW && entity.has(Shadow.class));

            if (Global.FRUSTRUM_CULLING) {
                modelInstance.calculateBoundingBox(bounds);
                center.set(bounds.getCenter(Vector3.Zero));
                dimensions.set(bounds.getDimensions(Vector3.Zero));
                radius = dimensions.len() / 2f;

                if (Global.getCamera().frustum.sphereInFrustum(transform.getTranslation(Vector3.Zero), radius)) {
                    renderables.add(modelInstance);
                    if (castShadow) {
                        shadows.add(modelInstance);
                    }

                    if (Global.DEBUG_FRUSTRUM_CULLING_SHAPES) {
                        com.badlogic.gdx.graphics.g3d.Model g3dModel = modelBuilder.createSphere(radius * 2f, radius * 2f, radius * 2f, 10, 10, GL20.GL_LINES, new Material(ColorAttribute.createDiffuse(Color.GREEN)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
                        toBeDisposedModels.add(g3dModel);
                        frustrumSphere = new ModelInstance(g3dModel);
                        frustrumSphere.transform.trn(transform.getTranslation(Vector3.Zero));
                        renderables.add(frustrumSphere);
                    }
                }
            } else {
                renderables.add(modelInstance);
                if (castShadow) {
                    shadows.add(modelInstance);
                }
            }

            if (Global.DEBUG_ROTATION) {
                com.badlogic.gdx.graphics.g3d.Model g3dModel = ModelFactory.getArrowModel(new Vector3(0, 0, 0), new Vector3(rotationArrowLength, 0, 0));
                toBeDisposedModels.add(g3dModel);
                arrowModel = new ModelInstance(g3dModel);
                arrowModel.transform.set(transform);
                renderables.add(arrowModel);
            }
        }

        renderShadows();
        render();
        debugPhysics();
    }

    private void clear() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        Gdx.gl.glClearColor(0.141f, 0.376f, 0.408f, 1.f);
        Gdx.gl.glClearColor(0.055f, 0.275f, 0.306f, 1.f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Global.VISIBLE_OBJECT_COUNT = 0;
        for (com.badlogic.gdx.graphics.g3d.Model model : toBeDisposedModels) {
            model.dispose();
        }
        renderables.clear();
        shadows.clear();
        toBeDisposedModels.clear();
    }

    private void render() {
        Global.VISIBLE_OBJECT_COUNT = renderables.size();
        modelBatch.begin(Global.getCamera());
        modelBatch.render(renderables, Global.getEnvironment());
        modelBatch.end();
    }

    private void renderShadows() {
        Global.getShadowLight().begin(Vector3.Zero, Global.getCamera().direction);
        shadowBatch.begin(Global.getShadowLight().getCamera());
        shadowBatch.render(shadows);
        shadowBatch.end();
        Global.getShadowLight().end();
        Gdx.gl.glClearColor(0, 0, 0, 1.f);
    }

    private void debugPhysics() {
        if (Global.DEBUG_PHYSICS && Engine.systemManager.has(PhysicsSystem.class)) {
            if (debugDrawer == null) {
                debugDrawer = new DebugDrawer();
                debugDrawer.setDebugMode(1);
                Global.getDynamicsWorld().setDebugDrawer(debugDrawer);
            }
            debugDrawer.begin(Global.getCamera());
            Global.getDynamicsWorld().debugDrawWorld();
            debugDrawer.end();
        }
        
        if (Global.DEBUG_PHYSICS && Engine.systemManager.has(PhysicsSystem2D.class)) {
            if (debugDrawer2D == null) {
                debugDrawer2D = new Box2DDebugRenderer(true, true, true, true, true, true);
            }
            
            debugDrawer2D.render(Global.getDynamicsWorld2D(), Global.getCamera().combined.cpy().rotate(Vector3.X, 90));
        }
    }

    @Override
    public void dispose() {
        System.out.println("Disposing " + this.getClass());
        for (com.badlogic.gdx.graphics.g3d.Model model : toBeDisposedModels) {
            model.dispose();
        }
        toBeDisposedModels.clear();
        renderables.clear();
        shadows.clear();
        modelBatch.dispose();
        shadowBatch.dispose();
        if (debugDrawer != null) {
            debugDrawer.dispose();
        }
        if (debugDrawer2D != null) {
            debugDrawer2D.dispose();
        }
        entities.clear();
    }

}
