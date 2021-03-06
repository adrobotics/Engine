package com.blackboxgaming.engine.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.blackboxgaming.engine.Engine;
import com.blackboxgaming.engine.Entity;
import com.blackboxgaming.engine.components.Physics;
import com.blackboxgaming.engine.components.Transform;
import com.blackboxgaming.engine.systems.PhysicsSystem;
import com.blackboxgaming.engine.util.Global;
import com.blackboxgaming.engine.util.WorldSetupUtil;
//import org.apache.commons.math3.util.FastMath;

/**
 *
 * @author Adrian
 */
public class BallFlingDetector extends GestureDetector {

    public BallFlingDetector() {
        super(new BallFlingAdaptor());
        setTapSquareSize(1);
    }

}

class BallFlingAdaptor extends GestureAdapter {

    private Entity ball;
    public static double lastFlingVelocity;
    private final Vector3 intersection = new Vector3();
    private float yLevel = 0 + 0.5f;

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (Intersector.intersectRayPlane(Global.getCamera().getPickRay(x, y), new Plane(Vector3.Y, 0), intersection)) {
            ball = WorldSetupUtil.createBall(intersection.add(0, yLevel, 0));
        }
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (Engine.systemManager.has(PhysicsSystem.class)) {
            ball.add(WorldSetupUtil.createBallPhysics());
            Engine.systemManager.get(PhysicsSystem.class).add(ball);
        }
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (Intersector.intersectRayPlane(Global.getCamera().getPickRay(x, y), new Plane(Vector3.Y, 0), intersection)) {
            ball.get(Transform.class).transform.setToTranslation(intersection.add(0, yLevel, 0));
        }
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        if (Engine.systemManager.has(PhysicsSystem.class)) {
            ball.add(WorldSetupUtil.createBallPhysics());
            Engine.systemManager.get(PhysicsSystem.class).add(ball);
        }
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
//        lastFlingVelocity = FastMath.hypot(velocityX, velocityY);
        float vx = -(velocityY * Global.boxLength / Gdx.graphics.getHeight());
        float vz = velocityX * Global.boxWidth / Gdx.graphics.getWidth();
        if (Engine.systemManager.has(PhysicsSystem.class)) {
            ball.get(Physics.class).body.applyCentralImpulse(new Vector3(vx, 0, vz).scl(1.0f));
        }
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        if (Engine.systemManager.has(PhysicsSystem.class)) {
            ball.add(WorldSetupUtil.createBallPhysics());
            Engine.systemManager.get(PhysicsSystem.class).add(ball);
        }
        return false;
    }

}
