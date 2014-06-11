package com.aplana.sbrf.taxaccounting.model.source;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;

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
    private FormType sourceType = null;
    /** Тип формы-приемника */
    private FormType destinationFormType = null;
    /** Тип декларации-источника */
    private FormType destinationDeclarationType = null;
    /** Вид назначения-источника */
    private FormDataKind sourceKind = null;
    /** Вид назначения-приемника */
    private FormDataKind destinationKind = null;

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

    public FormType getSourceType() {
        return sourceType;
    }

    public void setSourceType(FormType sourceType) {
        this.sourceType = sourceType;
    }

    public FormType getDestinationFormType() {
        return destinationFormType;
    }

    public void setDestinationFormType(FormType destinationFormType) {
        this.destinationFormType = destinationFormType;
    }

    public FormType getDestinationDeclarationType() {
        return destinationDeclarationType;
    }

    public void setDestinationDeclarationType(FormType destinationDeclarationType) {
        this.destinationDeclarationType = destinationDeclarationType;
    }

    public FormDataKind getSourceKind() {
        return sourceKind;
    }

    public void setSourceKind(FormDataKind sourceKind) {
        this.sourceKind = sourceKind;
    }

    public FormDataKind getDestinationKind() {
        return destinationKind;
    }

    public void setDestinationKind(FormDataKind destinationKind) {
        this.destinationKind = destinationKind;
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
