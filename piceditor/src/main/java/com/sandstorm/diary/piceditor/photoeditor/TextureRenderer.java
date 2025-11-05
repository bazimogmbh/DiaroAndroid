package com.sandstorm.diary.piceditor.photoeditor;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class TextureRenderer implements GLSurfaceView.Renderer {
    private static final String FRAGMENT_SHADER = "precision mediump float;\nuniform sampler2D tex_sampler;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler, v_texcoord);\n}\n";
    private static final float[] POS_VERTICES = {-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f};
    private static final float[] TEX_VERTICES = {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};
    private static final String VERTEX_SHADER = "attribute vec4 a_position;\nattribute vec2 a_texcoord;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_Position = a_position;\n  v_texcoord = a_texcoord;\n}\n";
    private int mPosCoordHandle;
    private FloatBuffer mPosVertices;
    private int mProgram;
    private int mTexCoordHandle;
    private int mTexHeight;
    private int mTexSamplerHandle;
    private FloatBuffer mTexVertices;
    private int mTexWidth;
    private int mViewHeight;
    private int mViewWidth;

    public void onDrawFrame(GL10 gl10) {
    }

    public void onSurfaceChanged(GL10 gl10, int i, int i2) {
    }

    TextureRenderer() {
    }

    public void init() {
        this.mProgram = GLToolbox.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        this.mTexSamplerHandle = GLES20.glGetUniformLocation(this.mProgram, "tex_sampler");
        this.mTexCoordHandle = GLES20.glGetAttribLocation(this.mProgram, "a_texcoord");
        this.mPosCoordHandle = GLES20.glGetAttribLocation(this.mProgram, "a_position");
        this.mTexVertices = ByteBuffer.allocateDirect(TEX_VERTICES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mTexVertices.put(TEX_VERTICES).position(0);
        this.mPosVertices = ByteBuffer.allocateDirect(POS_VERTICES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mPosVertices.put(POS_VERTICES).position(0);
    }

    public void tearDown() {
        GLES20.glDeleteProgram(this.mProgram);
    }

    public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
        init();
    }
}
