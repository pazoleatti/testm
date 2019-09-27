package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.TransportMessageDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageFilter;
import com.aplana.sbrf.taxaccounting.permissions.PermissionUtils;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
    @Autowired
    private PrintingService printingService;


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

    private boolean checkExportAccess(TAUserInfo userInfo) {
        TAUser user = userInfo.getUser();
        if (user.hasRole(TARole.N_ROLE_CONTROL_NS) || user.hasRole(TARole.N_ROLE_CONTROL_UNP)) {
            return true;
        }
        throw new AccessDeniedException("Нет прав на выгрузку транспортных сообщений");
    }

    @Override
    public InputStream export(String headerDescription, TransportMessageFilter filter, TAUserInfo userInfo) throws IOException {
        checkExportAccess(userInfo);

        List<Long> transportMessageIds = getTransportMessageIds(filter);

        List<TransportMessage> transportMessages =
                CollectionUtils.isEmpty(transportMessageIds)
                        ? transportMessageDao.findByFilter(filter, null)
                        : transportMessageDao.findByIds(transportMessageIds);

        InputStream inputStream = buildExcelStream(transportMessages, headerDescription);
        if (inputStream == null)
            throw new ServiceException("Нет данных для формирования Excel-файла транспортных сообщений");
        return inputStream;
    }

    private List<Long> getTransportMessageIds(TransportMessageFilter filter) {
        return filter != null ? filter.getIds() : null;
    }


    private InputStream buildExcelStream(List<TransportMessage> transportMessages, String headerDescription) throws IOException {
        return printingService.generateExcelTransportMessages(transportMessages, headerDescription);
    }

}
