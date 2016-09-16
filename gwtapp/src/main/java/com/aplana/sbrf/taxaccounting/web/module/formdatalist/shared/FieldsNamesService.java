package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;


import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.util.HashMap;
import java.util.Map;

public class FieldsNamesService {
    public static Map<FormDataElementName, String> get(TaxType taxType) {
        Map<FormDataElementName, String> names = new HashMap<FormDataElementName, String>();
        if (taxType.isTax()) {
            names.put(FormDataElementName.HEADER,  "Список налоговых форм");
            names.put(FormDataElementName.FORM_KIND, "Тип налоговой формы");
            names.put(FormDataElementName.FORM_TYPE, "Вид налоговой формы");
            names.put(FormDataElementName.FORM_KIND_REFBOOK, "Выбор типа налоговой формы");
            names.put(FormDataElementName.FORM_TYPE_REFBOOK, "Выбор вида налоговой формы");
            names.put(FormDataElementName.REPORT_PERIOD, "Период");
        } else {
            names.put(FormDataElementName.HEADER, "Список форм");
            names.put(FormDataElementName.FORM_KIND, "Тип формы");
            names.put(FormDataElementName.FORM_TYPE, "Вид формы");
            names.put(FormDataElementName.FORM_KIND_REFBOOK, "Выбор типа формы");
            names.put(FormDataElementName.FORM_TYPE_REFBOOK, "Выбор вида формы");
            names.put(FormDataElementName.REPORT_PERIOD, "Период");
        }

        return names;
    }
}
