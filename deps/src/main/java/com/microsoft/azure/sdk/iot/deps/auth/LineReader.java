package com.microsoft.azure.sdk.iot.deps.auth;

class LineReader {

    private String[] lines;
    private int index;

    public LineReader(String lines) {
        this.lines = lines.split("\n");
    }

    public String readLine() {
        if (index >= lines.length) {
            return null;
        }
        return lines[index++];
    }
}
