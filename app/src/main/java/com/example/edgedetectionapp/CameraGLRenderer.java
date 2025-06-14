package com.example.edgedetectionapp;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraGLRenderer implements GLSurfaceView.Renderer {

    private int textureId;
    private Bitmap bitmap;
    private boolean updateTexture = false;

    // Full-screen quad vertices (x, y)
    private final float[] vertexData = {
            -1f,  1f,
            -1f, -1f,
            1f,  1f,
            1f, -1f
    };

    // Corresponding texture coordinates (u, v)
    private final float[] texCoordData = {
            0f, 0f,
            0f, 1f,
            1f, 0f,
            1f, 1f
    };

    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordBuffer;

    private int program;
    private int aPosition;
    private int aTexCoord;
    private int uTexture;

    private boolean textureInitialized = false;
    public CameraGLRenderer() {
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(vertexData).position(0);

        texCoordBuffer = ByteBuffer.allocateDirect(texCoordData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        texCoordBuffer.put(texCoordData).position(0);
    }

//    public void updateBitmap(Bitmap bmp) {
//        if (bmp != null && !bmp.isRecycled()) {
//            this.bitmap = bmp;
//            updateTexture = true;
//        }
//    }
public void updateBitmap(Bitmap bmp) {
    if (bmp != null && !bmp.isRecycled()) {
        this.bitmap = bmp;
        updateTexture = true;
    }
}
//    public void updateBitmap(Bitmap bmp) {
//        if (bmp != null && !bmp.isRecycled()) {
//            this.bitmap = bmp;
//
//            // Allocate texture only once
//            if (!textureInitialized) {
//                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
//                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
//                textureInitialized = true;
//            } else {
//                updateTexture = true;  // will call texSubImage2D later
//            }
//        }
//    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 1);
        textureId = createTexture();
        program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        aPosition = GLES20.glGetAttribLocation(program, "a_Position");
        aTexCoord = GLES20.glGetAttribLocation(program, "a_TexCoord");
        uTexture = GLES20.glGetUniformLocation(program, "u_Texture");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

//    @Override
//    public void onDrawFrame(GL10 gl) {
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//
//        GLES20.glUseProgram(program);
//
//        GLES20.glEnableVertexAttribArray(aPosition);
//        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
//
//        GLES20.glEnableVertexAttribArray(aTexCoord);
//        GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);
//
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
//        GLES20.glUniform1i(uTexture, 0);
//
//        if (updateTexture && bitmap != null) {
//            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap);
//            updateTexture = false;
//        }
//
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
//
//        GLES20.glDisableVertexAttribArray(aPosition);
//        GLES20.glDisableVertexAttribArray(aTexCoord);
//    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(program);

        GLES20.glEnableVertexAttribArray(aPosition);
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        GLES20.glEnableVertexAttribArray(aTexCoord);
        GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(uTexture, 0);
        Log.d("GLRenderer", "onDrawFrame called");
        if (updateTexture && bitmap != null) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

            if (!textureInitialized) {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                textureInitialized = true;
            } else {
                GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap);
            }

            updateTexture = false;
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(aPosition);
        GLES20.glDisableVertexAttribArray(aTexCoord);
    }


//    @Override
//    public void onDrawFrame(GL10 gl) {
//        GLES20.glClearColor(1f, 0f, 0f, 1f); // Bright red background
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//
//        // TEMP: comment out everything that draws the texture
//        // GLES20.glUseProgram(program);
//        // GLES20.glEnableVertexAttribArray(aPosition);
//        // GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
//        // GLES20.glEnableVertexAttribArray(aTexCoord);
//        // GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);
//        // GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        // GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
//        // GLES20.glUniform1i(uTexture, 0);
//
//        Log.d("GLRenderer", "onDrawFrame called");
//    }

    private int createTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int id = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        return id;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        return program;
    }

    private int loadShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        return shader;
    }

    // Vertex shader
    private static final String VERTEX_SHADER =
            "attribute vec4 a_Position;" +
                    "attribute vec2 a_TexCoord;" +
                    "varying vec2 v_TexCoord;" +
                    "void main() {" +
                    "  gl_Position = a_Position;" +
                    "  v_TexCoord = a_TexCoord;" +
                    "}";

    // Fragment shader
    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "uniform sampler2D u_Texture;" +
                    "varying vec2 v_TexCoord;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(u_Texture, v_TexCoord);" +
                    "}";
}
