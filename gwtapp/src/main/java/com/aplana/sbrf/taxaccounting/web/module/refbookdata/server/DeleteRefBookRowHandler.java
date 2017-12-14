package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.RegionSecurityService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.DeleteRefBookRowAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.DeleteRefBookRowResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class DeleteRefBookRowHandler extends AbstractActionHandler<DeleteRefBookRowAction, DeleteRefBookRowResult> {

	public DeleteRefBookRowHandler() {
		super(DeleteRefBookRowAction.class);
	}

	@Autowired
	RefBookFactory refBookFactory;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private RegionSecurityService regionSecurityService;

	@Override
	public DeleteRefBookRowResult execute(DeleteRefBookRowAction action, ExecutionContext executionContext) throws ActionException {
        DeleteRefBookRowResult result = new DeleteRefBookRowResult();

        TAUser user = securityService.currentUserInfo().getUser();
        for (Long recordId : action.getRecordsId()) {
            if (!regionSecurityService.checkDelete(user, action.getRefBookId(), recordId, action.isDeleteVersion())) {
                result.setCheckRegion(false);
                return result;
            }
        }
        result.setCheckRegion(true);

        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefBookId());
        Logger logger = new Logger();
        logger.setTaUserInfo(securityService.currentUserInfo());
        if (!action.getRecordsId().isEmpty()) {
            if (action.isDeleteVersion()) {
                Long nextVersion = refBookDataProvider.getFirstRecordId(action.getRecordsId().get(0));
                refBookDataProvider.deleteRecordVersions(logger, action.getRecordsId(), false);
                result.setNextVersion(nextVersion);
            } else {
                try {
                    if (action.getRefBookId().equals(RefBook.Id.PERSON.getId())) {
                        RefBookDataProvider idDocDataProvider = refBookFactory.getDataProvider(RefBook.Id.ID_DOC.getId());
                        RefBookDataProvider idTaxPayerDataProvider = refBookFactory.getDataProvider(RefBook.Id.ID_TAX_PAYER.getId());
                        List<Map<Long, Map<String, RefBookValue>>> idDocs = new LinkedList<Map<Long, Map<String, RefBookValue>>>();
                        List<Map<Long, Map<String, RefBookValue>>> idTaxPayers = new LinkedList<Map<Long, Map<String, RefBookValue>>>();
                        for (Long personId : action.getRecordsId()) {
                            idDocs.add(idDocDataProvider.getRecordDataWhere("PERSON_ID = " + personId));
                            idTaxPayers.add(idTaxPayerDataProvider.getRecordDataWhere("PERSON_ID = " + personId));
                        }
                        List<Long> idDocsForDelete = new LinkedList<Long>();
                        List<Long> idTaxPayersForDelete = new LinkedList<Long>();
                        for (Map<Long, Map<String, RefBookValue>> idDocItem : idDocs) {
                            idDocsForDelete.addAll(idDocItem.keySet());
                        }
                        for (Map<Long, Map<String, RefBookValue>> idTaxPayerItem : idTaxPayers) {
                            idTaxPayersForDelete.addAll(idTaxPayerItem.keySet());
                        }
                        if (!idDocsForDelete.isEmpty()) {
                            idDocDataProvider.deleteAllRecords(logger, idDocsForDelete);
                        }
                        if (!idTaxPayersForDelete.isEmpty()) {
                            idTaxPayerDataProvider.deleteAllRecords(logger, idTaxPayersForDelete);
                        }
                        refBookDataProvider.deleteAllRecords(logger, action.getRecordsId());
                    } else {
                        refBookDataProvider.deleteAllRecords(logger, action.getRecordsId());
                    }
                } catch (ServiceLoggerException e) {
                    result.setException(true);
                    logger.error(e.getMessage());
                    if (e.getUuid() != null) {
                        logger.getEntries().addAll(logEntryService.getAll(e.getUuid()));
                    }
                }
            }
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
		return result;
	}

	@Override
	public void undo(DeleteRefBookRowAction deleteRefBookRowAction, DeleteRefBookRowResult deleteRefBookRowResult, ExecutionContext executionContext) throws ActionException {
	}
}
