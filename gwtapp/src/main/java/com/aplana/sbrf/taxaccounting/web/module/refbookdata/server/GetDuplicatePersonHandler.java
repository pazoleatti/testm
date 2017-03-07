package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerUtils;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetDuplicatePersonHandler extends AbstractActionHandler<GetDuplicatePersonAction, GetDuplicatePersonResult> {

    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private RefBookHelper refBookHelper;
    @Autowired
    private PersonService personService;

    public GetDuplicatePersonHandler() {
        super(GetDuplicatePersonAction.class);
    }

    @Override
    public GetDuplicatePersonResult execute(GetDuplicatePersonAction action, ExecutionContext executionContext)
            throws ActionException {
        Map<RefBookAttribute, Column> columnMap = new HashMap<RefBookAttribute, Column>();
        GetDuplicatePersonResult result = new GetDuplicatePersonResult();

        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(RefBook.Id.PERSON.getId());
        RefBook refBook = refBookFactory.get(RefBook.Id.PERSON.getId());
        result.setTableHeaders(refBook.getAttributes());

        Map<String, RefBookValue> rowRecord = refBookDataProvider.getRecordData(action.getRecord().getRefBookRowId());
        Long recordId = rowRecord.get("RECORD_ID").getNumberValue().longValue();
        Long oldId = null;
        if (rowRecord.get("OLD_ID") != null && rowRecord.get("OLD_ID").getNumberValue() != null) {
            oldId = rowRecord.get("OLD_ID").getNumberValue().longValue();
        }

        // Получаем исходную запись
        List<RefBookDataRow> rows = new ArrayList<RefBookDataRow>();
        GetRefBookDataRowHandler.dereference(refBook, Arrays.asList(rowRecord), rows, refBookHelper, columnMap);
        result.setDataRow(rows.get(0));

        // Получаем оригиал, если запись не является оригиналом
        if (oldId != null) {
            rows = new ArrayList<RefBookDataRow>();
            Long originalId = personService.getOriginal(recordId);
            Map<String, RefBookValue> originalRecord = refBookDataProvider.getRecordData(originalId);
            GetRefBookDataRowHandler.dereference(refBook, Arrays.asList(originalRecord), rows, refBookHelper, columnMap);
            result.setOriginalRow(rows.get(0));
        }

        // Получаем дубликаты
        PagingResult<Map<String, RefBookValue>> refBookPage = new PagingResult<Map<String, RefBookValue>>();
        List<Long> duplicateIds = personService.getDuplicate(recordId);
        duplicateIds.remove(action.getRecord().getRefBookRowId()); // Исключаем выбранную запись из дубликатов
        if (!duplicateIds.isEmpty()) {
            Map<Long, Map<String, RefBookValue>> recordData = refBookDataProvider.getRecordData(duplicateIds);
            refBookPage.addAll(recordData.values());
        }
        rows = new ArrayList<RefBookDataRow>();
        GetRefBookDataRowHandler.dereference(refBook, refBookPage, rows, refBookHelper, columnMap);
        result.setDuplicateRows(rows);
        return result;
    }


    @Override
    public void undo(GetDuplicatePersonAction action, GetDuplicatePersonResult result,
                     ExecutionContext executionContext) throws ActionException {
    }
}
