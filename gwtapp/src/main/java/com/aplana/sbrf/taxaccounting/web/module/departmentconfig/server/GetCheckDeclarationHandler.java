package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetCheckDeclarationAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetCheckDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Lenar Haziev
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetCheckDeclarationHandler extends AbstractActionHandler<GetCheckDeclarationAction, GetCheckDeclarationResult> {

    private static final String WARN_MSG = "\"%s\" %s, \"%s\"%s, состояние - \"%s\"";
    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Autowired
    private TAUserService userService;
    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;
    @Autowired
    private PeriodService reportService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private RefBookFactory rbFactory;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    public GetCheckDeclarationHandler() {
        super(GetCheckDeclarationAction.class);
    }

    @Override
    public GetCheckDeclarationResult execute(GetCheckDeclarationAction action, ExecutionContext executionContext) throws ActionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void undo(GetCheckDeclarationAction action, GetCheckDeclarationResult result, ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}