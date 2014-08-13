package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CheckExistenceDeclaration;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CheckExistenceDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CheckExistenceDeclarationHandler extends AbstractActionHandler<CheckExistenceDeclaration, CheckExistenceDeclarationResult> {

	public CheckExistenceDeclarationHandler() {
		super(CheckExistenceDeclaration.class);
	}

	@Autowired
	DeclarationDataService declarationService;

    @Autowired
    DeclarationTypeService declarationTypeService;

    @Autowired
    private RefBookFactory rbFactory;

    @Autowired
    DepartmentDao departmentDao;

    @Autowired
    private LogEntryService logEntryService;

	@Override
	public CheckExistenceDeclarationResult execute(CheckExistenceDeclaration command, ExecutionContext executionContext) throws ActionException {
        Integer declarationTypeId = command.getDeclarationTypeId();
        if (command.getTaxType().equals(TaxType.DEAL)) {
            List<DeclarationType> declarationTypeList = declarationTypeService.getTypes(command.getDepartmentId(), command.getReportPeriodId(), TaxType.DEAL);
            if (declarationTypeList.size() == 1) {
                declarationTypeId = declarationTypeList.get(0).getId();
            } else {
                throw new ActionException("Не удалось определить шаблон для уведомления.");
            }
        }
        DeclarationData declarationData = declarationService.find(declarationTypeId, command.getDepartmentId(), command.getReportPeriodId());
		CheckExistenceDeclarationResult result = new CheckExistenceDeclarationResult();
        Logger logger = new Logger();
		if ((declarationData != null)) {
			if (declarationData.isAccepted()) {
				result.setStatus(CheckExistenceDeclarationResult.DeclarationStatus.EXIST_ACCEPTED);
			} else {
				result.setStatus(CheckExistenceDeclarationResult.DeclarationStatus.EXIST_CREATED);
			}
            logger.error("Декларация с заданными параметрами уже существует");
		} else {
			result.setStatus(CheckExistenceDeclarationResult.DeclarationStatus.NOT_EXIST);
		}

        if(command.getTaxType().equals(TaxType.PROPERTY)) {
            // TODO проверить
            StringBuilder filter = new StringBuilder();
            Long regionId = departmentDao.getDepartment(command.getDepartmentId()).getRegionId();
            filter.append("TAX_ORGAN_CODE").append(" = ").append(command.getTaxOrganCode())
                    .append(" and ").append("KPP").append(" = ").append(command.getTaxOrganKpp())
                    .append(" and ").append("DECLARATION_REGION_ID").append(" = ").append(regionId);
            RefBookDataProvider provider = rbFactory.getDataProvider(200L);
            List<Pair<Long, Long>> checkRecordExistence = provider.checkRecordExistence(new Date(), filter.toString());
            if (checkRecordExistence == null || checkRecordExistence.isEmpty()) {
                logger.error("Выбранные налоговый орган и КПП не созданы в выбранном периоде (справочник \"Параметры представления деклараций по налогу на имущество\"");
                result.setStatus(null);
            }
		}

        result.setUuid(logEntryService.save(logger.getEntries()));
		return result;
	}

	@Override
	public void undo(CheckExistenceDeclaration createDeclaration, CheckExistenceDeclarationResult createDeclarationResult, ExecutionContext executionContext) throws ActionException {
		//Nothing
	}
}
