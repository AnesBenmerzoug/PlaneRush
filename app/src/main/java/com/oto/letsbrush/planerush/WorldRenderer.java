package com.oto.letsbrush.planerush;

import com.oto.letsbrush.planerush.gl.Animation;
import com.oto.letsbrush.planerush.gl.Camera2D;
import com.oto.letsbrush.planerush.gl.SpriteBatcher;
import com.oto.letsbrush.planerush.gl.TextureRegion;
import com.oto.letsbrush.planerush.implementation.GLGraphics;

import javax.microedition.khronos.opengles.GL10;

public class WorldRenderer {
    static final float FRUSTUM_WIDTH = 18;
    static final float FRUSTUM_HEIGHT = 12;
    GLGraphics glGraphics;
    World world;
    Camera2D cam;
    SpriteBatcher batcher;

    public WorldRenderer(GLGraphics glGraphics, SpriteBatcher batcher, World world) {
        this.glGraphics = glGraphics;
        this.world = world;
        this.cam = new Camera2D(glGraphics, FRUSTUM_WIDTH, FRUSTUM_HEIGHT);
        this.batcher = batcher;
    }

    public void render() {
        if(world.plane.position.x < (World.World_Width - World.World_Height / 2 - FRUSTUM_WIDTH / 3))
            cam.position.x = world.plane.position.x + FRUSTUM_WIDTH / 3;
        cam.setViewportAndMatrices();
        renderBackground();
        renderObjects();
    }

    public void renderBackground() {
        batcher.beginBatch(Assets.Atlas);
        batcher.drawSprite(cam.position.x, cam.position.y,
                FRUSTUM_WIDTH, FRUSTUM_HEIGHT,
                Assets.backgroundRegion);
        batcher.endBatch();
    }

    public void renderObjects() {
        GL10 gl = glGraphics.getGL();
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        batcher.beginBatch(Assets.Atlas);
        renderGround();
        renderPlane();
        renderStars();
        batcher.endBatch();
        gl.glDisable(GL10.GL_BLEND);
    }

    private void renderPlane() {
        if(world.plane.position.x <= world.World_Width + World.World_Height / 2 + FRUSTUM_WIDTH / 3) {
            TextureRegion keyFrame;
            switch (world.plane.state) {
                default:
                    keyFrame = Assets.planeFly.getKeyFrame(world.plane.stateTime, Animation.ANIMATION_LOOPING);
            }
            batcher.drawSprite(world.plane.position.x, world.plane.position.y, Plane.Plane_Width, Plane.Plane_Height, keyFrame);
        }
    }

    private void renderStars(){
        int len = world.stars.size();
        for(int i = 0; i < len; i++) {
            Star star = world.stars.get(i);
            if((int)(star.position.x/FRUSTUM_WIDTH) <= (int)(world.plane.position.x/FRUSTUM_WIDTH) + 1) {
                TextureRegion keyFrame = Assets.star.getKeyFrame(star.stateTime,
                        Animation.ANIMATION_LOOPING);
                batcher.drawSprite(star.position.x, star.position.y, Star.Star_Width, Star.Star_Height, keyFrame);
            }
        }
    }

    private void renderGround(){
        GL10 gl = glGraphics.getGL();
        for(int i = (int)(world.plane.position.x/FRUSTUM_WIDTH) - 1; i < world.plane.position.x/FRUSTUM_WIDTH + FRUSTUM_WIDTH * 2 ; i++) {
            batcher.drawSprite(FRUSTUM_WIDTH *( i + .45f), FRUSTUM_HEIGHT / 16, FRUSTUM_WIDTH, FRUSTUM_HEIGHT / 8, Assets.groundGrass);
        }
    }
}
