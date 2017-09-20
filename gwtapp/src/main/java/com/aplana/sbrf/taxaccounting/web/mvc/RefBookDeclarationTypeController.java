package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDeclarationTypeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для работы с видами налоговых форм
 */
@RestController
public class RefBookDeclarationTypeController {
    private static final Log LOG = LogFactory.getLog(RefBookDeclarationTypeController.class);

    @Autowired
    private RefBookDeclarationTypeService refBookDeclarationTypeService;

    /**
     * Получить список справочника Виды форм для создания новой налоговой формы
     *
     * @param declarationKind Вид налоговой формы
     * @param departmentId    ID Подразделения
     * @param periodId        ID отчетного периода
     * @return Значения справочника
     */
    @GetMapping(value = "/rest/refBook/207/declarationTypesForCreate")
    public List<RefBookDeclarationType> fetchDeclarationTypes(Long declarationKind, Integer departmentId, Integer periodId) {
        LOG.info("Fetch records for refbook DECLARATION_TYPE for declaration create");
        return refBookDeclarationTypeService.fetchDeclarationTypesForCreate(declarationKind, departmentId, periodId);
    }

}
