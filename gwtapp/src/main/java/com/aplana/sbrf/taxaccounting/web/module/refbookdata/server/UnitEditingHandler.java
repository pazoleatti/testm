package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentChangeOperationType;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.server.ws.DepartmentWS_Service;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookValueSerializable;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.UnitEditingAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.UnitEditingResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Обновление имени подразделения в печатных формах
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
@Transactional
public class UnitEditingHandler extends AbstractActionHandler<UnitEditingAction, UnitEditingResult> {

    @Autowired
	private FormDataService formDataService;
    @Autowired
	private SecurityService securityService;
    @Autowired
    private RefBookDepartmentDao refBookDepartmentDao;
    @Autowired
	private AuditService auditService;
    @Autowired
	private RefBookDao refBookDao;
    @Autowired
    private LockDataService lockService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private DepartmentWS_Service departmentWS_service;

	private static final String LOCK_MESSAGE = "Справочник \"%s\" заблокирован, попробуйте выполнить операцию позже!";


    public UnitEditingHandler() {
        super(UnitEditingAction.class);
    }

    @Override
    public UnitEditingResult execute(UnitEditingAction action, ExecutionContext executionContext) throws ActionException {
        List<String> lockedObjects = new ArrayList<String>();
        Logger logger = new Logger();
        int userId = securityService.currentUserInfo().getUser().getId();
        String lockKey = refBookFactory.generateTaskKey(RefBookDepartmentDao.REF_BOOK_ID, ReportType.EDIT_REF_BOOK);
        RefBook refBook = refBookDao.get(RefBookDepartmentDao.REF_BOOK_ID);
        Pair<ReportType, LockData> lockType = refBookFactory.getLockTaskType(RefBookDepartmentDao.REF_BOOK_ID);
        if (lockType == null && lockService.lock(lockKey, userId,
                String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), refBook.getName())) == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                        String referenceLockKey = refBookFactory.generateTaskKey(attribute.getRefBookId(), ReportType.EDIT_REF_BOOK);
                        if (!lockedObjects.contains(referenceLockKey)) {
                            LockData referenceLockData = lockService.lock(referenceLockKey, userId,
                                    String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), attributeRefBook.getName()));
                            if (referenceLockData == null) {
                                //Блокировка установлена
                                lockedObjects.add(referenceLockKey);
                            } else {
                                throw new ServiceLoggerException(String.format(LOCK_MESSAGE, attributeRefBook.getName()),
                                        logEntryService.save(logger.getEntries()));
                            }
                        }
                    }
                }

                Map<String, RefBookValue> valueToSave = new HashMap<String, RefBookValue>();
                for(Map.Entry<String, RefBookValueSerializable> v : action.getValueToSave().entrySet()) {
                    RefBookValue value = new RefBookValue(v.getValue().getAttributeType(), v.getValue().getValue());
                    valueToSave.put(v.getKey(), value);
                }

                refBookDepartmentDao.update(action.getDepId(), valueToSave, refBook.getAttributes());
                logger.info("Подразделение сохранено");
                departmentWS_service.sendChange(DepartmentChangeOperationType.UPDATE, action.getDepId(), logger);

                auditService.add(FormDataEvent.UPDATE_DEPARTMENT, securityService.currentUserInfo(), action.getDepId(),
                        null, null, null, null,
                        String.format("Изменены значения атрибутов подразделения %s, новые значения атрибутов: %s",
                                action.getDepName(),
                                assembleMessage(valueToSave)), null);
                if (action.getVersionFrom()!= null){
                    if (!action.isChangeType()){
                        //Обновляем имена подразделений в печатных формах
                        formDataService.
                                updateFDDepartmentNames(action.getDepId(), action.getDepName(), action.getVersionFrom(), action.getVersionTo(), securityService.currentUserInfo());
                    }else {
                        //Обновляем имена ТБ в печатных формах
                        formDataService.
                                updateFDTBNames(action.getDepId(), action.getDepName(), action.getVersionFrom(), action.getVersionTo(), action.isChangeType(), securityService.currentUserInfo());
                    }
                }
            } finally {
                for (String lock : lockedObjects) {
                    lockService.unlock(lock, userId);
                }
            }

        } else {
            throw new ServiceException(String.format(LOCK_MESSAGE, refBook.getName()));
        }
        return new UnitEditingResult();
    }

    @Override
    public void undo(UnitEditingAction formDataPrintAction, UnitEditingResult checkCorrectessFDResult, ExecutionContext executionContext) throws ActionException {
        //Nothing
    }

    private String assembleMessage(Map<String, RefBookValue> records){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, RefBookValue> record : records.entrySet()){
            if (!record.getKey().equals("NAME"))
                sb.append(String.format(" %s- %s ", record.getKey(), record.getValue().toString()));
        }

        return sb.toString();
    }
}
