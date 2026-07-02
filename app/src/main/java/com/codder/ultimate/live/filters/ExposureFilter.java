package com.codder.ultimate.live.filters;

import android.opengl.GLES20;
import android.util.Log;

import androidx.annotation.NonNull;

import com.otaliastudios.cameraview.filter.BaseFilter;
import com.otaliastudios.cameraview.filter.OneParameterFilter;
import com.otaliastudios.opengl.core.Egloo;

public class ExposureFilter extends BaseFilter implements OneParameterFilter {

    private static final String TAG = "ExposureFilter";
    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ";\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "uniform float exposure;\n" +
                    "void main()\n" +
                    "{\n" +
                    "    vec4 textureColor = texture2D(sTexture, " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ");\n" +
                    "    gl_FragColor = vec4(textureColor.rgb * pow(2.0, exposure), textureColor.w);\n" +
                    "}";

    private float mExposure = 1f;
    private int mExposureLocation = -1;

    @Override
    public float getParameter1() {
        return mExposure;
    }

    @Override
    public void setParameter1(float value) {
        mExposure = value;
    }

    public float getExposure() {
        return getParameter1();
    }

    public void setExposure(float exposure) {
        setParameter1(exposure);
    }

    @NonNull
    @Override
    public String getFragmentShader() {
        return FRAGMENT_SHADER;
    }

    @Override
    public void onCreate(int programHandle) {
        super.onCreate(programHandle);
        try {
            mExposureLocation = GLES20.glGetUniformLocation(programHandle, "exposure");
            Egloo.checkGlProgramLocation(mExposureLocation, "exposure");

            if (mExposureLocation < 0) {
                Log.e(TAG, "Uniform location for 'exposure' is invalid.");
                // Optionally, you could throw here if it's critical
            }
        } catch (Exception e) {
            mExposureLocation = -1;
            Log.e(TAG, "Exception during onCreate: " + e.getMessage(), e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExposureLocation = -1;
    }

    @Override
    protected void onPreDraw(long timestampUs, @NonNull float[] transformMatrix) {
        super.onPreDraw(timestampUs, transformMatrix);
        // Only set uniform if location is valid
        if (mExposureLocation >= 0) {
            try {
                GLES20.glUniform1f(mExposureLocation, mExposure);
                Egloo.checkGlError("glUniform1f");
            } catch (Exception e) {
                Log.e(TAG, "Error in onPreDraw setting exposure: " + e.getMessage(), e);
            }
        } else {
            Log.w(TAG, "mExposureLocation is invalid, uniform not set.");
        }
    }
}

