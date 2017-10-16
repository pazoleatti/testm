package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.LockDataItem;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Контроллер для работы с блокировками
 *
 * @author dloshkarev
 */
@RestController
public class LockDataController {

    @Autowired
    private LockDataService lockDataService;

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
    public JqgridPagedList<LockDataItem> fetchLocks(@RequestParam(required = false) String filter, @RequestParam PagingParams pagingParams) {
        PagingResult<LockDataItem> locks = lockDataService.getLocks(filter, pagingParams);
        return JqgridPagedResourceAssembler.buildPagedList(
                locks,
                locks.getTotalCount(),
                pagingParams
        );
    }

    /**
     * Удаление блокировок
     *
     * @param ids идентификаторы блокировок
     */
    @PostMapping(value = "/actions/lock/delete")
    public void deleteLocks(@RequestParam Long[] ids) {
        lockDataService.unlockAll(Arrays.asList(ids));
    }
}
