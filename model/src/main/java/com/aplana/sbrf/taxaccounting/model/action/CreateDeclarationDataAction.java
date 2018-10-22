package com.aplana.sbrf.taxaccounting.model.action;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Параметры для создания формы из GUI
 */
@Getter
@Setter
@ToString
public class CreateDeclarationDataAction {

    private Long declarationTypeId;
    private Integer departmentId;
    private Integer periodId;
    private Long asnuId;
    private RefBookKnfType knfType;
    private List<String> kppList;
}
