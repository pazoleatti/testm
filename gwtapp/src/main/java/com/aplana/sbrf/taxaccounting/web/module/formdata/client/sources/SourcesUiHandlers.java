package com.aplana.sbrf.taxaccounting.web.module.formdata.client.sources;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * @author auldanov
 */
public interface SourcesUiHandlers extends UiHandlers{
    TaxType getTaxType();
}
