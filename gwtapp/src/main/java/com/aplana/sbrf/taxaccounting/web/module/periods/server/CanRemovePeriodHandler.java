package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CanRemovePeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CanRemovePeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
@Service
public class CanRemovePeriodHandler extends AbstractActionHandler<CanRemovePeriodAction, CanRemovePeriodResult> {

	public CanRemovePeriodHandler() {
		super(CanRemovePeriodAction.class);
	}

	@Autowired
	private RefBookFactory rbFactory;
	@Autowired
	private PeriodService periodService;

	static final long REF_BOOK_101 = 50L;
	static final long REF_BOOK_102 = 52L;

	@Override
	public CanRemovePeriodResult execute(CanRemovePeriodAction action, ExecutionContext executionContext) throws ActionException {
		CanRemovePeriodResult result = new CanRemovePeriodResult();
		RefBookDataProvider dataProvider = rbFactory.getDataProvider(REF_BOOK_101);

		Date endDate = periodService.getEndDate(action.getReportPeriodId()).getTime();
		PagingResult<Map<String, RefBookValue>> result101 =  dataProvider.getRecords(endDate, null, null, null);

		dataProvider = rbFactory.getDataProvider(REF_BOOK_102);
		PagingResult<Map<String, RefBookValue>> result102 =  dataProvider.getRecords(endDate, null, null, null);
		result.setCanRemove(result101.isEmpty() && result102.isEmpty());
		return result;

	}

	@Override
	public void undo(CanRemovePeriodAction canRemovePeriodAction, CanRemovePeriodResult canRemovePeriodResult, ExecutionContext executionContext) throws ActionException {
	}
}
