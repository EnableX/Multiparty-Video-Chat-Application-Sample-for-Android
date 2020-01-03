package com.enablex.multiconferencequickapp.model;

import enx_rtc_android.Controller.EnxPlayerView;
import enx_rtc_android.Controller.EnxStream;

public class HorizontalViewModel {
    private EnxStream enxStream;
    private boolean isAudioOnly;
    private String mediaType;
    private EnxPlayerView enxPlayerView;

    public EnxStream getEnxStream() {
        return enxStream;
    }

    public void setEnxStream(EnxStream enxStream) {
        this.enxStream = enxStream;
    }

    public EnxPlayerView getEnxPlayerView() {
        return enxPlayerView;
    }

    public void setEnxPlayerView(EnxPlayerView enxPlayerView) {
        this.enxPlayerView = enxPlayerView;
    }
}
