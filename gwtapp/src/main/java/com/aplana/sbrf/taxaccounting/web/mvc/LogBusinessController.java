package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.HistoryBusinessSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.model.LogBusinessModel;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер для отображения информации по налоговой форме
 */
@RestController
public class LogBusinessController {

    private LogBusinessService logBusinessService;

    private TAUserService taUserService;

    public LogBusinessController(LogBusinessService logBusinessService, TAUserService taUserService) {
        this.logBusinessService = logBusinessService;
        this.taUserService = taUserService;
    }

    /**
     * Возвращает список измений по налоговой форме
     *
     * @param declarationId идентификатор формы
     */
    @GetMapping(value = "/rest/logBusines/{declarationId}")
    public List<LogBusinessModel> getMessagesLog(@PathVariable long declarationId) {
        ArrayList<LogBusinessModel> resultListBusines = new ArrayList<LogBusinessModel>();
        for (LogBusiness logBusiness : logBusinessService.getDeclarationLogsBusiness(declarationId, HistoryBusinessSearchOrdering.DATE, true)) {
            LogBusinessModel logBusinessModel = new LogBusinessModel(logBusiness, (FormDataEvent.getByCode(logBusiness.getEventId())).getTitle(),
                    taUserService.getUser(logBusiness.getUserLogin()).getName());
            resultListBusines.add(logBusinessModel);
        }
        return resultListBusines;
    }
}