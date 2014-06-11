package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.HasName;
import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Вспомогательный рендеред для листбоксов с моделями имплеметирующими интерфейс HasName
 *
 * @author aivanov
 * @since 23.05.2014
 */
public class ValueBoxRenderer<T extends HasName> extends AbstractRenderer<T> {
    @Override
    public String render(T object) {
        return object == null ? "" : object.getName();
    }
}
