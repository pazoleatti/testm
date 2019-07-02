package com.aplana.sbrf.taxaccounting.model.action;

import com.aplana.sbrf.taxaccounting.model.KppOktmoPair;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Параметры для создания формы 2-НДФЛ (ФЛ) из GUI
 */
@Getter
@Setter
public class Create2NdflFLAction {
    private long personId;
    private int declarationTypeId;
    private int departmentId;
    private int reportPeriodId;
    private List<KppOktmoPair> kppOktmoPairs;
    private String signatory;
}
