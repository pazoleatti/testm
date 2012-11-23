package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetFilterDataHandler  extends AbstractActionHandler<GetFilterData, GetFilterDataResult> {

	@Autowired
	private FormDataSearchService formDataSearchService;


    public GetFilterDataHandler() {
        super(GetFilterData.class);
    }

    @Override
    public GetFilterDataResult execute(GetFilterData action, ExecutionContext executionContext) throws ActionException {
        GetFilterDataResult res = new GetFilterDataResult();
        res.setDepartments(formDataSearchService.listDepartments());
        res.setKinds(formDataSearchService.listFormTypes());
		/*TODO: убрать хардкод по TaxType и получать значение из закладки!*/
		res.setPeriods(formDataSearchService.listReportPeriodsByTaxType(TaxType.TRANSPORT));
        return res;
    }

    @Override
    public void undo(GetFilterData getFilterData, GetFilterDataResult getFilterDataResult, ExecutionContext executionContext) throws ActionException {
        //ничего не делаем
    }
}
