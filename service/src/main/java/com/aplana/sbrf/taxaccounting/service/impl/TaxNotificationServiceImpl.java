package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAsnuService;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Service
public class TaxNotificationServiceImpl implements TaxNotificationService {

    private final DeclarationDataService declarationDataService;
    private final DepartmentService departmentService;
    private final PeriodService periodService;
    private final RefBookAsnuService asnuService;
    private final LogEntryService logEntryService;

    public TaxNotificationServiceImpl(DeclarationDataService declarationDataService,
                                      DepartmentService departmentService,
                                      PeriodService periodService,
                                      RefBookAsnuService asnuService,
                                      LogEntryService logEntryService) {
        this.declarationDataService = declarationDataService;
        this.departmentService = departmentService;
        this.periodService = periodService;
        this.asnuService = asnuService;
        this.logEntryService = logEntryService;
    }


    @Override
    public String createAsync(Integer departmentId, Integer periodId, List<Long> asnuIds) {

        Department department = departmentService.getDepartment(departmentId);
        ReportPeriod period = periodService.fetchReportPeriod(periodId);

        // Поиск КНФ по параметрам
        List<DeclarationData> declarations = declarationDataService.findAllDeclarationData(DeclarationType.NDFL_CONSOLIDATE, departmentId, periodId);
        DeclarationData declaration = findAcceptedDeclarationForNonholdingTax(declarations);

        String asnuString = "";
        // Если есть АСНУ
        if (declaration != null && isNotEmpty(asnuIds)) {
            // проверяем, есть ли АСНУ декларации в списке
            Long declarationAsnuId = declaration.getAsnuId();
            if (!asnuIds.contains(declarationAsnuId)) {
                declaration = null;
            } else {
                // Если есть, заполняем список кодов АСНУ для логов
                List<RefBookAsnu> asnuList = asnuService.fetchByIds(asnuIds);
                Collection<String> asnuCodes = Collections2.transform(asnuList, new Function<RefBookAsnu, String>() {
                    @Override
                    public String apply(RefBookAsnu asnu) {
                        return asnu.getCode();
                    }
                });
                asnuString = " по АСНУ: " + StringUtils.join(asnuCodes, ", ");
            }
        }

        Logger logger = new Logger();
        if (declaration == null) {
            String errorMessage = "За указанный период %d: %s по территориальному банку %s отсутствует КНФ, " +
                    "необходимая для формирования Уведомлений о неудержанном налоге. " +
                    "Необходимо сформировать КНФ и повторить выполнение операции.";
            logger.error(errorMessage, period.getTaxPeriod().getYear(), period.getName(), department.getShortName());
        } else {
            String message = "Поставлена в очередь на исполнение операция " +
                    "\"Формирование Уведомлений о неудержанном налоге для территориального банка %s за период %d: %s%s по КНФ %d\"";
            logger.info(message, department.getShortName(), period.getTaxPeriod().getYear(), period.getName(), asnuString, declaration.getId());
        }

        String taskLogsUuid = logEntryService.save(logger.getEntries());
        return taskLogsUuid;
    }

    private DeclarationData findAcceptedDeclarationForNonholdingTax(Collection<DeclarationData> declarations) {
        if (isEmpty(declarations)) return null;

        for (DeclarationData declaration : declarations) {
            if (declaration.getKnfType().equals(RefBookKnfType.BY_NONHOLDING_TAX) && declaration.getState().equals(State.ACCEPTED)) {
                return declaration;
            }
        }
        return null;
    }
}
