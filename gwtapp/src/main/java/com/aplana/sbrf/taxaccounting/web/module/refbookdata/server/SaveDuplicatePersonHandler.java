package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
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
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
@Transactional
public class SaveDuplicatePersonHandler extends AbstractActionHandler<SaveDuplicatePersonAction, SaveDuplicatePersonResult> {

    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private PersonService personService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private SecurityService securityService;

    public SaveDuplicatePersonHandler() {
        super(SaveDuplicatePersonAction.class);
    }

    @Override
    @Transactional
    public SaveDuplicatePersonResult execute(SaveDuplicatePersonAction action, ExecutionContext executionContext)
            throws ActionException {
        Map<RefBookAttribute, Column> columnMap = new HashMap<RefBookAttribute, Column>();
        SaveDuplicatePersonResult result = new SaveDuplicatePersonResult();
        Logger logger = new Logger();
        logger.setTaUserInfo(securityService.currentUserInfo());
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(RefBook.Id.PERSON.getId());
        RefBookDataProvider dulDataProvider = refBookFactory.getDataProvider(RefBook.Id.ID_DOC.getId());
        RefBook refBook = refBookFactory.get(RefBook.Id.PERSON.getId());

        //TODO: (dloshkarev) ниже начинается какой то ужас, надо это переписать

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
        List<Long> originalUniqueRecordIds = refBookDataProvider.getUniqueRecordIds(null, "RECORD_ID = "+originalRecordId);

        //набор ДУЛов оригинала
        Map<Long, Map<String, RefBookValue>> originalDulList = dulDataProvider.getRecordDataWhere("PERSON_ID = "+originalMap.get(RefBook.RECORD_ID_ALIAS).getNumberValue());

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

                //набор ДУЛов дубликата
                Map<Long, Map<String, RefBookValue>> duplicateDulList = dulDataProvider.getRecordDataWhere("PERSON_ID = "+duplicateRecord.get(RefBook.RECORD_ID_ALIAS).getNumberValue());
                List<RefBookRecord> newDulList = new ArrayList<RefBookRecord>();
                for(Map.Entry<Long, Map<String, RefBookValue>> entryDuplicate: duplicateDulList.entrySet()) {
                    boolean exist = false;
                    for(Map.Entry<Long, Map<String, RefBookValue>> entryOriginal: originalDulList.entrySet()) {
                        // проверяем существование ДУЛов у оригинала
                        if (compare(entryOriginal.getValue(), entryDuplicate.getValue())) {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        // Копируем ДУЛы
                        for (Long uniqueRecordId: originalUniqueRecordIds) {
                            RefBookRecord refBookRecord = new RefBookRecord();
                            refBookRecord.setValues(newDul(entryDuplicate.getValue(), uniqueRecordId, duplicateRecordId));
                            newDulList.add(refBookRecord);
                        }
                    }
                }

                if (!newDulList.isEmpty()) {
                    dulDataProvider.createRecordVersion(logger, new Date(), null, newDulList);
                }
            } else if (duplicateRecordId.equals(originalRecordId)) {
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
            } else if (duplicateRecordId.equals(originalRecordId)) {
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

    private boolean compare(Map<String, RefBookValue> o1, Map<String, RefBookValue> o2) {
        if (!o1.get("DOC_ID").equals(o2.get("DOC_ID"))) {
            return false;
        }
        if (!o1.get("DOC_NUMBER").equals(o2.get("DOC_NUMBER"))) {
            return false;
        }
        return true;
    }

    private Map<String, RefBookValue> newDul(Map<String, RefBookValue> original, Long originalId, Long duplicateRecordId) {
        Map<String, RefBookValue> newDul = new HashMap<String, RefBookValue>();
        newDul.put("DOC_ID", original.get("DOC_ID"));
        newDul.put("DOC_NUMBER", original.get("DOC_NUMBER"));
        newDul.put("ISSUED_BY", original.get("ISSUED_BY"));
        newDul.put("ISSUED_DATE", original.get("ISSUED_DATE"));
        newDul.put("INC_REP", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        newDul.put("PERSON_ID", new RefBookValue(RefBookAttributeType.REFERENCE, originalId));
        if (original.get("DUPLICATE_RECORD_ID").getNumberValue() != null) {
            newDul.put("DUPLICATE_RECORD_ID", original.get("DUPLICATE_RECORD_ID"));
        } else {
            newDul.put("DUPLICATE_RECORD_ID", new RefBookValue(RefBookAttributeType.NUMBER, duplicateRecordId));
        }
        return newDul;
    }


    @Override
    public void undo(SaveDuplicatePersonAction action, SaveDuplicatePersonResult result,
                     ExecutionContext executionContext) throws ActionException {
    }
}
