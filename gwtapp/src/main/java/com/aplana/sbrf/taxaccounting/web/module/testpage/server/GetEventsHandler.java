package com.aplana.sbrf.taxaccounting.web.module.testpage.server;

import com.aplana.sbrf.taxaccounting.common.model.EventType;
import com.aplana.sbrf.taxaccounting.web.module.testpage.shared.GetEventsAction;
import com.aplana.sbrf.taxaccounting.web.module.testpage.shared.GetEventsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author aivanov
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetEventsHandler extends AbstractActionHandler<GetEventsAction, GetEventsResult> {

    public GetEventsHandler() {
        super(GetEventsAction.class);
    }

    @Override
    public GetEventsResult execute(GetEventsAction action, ExecutionContext executionContext) throws ActionException {
        GetEventsResult result = new GetEventsResult();
        Map<Integer, String> events = new HashMap<Integer, String>();
        for (EventType eventType : EventType.values()) {
            events.put(eventType.getCode(), eventType.getTitle());
        }
        result.setMap(events);
        return result;
    }


    @Override
    public void undo(GetEventsAction deleteFormsSourseAction, GetEventsResult deleteFormsSourceResult, ExecutionContext executionContext) throws ActionException {

    }
}
