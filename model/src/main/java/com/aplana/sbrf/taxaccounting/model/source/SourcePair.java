package com.aplana.sbrf.taxaccounting.model.source;

import java.io.Serializable;

/**
 * Пара источник-приемник
 * @author Denis Loshkarev
 */
public class SourcePair implements Serializable {
    private static final long serialVersionUID = 971238291822549454L;

    /** Идентификатор назначения-источника*/
    private Long source;
    /** Идентификатор назначения-приемника*/
    private Long destination;

    /**
     * Вспомогательные поля. Нужны для отображения информации по паре источников-приемников без дополнительных запросов
     */

    /** Тип назначения-источника */
    private String sourceType = null;
    /** Тип назначения-приемника */
    private String destinationType = null;
    /** Вид назначения-источника */
    private String sourceKind = null;
    /** Вид назначения-приемника */
    private String destinationKind = null;
    /** Название подразделения-источника */
    private String sourceDepartmentName;
    /** Название подразделения-приемника */
    private String destinationDepartmentName;


    public SourcePair() {}

    public SourcePair(Long source, Long destination) {
        this.source = source;
        this.destination = destination;
    }

    public Long getSource() {
        return source;
    }

    public void setSource(Long source) {
        this.source = source;
    }

    public Long getDestination() {
        return destination;
    }

    public void setDestination(Long destination) {
        this.destination = destination;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public String getSourceKind() {
        return sourceKind;
    }

    public void setSourceKind(String sourceKind) {
        this.sourceKind = sourceKind;
    }

    public String getDestinationKind() {
        return destinationKind;
    }

    public void setDestinationKind(String destinationKind) {
        this.destinationKind = destinationKind;
    }

    public String getSourceDepartmentName() {
        return sourceDepartmentName;
    }

    public void setSourceDepartmentName(String sourceDepartmentName) {
        this.sourceDepartmentName = sourceDepartmentName;
    }

    public String getDestinationDepartmentName() {
        return destinationDepartmentName;
    }

    public void setDestinationDepartmentName(String destinationDepartmentName) {
        this.destinationDepartmentName = destinationDepartmentName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourcePair that = (SourcePair) o;

        if (destination != null ? !destination.equals(that.destination) : that.destination != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + (destination != null ? destination.hashCode() : 0);
        return result;
    }
}
