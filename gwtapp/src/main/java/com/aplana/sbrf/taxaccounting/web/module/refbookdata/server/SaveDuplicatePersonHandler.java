package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@PreAuthorize("isAuthenticated()")
@Transactional
public class SaveDuplicatePersonHandler extends AbstractActionHandler<SaveDuplicatePersonAction, SaveDuplicatePersonResult> {

    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private PersonService personService;
    @Autowired
    private LogEntryService logEntryService;

    public SaveDuplicatePersonHandler() {
        super(SaveDuplicatePersonAction.class);
    }

    @Override
    public SaveDuplicatePersonResult execute(SaveDuplicatePersonAction action, ExecutionContext executionContext)
            throws ActionException {
        Map<RefBookAttribute, Column> columnMap = new HashMap<RefBookAttribute, Column>();
        SaveDuplicatePersonResult result = new SaveDuplicatePersonResult();
        Logger logger = new Logger();
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(RefBook.Id.PERSON.getId());
        RefBook refBook = refBookFactory.get(RefBook.Id.PERSON.getId());

        List<RefBookDataRow> duplicateRecords = action.getDuplicateRecords();
        Map<String, RefBookValue> originalMap;
        if (action.getOriginalRecord() != null) {
            originalMap = refBookDataProvider.getRecordData(action.getOriginalRecord().getRefBookRowId());
            Long originalOldId = null;
            if (originalMap.get("OLD_ID") != null && originalMap.get("OLD_ID").getNumberValue() != null) {
                originalOldId = originalMap.get("OLD_ID").getNumberValue().longValue();
            }
            // Проверка оригинала
            if (originalOldId != null) {
                throw new ActionException(String.format("Выбранная оригиналом запись являеется дубликатом записи с \"%s\" = %d", refBook.getAttribute("RECORD_ID").getName(), originalMap.get("RECORD_ID").getNumberValue().longValue()));
            }

            duplicateRecords.add(action.getRecord()); // исходная запись является дубликатом
        } else {
            originalMap = refBookDataProvider.getRecordData(action.getRecord().getRefBookRowId());
            Long originalOldId = null;
            if (originalMap.get("OLD_ID") != null && originalMap.get("OLD_ID").getNumberValue() != null) {
                originalOldId = originalMap.get("OLD_ID").getNumberValue().longValue();
            }
            if (originalOldId != null) {
                personService.setOriginal(Arrays.asList(originalOldId));
            }
            originalMap = refBookDataProvider.getRecordData(action.getRecord().getRefBookRowId());  // исходная запись становиться оригиналом
        }
        Long originalRecordId = originalMap.get("RECORD_ID").getNumberValue().longValue();

        for(RefBookDataRow refBookDataRow: action.getDuplicateRecords()) {
            Map<String, RefBookValue> duplicateRecord = refBookDataProvider.getRecordData(refBookDataRow.getRefBookRowId());
            Long duplicateRecordId = duplicateRecord.get("RECORD_ID").getNumberValue().longValue();
            Long duplicateOldId = null;
            if (duplicateRecord.get("OLD_ID") != null && duplicateRecord.get("OLD_ID").getNumberValue() != null) {
                duplicateOldId = duplicateRecord.get("OLD_ID").getNumberValue().longValue();
            }
            if (duplicateOldId == null) {
                // оригинал становиться дубликатом
                // нужно проверить дубликаты данной записи
                personService.setDuplicate(Arrays.asList(duplicateRecordId), originalRecordId);
            } else if (duplicateRecordId == originalRecordId) {
                // уже назначен дубликатом
            } else {
                // версия назначена дубликатом на другую запись???
                if (action.getOriginalRecord() == null) {
                    Long oldRecordId = Long.parseLong(action.getRecord().getValues().get("RECORD_ID"));
                    if (oldRecordId == duplicateRecordId) {
                        personService.changeRecordId(Arrays.asList(duplicateOldId), originalRecordId);
                    }
                }
            }
        }

        for(RefBookDataRow refBookDataRow: action.getDeleteDuplicateRecords()) {
            Map<String, RefBookValue> duplicateRecord = refBookDataProvider.getRecordData(refBookDataRow.getRefBookRowId());
            Long duplicateRecordId = duplicateRecord.get("RECORD_ID").getNumberValue().longValue();
            Long duplicateOldId = null;
            if (duplicateRecord.get("OLD_ID") != null && duplicateRecord.get("OLD_ID").getNumberValue() != null) {
                duplicateOldId = duplicateRecord.get("OLD_ID").getNumberValue().longValue();
            }
            if (duplicateOldId == null) {
                // уже является оригиналом
            } else if (duplicateRecordId == originalRecordId) {
                // является дубликатом данной записи
                personService.setOriginal(Arrays.asList(duplicateOldId));
            } else {
                // является дубликатом другой записи
            }
        }
        logger.info("Изменения успешно сохранены");
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }


    @Override
    public void undo(SaveDuplicatePersonAction action, SaveDuplicatePersonResult result,
                     ExecutionContext executionContext) throws ActionException {
    }
}
