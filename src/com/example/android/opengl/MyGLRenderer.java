/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";
   // private Triangle mTriangle;
    //private Square   mSquare;

    private final float[] MVPMatrix = new float[16];
    private final float[] MVMatrix = new float[16];
    private final float[] projMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    
    private final float[] xRotationMatrix = new float[16];
    private final float[] yRotationMatrix = new float[16];
    private final float[] modelMatrix = new float[16];

    // Declare as volatile because we are updating it from another thread
    public volatile float mAngle;
    public volatile float mAngle2;
    
    private Mesh mMesh;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
//        GLES20.glClearColor(0.2f, 0.2f, 0.25f, 1.0f);
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        ResourceLoader loader = ResourceLoader.getResourceLoader();
        mMesh = loader.getMeshByName("alduin");
        mMesh.initialize();
        
        // depth testing functions
        GLES20.glDepthFunc(GL10.GL_LEQUAL);
        GLES20.glEnable(GL10.GL_DEPTH_TEST);
        GLES20.glEnable(GL10.GL_CULL_FACE);
        GLES20.glCullFace(GL10.GL_BACK);
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -8, 0f, 0f, 0f, 0f, 5.0f, 0.0f);

        Matrix.setRotateM(xRotationMatrix, 0, -mAngle2, 1, 0, 0f);
        Matrix.setRotateM(yRotationMatrix, 0, -mAngle, 0, 1, 0f);
        Matrix.multiplyMM(modelMatrix, 0, xRotationMatrix, 0, yRotationMatrix, 0);
        
        // Combine the rotation matrix with the project and camera view
        Matrix.multiplyMM(MVMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, projMatrix, 0, MVMatrix, 0);
        
        if (mMesh != null) {
        	mMesh.draw(MVPMatrix, MVMatrix);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1, 2, 3, 15);

    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}