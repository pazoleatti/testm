package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDepartmentCombinedAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDepartmentCombinedResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Получение параметров подразделения и списка доступных налоговых периодов
 *
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
public class GetDepartmentCombinedHandler extends AbstractActionHandler<GetDepartmentCombinedAction,
        GetDepartmentCombinedResult> {

    private static final Log LOG = LogFactory.getLog(GetDepartmentCombinedHandler.class);

    @Autowired
    private PeriodService periodService;

    @Autowired
    private CommonRefBookService commonRefBookService;

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private RefBookHelper refBookHelper;

    public GetDepartmentCombinedHandler() {
        super(GetDepartmentCombinedAction.class);
    }

    @Override
    public GetDepartmentCombinedResult execute(GetDepartmentCombinedAction action, ExecutionContext executionContext)
            throws ActionException {

        throw new UnsupportedOperationException();
    }

    /**
     * Проверка существования записей справочника на которые ссылаются атрибуты.
     * Считаем что все справочные атрибуты хранятся в универсальной структуре как и сами настройки
     * @param refBook
     * @param rows
     */
    private void checkReferenceValues(RefBookDataProvider provider, RefBook refBook, Map<String, RefBookValue> rows, Date versionFrom, Date versionTo, Logger logger) {
        Map<RefBookDataProvider, List<RefBookLinkModel>> references = new HashMap<RefBookDataProvider, List<RefBookLinkModel>>();

        RefBookDataProvider oktmoProvider = refBookFactory.getDataProvider(RefBook.Id.OKTMO.getId());
        for (Map.Entry<String, RefBookValue> e : rows.entrySet()) {
            if (e.getValue().getAttributeType() == RefBookAttributeType.REFERENCE
                    && !e.getKey().equals("DEPARTMENT_ID")) { //Подразделения не версионируются и их нет смысла проверять
                Long link = e.getValue().getReferenceValue();
                if (link != null) {
                    //Собираем ссылки на справочники и группируем их по провайдеру, обрабатывающему справочники
                    RefBookDataProvider linkProvider = e.getKey().equals("OKTMO") ? oktmoProvider : provider;
                    if (!references.containsKey(linkProvider)) {
                        references.put(linkProvider, new ArrayList<RefBookLinkModel>());
                    }
                    //Сохраняем данные для отображения сообщений
                    references.get(linkProvider).add(new RefBookLinkModel(null, e.getKey(), link, null, versionFrom, versionTo));
                }
            }
        }

        //Проверяем ссылки и выводим соообщения если надо
        refBookHelper.checkReferenceValues(refBook, references, RefBookHelper.CHECK_REFERENCES_MODE.DEPARTMENT_CONFIG, logger);
    }

    /**
     * Разыменование значения справочника с обработкой исключения, возникающего при отсутствии записи
     *
     * @param map             Id атрибута -> Разыменованное значение
     * @param refBookId       Id справочника
     * @param attributeId     Id атрибута
     * @param recordId        Id записи
     * @param logger          Логгер для передачи клиенту
     */
    private void getValueIgnoreEmptyResult(Map<Long, String> map, long refBookId, long attributeId, long recordId, Logger logger) {
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBookId);
        if (provider.isRecordsExist(Collections.singletonList(recordId)).isEmpty()){
            RefBookValue value = provider.getValue(recordId, attributeId);
            map.put(attributeId, getNumberValue(value));            
        }
    }

    /**
     * Разыменование числовых значений как строк и строк как строк
     */
    private String getNumberValue(RefBookValue value) {
        if (value == null) {
            return null;
        }
        if (value.getAttributeType() == RefBookAttributeType.STRING) {
            return value.getStringValue();
        }
        if (value.getNumberValue() == null) {
            return null;
        }
        return value.getNumberValue().toString();
    }

    @Override
    public void undo(GetDepartmentCombinedAction action, GetDepartmentCombinedResult result,
                     ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }

    private List<Long> getList(Long value) {
        List<Long> list = null;
        if (value != null) {
            list = new ArrayList<Long>();
            list.add(value);
        }
        return list;
    }

    private Date addDayToDate(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }
}
