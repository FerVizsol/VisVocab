package com.example.visualvocab;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
public class ObjectDetector {
    private Interpreter interpreter;

    public ObjectDetector(AssetManager assetManager, String modelFilename) {
        try {
            interpreter = new Interpreter(loadModelFile(assetManager, modelFilename));
        } catch (IOException e) {
            Log.d("Error IO",e.toString());
        }
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelFilename) throws IOException {
        InputStream inputStream = assetManager.open(modelFilename);
        long declaredLength = inputStream.available();
        ByteBuffer buffer = ByteBuffer.allocateDirect((int) declaredLength);
        byte[] data = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(data)) != -1) {
            buffer.put(data, 0, bytesRead);
        }
        inputStream.close();
        buffer.rewind();
        return (MappedByteBuffer) buffer;
    }


    public List<Detection> detectObjects(Bitmap inputImage) {

        ByteBuffer inputBuffer = getInputBuffer(inputImage);
        ByteBuffer outputBuffer = ByteBuffer.allocateDirect(1431600);
        interpreter.run(inputBuffer, outputBuffer);
        interpreter.getOutputTensor(0);

        List<Detection> detections = postProcess(outputBuffer);
        return detections;
    }

    private ByteBuffer getInputBuffer(Bitmap inputImage) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        int channelCount = 3;
        int bufferSize = width * height * channelCount * Float.SIZE / Byte.SIZE;
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(bufferSize);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = inputImage.getPixel(x, y);

                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                float normalizedRed = (red / 255.0f);
                float normalizedGreen = (green / 255.0f);
                float normalizedBlue = (blue / 255.0f);

                inputBuffer.putFloat(normalizedBlue);
                inputBuffer.putFloat(normalizedGreen);
                inputBuffer.putFloat(normalizedRed);
            }
        }

        inputBuffer.rewind();

        return inputBuffer;
    }

    private static final int NUM_CLASSES = 10;
    private static final int NUM_COORDS = 4;
    private List<Detection> postProcess(ByteBuffer outputBuffer) {
        List<Detection> detections = new ArrayList<>();
        int numDetections = outputBuffer.remaining() / ((NUM_CLASSES + NUM_COORDS + 1) * Float.SIZE / Byte.SIZE);
        for (int i = 0; i < numDetections; i++) {
            int labelStartIndex = i * ((NUM_CLASSES + NUM_COORDS + 1) * Float.SIZE / Byte.SIZE);
            int labelIndex = labelStartIndex;
            String label = String.valueOf(outputBuffer.getFloat(labelIndex));
            labelIndex++;

            RectF boundingBox = new RectF(
                    outputBuffer.getFloat(labelIndex),
                    outputBuffer.getFloat(labelIndex + 1),
                    outputBuffer.getFloat(labelIndex + 2),
                    outputBuffer.getFloat(labelIndex + 3)
            );
            labelIndex += NUM_COORDS;

            float confidence = outputBuffer.getFloat(labelIndex);
            detections.add(new Detection(label, boundingBox, confidence));
        }

        return detections;
    }

}