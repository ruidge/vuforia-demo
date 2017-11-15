/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.SampleApplication.utils;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;


public class Banana extends MeshObject {
    private static final String TAG = "Banana";

    private Buffer mVertBuff;//顶点
    private Buffer mTexCoordBuff;//纹理坐标
    private Buffer mNormBuff;//normal
    private int verticesNumber = 0;
    private AssetManager assetManager;

    public Banana(AssetManager inputassetManager) {
        this.assetManager = inputassetManager;
        setVerts();
        setTexCoords();
        setNorms();
    }

    double[] model_VERTS;
    double[] model_TEX_COORDS;
    double[] model_NORMS;
    InputStream inputFile = null;

    private int loadVertsFromModel(String fileName) throws IOException {
        try {
            inputFile = assetManager.open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputFile));
            String line = reader.readLine();
            int floatsToRead = Integer.parseInt(line);
            model_VERTS = new double[3 * floatsToRead];
            for (int i = 0; i < floatsToRead; i++) {
                String curline = reader.readLine();
                if (curline.indexOf('/') >= 0) {
                    i--;
                    continue;
                }
//                将一行分成3个数据
                String floatStrs[] = curline.split(",");
                model_VERTS[3 * i] = Float.parseFloat(floatStrs[0]);
                model_VERTS[3 * i + 1] = Float.parseFloat(floatStrs[1]);
                model_VERTS[3 * i + 2] = Float.parseFloat(floatStrs[2]);

            }
            return floatsToRead;
        } finally {
            if (inputFile != null) {
                inputFile.close();
            }
        }
    }

    private int loadTexCoordsFromModel(String fileName)
            throws IOException {
        try {
            inputFile = assetManager.open(fileName);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputFile));

            String line = reader.readLine();

            int floatsToRead = Integer.parseInt(line);
            model_TEX_COORDS = new double[2 * floatsToRead];


            for (int i = 0; i < floatsToRead; i++) {

                String curline = reader.readLine();
                if (curline.indexOf('/') >= 0) {
                    i--;
                    continue;
                }

                //将一行分成两个数据
                String floatStrs[] = curline.split(",");

                model_TEX_COORDS[2 * i] = Float.parseFloat(floatStrs[0]);
                model_TEX_COORDS[2 * i + 1] = Float.parseFloat(floatStrs[1]);
            }
            return floatsToRead;
        } finally {
            if (inputFile != null)
                inputFile.close();
        }
    }

    private int loadNormsFromModel(String fileName)
            throws IOException {
        try {
            inputFile = assetManager.open(fileName);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputFile));

            String line = reader.readLine();
            int floatsToRead = Integer.parseInt(line);
            model_NORMS = new double[3 * floatsToRead];


            for (int i = 0; i < floatsToRead; i++) {

                String curline = reader.readLine();
                if (curline.indexOf('/') >= 0) {
                    i--;
                    continue;
                }

                //将一行分成三个数据
                String floatStrs[] = curline.split(",");

                model_NORMS[3 * i] = Float.parseFloat(floatStrs[0]);
                model_NORMS[3 * i + 1] = Float.parseFloat(floatStrs[1]);
                model_NORMS[3 * i + 2] = Float.parseFloat(floatStrs[2]);
            }

            return floatsToRead;

        } finally {
            if (inputFile != null)
                inputFile.close();
        }
    }

    private void setVerts() {
        int num = 0;
        try {
            num = loadVertsFromModel("banana/verts.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
        mVertBuff = fillBuffer(model_VERTS);
        verticesNumber = num;
        Log.d(TAG, TAG + " setVerts: num " + num);
        Log.d(TAG, TAG + " model_VERTS: " + formatArray(model_VERTS));
    }

    private void setTexCoords() {
        int num = 0;
        try {
            num = loadTexCoordsFromModel("banana/texcoords.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        mTexCoordBuff = fillBuffer(model_TEX_COORDS);
        Log.d(TAG, TAG + " setTexCoords: num " + num);
        Log.d(TAG, TAG + " model_TEX_COORDS: " + formatArray(model_TEX_COORDS));
    }

    private void setNorms() {
        int num = 0;
        try {
            num = loadNormsFromModel("banana/norms.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mNormBuff = fillBuffer(model_NORMS);
        Log.d(TAG, TAG + " setNorms: num " + num);
        Log.d(TAG, TAG + " model_NORMS: " + formatArray(model_NORMS));
    }

    public int getNumObjectIndex() {
        return 0;
    }

    private String formatArray(double[] array) {
        StringBuilder sb = new StringBuilder();
        for (double d : array) {
            sb.append(d);
            sb.append(",");
        }
        return sb.toString();
    }

    @Override
    public int getNumObjectVertex() {
        return verticesNumber;
    }

    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType) {
        Buffer result = null;
        switch (bufferType) {
            case BUFFER_TYPE_VERTEX:
                result = mVertBuff;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = mTexCoordBuff;
                break;
            case BUFFER_TYPE_NORMALS:
                result = mNormBuff;
                break;
            default:
                break;

        }

        return result;
    }

}