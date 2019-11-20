package com.aplana.sbrf.taxaccounting.model.result;

import lombok.Getter;
import lombok.Setter;

/**
 * Моджельный класс для передачи данных связанных с налоговыми формами
 */
@Getter
@Setter
public class DeclarationDataResult {
    private boolean existDeclarationData = true;
    private long declarationDataId;
}
