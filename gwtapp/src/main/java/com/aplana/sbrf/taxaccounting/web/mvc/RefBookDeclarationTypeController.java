package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookDeclarationTypeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для работы с видами налоговых форм
 */
@RestController
public class RefBookDeclarationTypeController {
    private static final Log LOG = LogFactory.getLog(RefBookDeclarationTypeController.class);

    private RefBookDeclarationTypeService refBookDeclarationTypeService;

    public RefBookDeclarationTypeController(RefBookDeclarationTypeService refBookDeclarationTypeService) {
        this.refBookDeclarationTypeService = refBookDeclarationTypeService;
    }

    /**
     * Получение значений справочника на основе типа формы, подразделения и начала отчетного периода. Выполняется поиск
     * назначенных подразделению видов форм с действующей на момент начала периода версией шаблона формы указанного типа.
     * Т.е. видов форм, назначенных заданному подразделению, имеющих статус версии "действующий" и для которых есть шаблон
     * формы с заданным типом формы, "действующим" статусом версии и версией не более поздней, чем заданное начало
     * отчетного периода
     *
     * @param declarationKind Тип налоговой формы
     * @param departmentId    ID Подразделения
     * @param periodId        ID отчетного периода
     * @return Значения справочника
     */
    @GetMapping(value = "/rest/refBook/207/activeAndAssigned")
    public List<RefBookDeclarationType> fetchDeclarationTypes(Long declarationKind, Integer departmentId, Integer periodId) {
        return refBookDeclarationTypeService.fetchDeclarationTypes(declarationKind, departmentId, periodId);
    }

}
