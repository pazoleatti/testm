package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.Relation;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.SourcesResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Контроллер для получения источников и приемников формы
 */

@RestController
public class DeclarationSourcesRestController {

    @Autowired
    SourceService sourceService;

    @Autowired
    DeclarationDataService declarationDataService;

    @Autowired
    SecurityService securityService;

    @Autowired
    DeclarationDataScriptingService scriptingService;

    @RequestMapping(value = "/rest/sources/{declarationId}", method = RequestMethod.GET)
    public List<Relation> getDeclarationSourcesAndDestinations(@PathVariable Long declarationId) {
        if (declarationDataService.existDeclarationData(declarationId)) {
            TAUserInfo userInfo = securityService.currentUserInfo();
            Logger logger = new Logger();
            DeclarationData declaration = declarationDataService.get(declarationId, userInfo);

            List<Relation> relationList = new ArrayList<Relation>();
            relationList.addAll(sourceService.getDeclarationSourcesInfo(declaration, true, false, null, userInfo, logger));
            relationList.addAll(sourceService.getDeclarationDestinationsInfo(declaration, true, false, null, userInfo, logger));
            return relationList;
        } else {
            return Collections.emptyList();
        }
    }
}
