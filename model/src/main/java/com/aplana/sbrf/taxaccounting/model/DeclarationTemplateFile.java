package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Модельный класс для файлов версии макета
 * @author lhaziev
 */
public class DeclarationTemplateFile implements Serializable{
	private static final long serialVersionUID = 1L;

    private String fileName;
    private String blobDataId;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getBlobDataId() {
        return blobDataId;
    }

    public void setBlobDataId(String blobDataId) {
        this.blobDataId = blobDataId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeclarationTemplateFile that = (DeclarationTemplateFile) o;

        if (!fileName.equals(that.fileName)) return false;
        return blobDataId.equals(that.blobDataId);
    }

    @Override
    public int hashCode() {
        int result = fileName.hashCode();
        result = 31 * result + blobDataId.hashCode();
        return result;
    }
}
