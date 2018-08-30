package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.SaveDepartmentCombinedAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.SaveDepartmentCombinedResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
public class SaveDepartmentCombinedHandler extends AbstractActionHandler<SaveDepartmentCombinedAction,
        SaveDepartmentCombinedResult> {

    private static final String SUCCESS_INFO = "Настройки для \"%s\" в периоде с %s по %s успешно сохранены.";
    private static final ThreadLocal<SimpleDateFormat> SDF = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Autowired
    private PeriodService periodService;
    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    public SaveDepartmentCombinedHandler() {
        super(SaveDepartmentCombinedAction.class);
    }

    @Override
    public SaveDepartmentCombinedResult execute(SaveDepartmentCombinedAction action, ExecutionContext executionContext)
            throws ActionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void undo(SaveDepartmentCombinedAction action, SaveDepartmentCombinedResult result,
                     ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }

    private Long getFirstLong(List<Long> list) {
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    private boolean checkPattern(Logger logger, String name, String value, Pattern pattern, String patternMeaning) {
        if (value != null && !pattern.matcher(value).matches()){
            logger.error("Поле \"%s\" заполнено неверно (%s)! Ожидаемый паттерн: \"%s\".", name, value, pattern.pattern());
            logger.error("Расшифровка паттерна \"%s\": %s.", pattern.pattern(), patternMeaning);
            return false;
        }
        return true;
    }

    private void checkSumInn(Logger logger, String name, String value) {
        if (value != null && !RefBookUtils.checkControlSumInn(value)){
            logger.error("Вычисленное контрольное число по полю \"%s\" некорректно (%s).", name, value);
        }
    }
}