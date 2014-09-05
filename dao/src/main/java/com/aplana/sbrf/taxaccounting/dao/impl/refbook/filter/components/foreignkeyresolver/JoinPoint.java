package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.foreignkeyresolver;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;

/**
 * Модель описывающая параметры точки соприкосновения двух справочников
 * Точка соприкосновения двух справочников возникает когда
 * один справочник ссылается на другой через ссылочный атрибут
 *
 * @author auldanov on 14.08.2014.
 */
public class JoinPoint {
    /**
     * Аттрибут который ссылается на destinationAttribute
     */
    private RefBookAttribute referenceAttribute;

    /**
     * Аттрибут на который ссылается referenceAttribute
     */
    private RefBookAttribute destinationAttribute;

    public JoinPoint(RefBookAttribute referenceAttribute, RefBookAttribute destinationAttribute) {
        this.referenceAttribute = referenceAttribute;
        this.destinationAttribute = destinationAttribute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinPoint joinPoint = (JoinPoint) o;

        if (destinationAttribute != null ? !destinationAttribute.equals(joinPoint.destinationAttribute) : joinPoint.destinationAttribute != null)
            return false;
        if (referenceAttribute != null ? !referenceAttribute.equals(joinPoint.referenceAttribute) : joinPoint.referenceAttribute != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = referenceAttribute != null ? referenceAttribute.hashCode() : 0;
        result = 31 * result + (destinationAttribute != null ? destinationAttribute.hashCode() : 0);
        return result;
    }

    public RefBookAttribute getReferenceAttribute() {
        return referenceAttribute;
    }

    public void setReferenceAttribute(RefBookAttribute referenceAttribute) {
        this.referenceAttribute = referenceAttribute;
    }

    public RefBookAttribute getDestinationAttribute() {
        return destinationAttribute;
    }

    public void setDestinationAttribute(RefBookAttribute destinationAttribute) {
        this.destinationAttribute = destinationAttribute;
    }
}
