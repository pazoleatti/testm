package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * Контроллер для работы с блокировками
 */
@RestController
public class LockDataController {

    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private SecurityService securityService;

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
     * @param pagingParams параметры пагинации
     * @return список {@link JqgridPagedList} блокировок {@link LockData}
     */
    @GetMapping(value = "/rest/locks")
    public JqgridPagedList<LockDataDTO> fetchLocks(@RequestParam(required = false) String filter, @RequestParam PagingParams pagingParams) {
        PagingResult<LockDataDTO> locks = lockDataService.getLocks(filter, pagingParams, securityService.currentUserInfo().getUser());
        return JqgridPagedResourceAssembler.buildPagedList(
                locks,
                locks.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Удаление блокировок
     *
     * @param keys ключи блокировок
     */
    @PostMapping(value = "/actions/lock/delete")
    public void deleteLocks(@RequestParam String[] keys) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        lockDataService.unlockAllWithCheckingTasks(userInfo, Arrays.asList(keys));
    }
}
