package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
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
    private DepartmentDao departmentDao;

    @Autowired
    private FormTypeDao formTypeDao;


    public GetFilterDataHandler() {
        super(GetFilterData.class);
    }

    @Override
    public GetFilterDataResult execute(GetFilterData action, ExecutionContext executionContext) throws ActionException {
        GetFilterDataResult res = new GetFilterDataResult();
        res.setDepartments(departmentDao.listDepartments());
        res.setKinds(formTypeDao.listFormTypes());
        return res;
    }

    @Override
    public void undo(GetFilterData getFilterData, GetFilterDataResult getFilterDataResult, ExecutionContext executionContext) throws ActionException {
        //ничего не делаем
    }
}
