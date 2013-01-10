package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetFilterDataHandler  extends AbstractActionHandler<GetFilterData, GetFilterDataResult> {

	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private FormDataSearchService formDataSearchService;

	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private ReportPeriodDao reportPeriodDao;


    public GetFilterDataHandler() {
        super(GetFilterData.class);
    }

    @Override
    public GetFilterDataResult execute(GetFilterData action, ExecutionContext executionContext) throws ActionException {
        GetFilterDataResult res = new GetFilterDataResult();
        res.setDepartments(formDataSearchService.listAllDepartmentsByParentDepartmentId(securityService.currentUser()
				.getDepartmentId()));
        res.setFormTypes(formDataSearchService.getAvailableFormTypes(securityService.currentUser().getId(),
                action.getTaxType()));
		res.setPeriods(formDataSearchService.listReportPeriodsByTaxType(action.getTaxType()));
		
		try {
			ReportPeriod rp = reportPeriodDao.getCurrentPeriod(action.getTaxType());
			if (rp != null) {
				res.setCurrentReportPeriodId(rp.getId());
			}
		} catch (DaoException e) {
			logger.warn("Failed to find current report period for taxType = " + action.getTaxType() + ", message is: " + e.getMessage());
		}
        return res;
    }

    @Override
    public void undo(GetFilterData getFilterData, GetFilterDataResult getFilterDataResult, ExecutionContext executionContext) throws ActionException {
        //ничего не делаем
    }
}
