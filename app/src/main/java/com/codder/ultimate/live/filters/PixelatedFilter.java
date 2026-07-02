package com.codder.ultimate.live.filters;

import android.opengl.GLES20;
import android.util.Log;

import androidx.annotation.NonNull;

import com.otaliastudios.cameraview.filter.BaseFilter;
import com.otaliastudios.cameraview.filter.TwoParameterFilter;
import com.otaliastudios.opengl.core.Egloo;

public class PixelatedFilter extends BaseFilter implements TwoParameterFilter {

    private static final String TAG = "PixelatedFilter";
    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision highp float;\n" +
                    "varying highp vec2 " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ";\n" +
                    "uniform float imageSizeFactor;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "uniform float pixel;\n" +
                    "void main()\n" +
                    "{\n" +
                    "    vec2 uv  = " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ".xy;\n" +
                    "    float dx = pixel * imageSizeFactor;\n" +
                    "    float dy = pixel * imageSizeFactor;\n" +
                    "    vec2 coord = vec2(dx * floor(uv.x / dx), dy * floor(uv.y / dy));\n" +
                    "    vec3 tc = texture2D(sTexture, coord).xyz;\n" +
                    "    gl_FragColor = vec4(tc, 1.0);\n" +
                    "}";

    private float mPixel = 1f;
    private int mPixelLocation = -1;
    private float mImageSizeFactor = 1f / 720;
    private int mImageSizeFactorLocation = -1;

    @NonNull
    @Override
    public String getFragmentShader() {
        return FRAGMENT_SHADER;
    }

    public float getImageSizeFactor() {
        return getParameter2();
    }

    public void setImageSizeFactor(float factor) {
        setParameter2(factor);
    }

    @Override
    public float getParameter1() {
        return mPixel;
    }

    @Override
    public void setParameter1(float value) {
        mPixel = value;
    }

    @Override
    public float getParameter2() {
        return mImageSizeFactor;
    }

    @Override
    public void setParameter2(float value) {
        mImageSizeFactor = value;
    }

    public float getPixel() {
        return getParameter1();
    }

    public void setPixel(float pixel) {
        setParameter1(pixel);
    }

    @Override
    public void onCreate(int programHandle) {
        super.onCreate(programHandle);
        try {
            mPixelLocation = GLES20.glGetUniformLocation(programHandle, "pixel");
            Egloo.checkGlProgramLocation(mPixelLocation, "pixel");
            if (mPixelLocation < 0) {
                Log.e(TAG, "Uniform location for 'pixel' is invalid.");
            }
        } catch (Exception e) {
            mPixelLocation = -1;
            Log.e(TAG, "Exception locating 'pixel' uniform: " + e.getMessage(), e);
        }

        try {
            mImageSizeFactorLocation = GLES20.glGetUniformLocation(programHandle, "imageSizeFactor");
            Egloo.checkGlProgramLocation(mImageSizeFactorLocation, "imageSizeFactor");
            if (mImageSizeFactorLocation < 0) {
                Log.e(TAG, "Uniform location for 'imageSizeFactor' is invalid.");
            }
        } catch (Exception e) {
            mImageSizeFactorLocation = -1;
            Log.e(TAG, "Exception locating 'imageSizeFactor' uniform: " + e.getMessage(), e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPixelLocation = -1;
        mImageSizeFactorLocation = -1;
    }

    @Override
    protected void onPreDraw(long timestampUs, @NonNull float[] transformMatrix) {
        super.onPreDraw(timestampUs, transformMatrix);
        // Defensive: only set uniforms if location is valid
        try {
            if (mPixelLocation >= 0) {
                GLES20.glUniform1f(mPixelLocation, mPixel);
                Egloo.checkGlError("glUniform1f(pixel)");
            } else {
                Log.w(TAG, "mPixelLocation is invalid, not setting uniform.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting pixel uniform: " + e.getMessage(), e);
        }
        try {
            if (mImageSizeFactorLocation >= 0) {
                GLES20.glUniform1f(mImageSizeFactorLocation, mImageSizeFactor);
                Egloo.checkGlError("glUniform1f(imageSizeFactor)");
            } else {
                Log.w(TAG, "mImageSizeFactorLocation is invalid, not setting uniform.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting imageSizeFactor uniform: " + e.getMessage(), e);
        }
    }
}

