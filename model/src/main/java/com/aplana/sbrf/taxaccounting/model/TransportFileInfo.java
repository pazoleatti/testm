package com.aplana.sbrf.taxaccounting.model;

/**
 * Данные тарнспортного файла
 */
public class TransportFileInfo {
    private String name;
    private String path;
    private long length;

    public TransportFileInfo(String name, String path, long length) {
        this.name = name;
        this.path = path;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getLength() {
        return length;
    }
}
