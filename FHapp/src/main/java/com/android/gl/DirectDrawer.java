package com.android.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.app.util.log;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class DirectDrawer {
	private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
            "attribute vec2 inputTextureCoordinate;" +
            "varying vec2 textureCoordinate;" +
            "void main()" +
            "{"+
                "gl_Position = vPosition;"+
                "textureCoordinate = inputTextureCoordinate;" +
            "}";

    private final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n"+
            "precision mediump float;" +
            "varying vec2 textureCoordinate;\n" +
            "uniform samplerExternalOES s_texture;\n" +
            "void main() {" +
            "  gl_FragColor = texture2D( s_texture, textureCoordinate );\n" +
            "}";

    private FloatBuffer vertexBuffer, textureVerticesBuffer;
    private ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mTextureCoordHandle;

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 2;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    static float squareCoords[] = {
       -1.0f,  1.0f,
       -1.0f, -1.0f,
        1.0f, -1.0f,
        1.0f,  1.0f,
    };

    static float textureVertices[] = {

    	0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,
    };

    private int texture;

    public DirectDrawer(int texture)
    {
        this.texture = texture;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureVerticesBuffer = bb2.asFloatBuffer();
        textureVerticesBuffer.put(textureVertices);
        textureVerticesBuffer.position(0);

        int vertexShader    = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader  = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL ES program executables
    }

    public void draw(float[] mtx, int[][] viewPort)
    {
    	int i;
        GLES20.glUseProgram(mProgram);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the <insert shape here> coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(mTextureCoordHandle);
        
//        textureVerticesBuffer.clear();
//        textureVerticesBuffer.put( transformTextureCoordinates( textureVertices, mtx ));
//        textureVerticesBuffer.position(0);
        
        GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);
        if (null == viewPort)
        {
        	GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        }
        else
        {
        	for(i = 0; i < viewPort.length; i++)
            {
            	GLES20.glViewport(viewPort[i][0], viewPort[i][1], viewPort[i][2], viewPort[i][3]);
            	GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
            }
        }
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
    }
    
    private  int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
    private float[] transformTextureCoordinates( float[] coords, float[] matrix)
    {          
       float[] result = new float[ coords.length ];        
       float[] vt = new float[4];      

       for ( int i = 0 ; i < coords.length ; i += 2 ) {
           float[] v = { coords[i], coords[i+1], 0 , 1  };
           Matrix.multiplyMV(vt, 0, matrix, 0, v, 0);
           result[i] = vt[0];
           result[i+1] = vt[1];
       }
       return result;
    }
}
