package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;


import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.TARoleService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.SendQueryAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.SendQueryResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;


@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_NS')")
public class SendQueryHandler extends AbstractActionHandler<SendQueryAction, SendQueryResult> {

    private final String TITLE = "АС «Учет налогов». Запрос на изменение справочника «Организации-участники контролируемых сделок»";

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private TAUserService taUserService;

    @Autowired
    private TARoleService taRoleService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DepartmentService departmentService;

    public SendQueryHandler() {
        super(SendQueryAction.class);
    }

    @Override
    public SendQueryResult execute(SendQueryAction action, ExecutionContext executionContext) throws ActionException {
        SendQueryResult result = new SendQueryResult();
        Logger logger = new Logger();

        String emails = getEmails();
        String title = TITLE;
        String message = getMessage(action.getMessage());

        //TODO отправка письма http://jira.aplana.com/browse/SBRFACCTAX-8470

        logger.info("Запрос на изменение отправлен на рассмотрение Контролёрам УНП");
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    private String getEmails(){
        StringBuilder emails = new StringBuilder();
        PagingResult<TAUserView> list = taUserService.getUsersByFilter(new MembersFilterData() {{
            setRoleIds(new ArrayList<Long>(Arrays.asList((long)taRoleService.getByAlias(TARole.ROLE_CONTROL_UNP).getId())));
        }});
        for (TAUserView userView : list) {
            if (userView.getEmail() != null) {
                emails.append(userView.getEmail()).append(";");
            }
        }
        return  emails.toString();
    }

    private String getMessage(String msg){
        TAUser user = securityService.currentUserInfo().getUser();
        StringBuilder message = new StringBuilder();
        message.append("Пользователь ").append(user.getName())
                .append(" подразделения «").append(departmentService.getDepartment(user.getDepartmentId()).getName()).append("»")
                .append(" запросил изменение справочника «Организации-участники контролируемых сделок».")
                .append("\n")
                .append("Содержание запроса: ")
                .append(msg)
                .append("\n\n")
                .append("--\n")
                .append("Сообщение было создано автоматизированной системой «Учет налогов» в связи с тем, " +
                        "что Вы указаны в этой системе как Контролёр УНП. Пожалуйста, не отвечайте на него.")
                .append("\n")
                .append("Если сообщение было направлено Вам по ошибке, то сообщите об этом, пожалуйста, " +
                        "администратору АС «Учет налогов».");
        return message.toString();
    }

    @Override
    public void undo(SendQueryAction getNameAction, SendQueryResult getNameResult, ExecutionContext executionContext) throws ActionException {
    }
}
