package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.dto.LogBusinessDTO;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для работы с историей изменений
 */
@RestController
public class LogBusinessController {

    @Autowired
    private LogBusinessService logBusinessService;

    /**
     * Привязка данных из параметров запроса
     *
     * @param binder спец. DataBinder для привязки
     */
    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
    }

    /**
     * Возвращает историю измений формы
     *
     * @param declarationId идентификатор формы
     * @param pagingParams  параметры пагинации
     * @return список изменений формы {@link LogBusiness}
     */
    @GetMapping(value = "/rest/logBusiness/{declarationId}", params = "projection=declarationBusinessLogs")
    public JqgridPagedList<LogBusinessDTO> findAllByDeclarationId(@PathVariable long declarationId, @RequestParam PagingParams pagingParams) {
        List<LogBusinessDTO> logBusinessList = logBusinessService.findAllByDeclarationId(declarationId, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                logBusinessList,
                logBusinessList.size(),
                pagingParams);
    }

    /**
     * Возвращает историю измений ФЛ
     *
     * @param personId     идентификатор ФЛ
     * @param pagingParams параметры пагинации
     * @return список изменений декларации {@link LogBusiness}
     */
    @GetMapping(value = "/rest/logBusiness/{personId}", params = "projection=personBusinessLogs")
    public JqgridPagedList<LogBusinessDTO> findAllByPersonId(@PathVariable long personId, @RequestParam PagingParams pagingParams) {
        PagingResult<LogBusinessDTO> logBusinessPage = logBusinessService.findAllByPersonId(personId, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                logBusinessPage,
                logBusinessPage.getTotalCount(),
                pagingParams);
    }
}
