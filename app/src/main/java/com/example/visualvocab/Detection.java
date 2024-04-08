package com.example.visualvocab;
import android.graphics.RectF;

public class Detection {
    private String label;
    private RectF boundingBox;
    private float confidence;

    public Detection(String label, RectF boundingBox, float confidence) {
        this.label = label;
        this.boundingBox = boundingBox;
        this.confidence = confidence;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public RectF getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(RectF boundingBox) {
        this.boundingBox = boundingBox;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "Detection{" +
                "label='" + label + '\'' +
                ", boundingBox=" + boundingBox +
                ", confidence=" + confidence +
                '}';
    }
}
