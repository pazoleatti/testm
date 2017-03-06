package com.aplana.sbrf.taxaccounting.model.identification;

import java.util.EventListener;

/**
 * @author Andrey Drunk
 */
public interface AttributeChangeListener extends EventListener {
    void processAttr(AttributeChangeEvent event);
}
