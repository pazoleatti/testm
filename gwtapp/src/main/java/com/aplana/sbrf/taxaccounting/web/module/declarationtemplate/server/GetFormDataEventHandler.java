package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.dao.EventDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetFormDataEventAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetFormDataEventResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class GetFormDataEventHandler extends AbstractActionHandler<GetFormDataEventAction, GetFormDataEventResult> {

    @Autowired
    private EventDao eventDao;

    public GetFormDataEventHandler() {
        super(GetFormDataEventAction.class);
    }

    @Override
    public GetFormDataEventResult execute(GetFormDataEventAction action, ExecutionContext context) throws ActionException {
        GetFormDataEventResult result = new GetFormDataEventResult();
        Collection<Integer> eventIdList = eventDao.fetch();
        for (Integer eventId: eventIdList) {
            result.getEventList().add(FormDataEvent.getByCode(eventId));
        }
        return result;
    }

    @Override
    public void undo(GetFormDataEventAction action, GetFormDataEventResult result, ExecutionContext context) throws ActionException {

    }
}
