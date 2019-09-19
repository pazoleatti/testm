package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.TransportMessageDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageFilter;
import com.aplana.sbrf.taxaccounting.permissions.PermissionUtils;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.service.TransportMessageService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class TransportMessageServiceImpl implements TransportMessageService {

    private static final Log LOG = LogFactory.getLog(TransportMessageServiceImpl.class);

    @Autowired
    private TransportMessageDao transportMessageDao;
    @Autowired
    private AuditService auditService;
    @Autowired
    protected TAUserService userService;
    @Autowired
    protected DepartmentService departmentService;

    @Override
    public TransportMessage findById(Long id) {
        return transportMessageDao.findById(id);
    }

    @Override
    public String findMessageBodyById(Long id) {
        return transportMessageDao.findMessageBodyById(id);
    }

    @Override
    public PagingResult<TransportMessage> findByFilter(TransportMessageFilter filter, PagingParams pagingParams) {
        return transportMessageDao.findByFilter(filter, pagingParams);
    }

    @Nullable
    @Override
    public TransportMessage findFirstByFilter(TransportMessageFilter filter) {
        List<TransportMessage> sourceTransportMessages = transportMessageDao.findByFilter(filter, null);

        TransportMessage result = null;
        if (!CollectionUtils.isEmpty(sourceTransportMessages)) {
            result = sourceTransportMessages.get(0);
        }
        return result;
    }

    @Override
    public PagingResult<TransportMessage> findByFilterWithUserDepartments(TransportMessageFilter filter, PagingParams pagingParams) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Если у пользователя есть роль "Контролер НС", но есть "Контролер УНП", то фильтрация проходит по общим правилам, иначе - согласно постановке
        if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_NS) && !PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP)) {
            TAUser taUser = userService.getUser(currentUser.getUsername());
            List<Integer> userDepartments = departmentService.findAllAvailableIds(taUser);
            if (filter.getDepartmentIds() == null) {
                filter.setDepartmentIds(userDepartments);
            } else {
                // Если пользователь задал подразделения для фильтрации, то удалить те, к которым пользователь не имеет отношения
                List<Integer> departmentList = filter.getDepartmentIds();
                Iterator<Integer> it = departmentList.iterator();
                while (it.hasNext()) {
                    Integer departmentId = it.next();
                    if (!userDepartments.contains(departmentId)) {
                        it.remove();
                    }
                }
                // Если пользователь выбрал только неподходящие для него подразделения, поиск должен вернуть пустоту
                if (departmentList.isEmpty()) {
                    return new PagingResult<>(new ArrayList<TransportMessage>(), 0);
                }
            }
        }
        return findByFilter(filter, pagingParams);
    }

    @Override
    @Transactional
    public void create(TransportMessage transportMessage) {
        transportMessageDao.create(transportMessage);
        LOG.info("Сохранено транспортное сообщение: " + transportMessage);
    }

    @Override
    @Transactional
    public void update(TransportMessage transportMessage) {
        transportMessageDao.update(transportMessage);
    }

    @Override
    public void sendAuditMessage(String noteFormat, TransportMessage transportMessage) {
        String note = String.format(noteFormat,
                transportMessage.getId(),
                transportMessage.getMessageUuid(),
                transportMessage.getType().getText(),
                transportMessage.getState().getText());
        auditService.add(null, userService.getSystemUserInfo(), note);
    }
}
