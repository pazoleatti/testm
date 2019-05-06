package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.SubsystemService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для справочника "Подсистемы АС УН".
 */
@RestController
public class SubsystemController {

    @Autowired
    private SubsystemService subsystemService;

    @GetMapping("rest/subsystems")
    public JqgridPagedList<Subsystem> getSubsystems(@RequestParam(required = false) String name) {
        PagingResult<Subsystem> subsystems = subsystemService.findByName(name);
        return JqgridPagedResourceAssembler.buildPagedList(subsystems, null);
    }
}
