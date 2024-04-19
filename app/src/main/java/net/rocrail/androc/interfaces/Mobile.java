package net.rocrail.androc.interfaces;

import android.graphics.Bitmap;

import net.rocrail.androc.widgets.LocoImage;

import org.xml.sax.Attributes;

public interface Mobile extends ItemBase {
    String getID();

    Bitmap getFunctionIcon(int fn);

    String getFunctionText(int fn);

    boolean isFunction(int fn);

    boolean isAutoStart();

    void setAutoStart(boolean autostart);

    boolean isLights();

    int getAddr();

    int getSpeed();

    int getVMax();

    int getSteps();

    boolean isDir();

    boolean isShow();

    void updateFunctions(Attributes atts);

    void flipLights();

    void flipFunction(int fn);

    void flipGo();

    void flipDir();

    void doRelease();

    String getConsist();

    Bitmap getBmp(LocoImage image);

    String getDescription();

    void setSpeed(int V, boolean force);

    void Dispatch();

    void addFunction(Attributes atts);

    long getRunTime();

    String getRoadname();

    boolean isHalfAuto();

    void setHalfAuto(boolean halfauto);

    boolean isPlacing();

    void setPlacing(boolean placing);

    void swap();

    LocoImage getImageView();

    void setPicData(String filename, String data, int nr);
}
