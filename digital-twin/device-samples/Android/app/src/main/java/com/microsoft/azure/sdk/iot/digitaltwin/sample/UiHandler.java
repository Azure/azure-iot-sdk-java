package com.microsoft.azure.sdk.iot.digitaltwin.sample;

interface UiHandler {
    void updateName(String name);
    void updateBrightness(double brightness);
    void updateTemperature(double temperature);
    void updateHumidity(double humidity);
    void updateOnoff(boolean on);
    void startBlink(long interval);
}
