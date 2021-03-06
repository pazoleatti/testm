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
    @Autowired
    private TransactionHelper transactionHelper;


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

        // ???????? ?? ???????????????????????? ???????? ???????? "?????????????????? ????", ???? ???????? "?????????????????? ??????", ???? ???????????????????? ???????????????? ???? ?????????? ????????????????, ?????????? - ???????????????? ????????????????????
        if (PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_NS) && !PermissionUtils.hasRole(currentUser, TARole.N_ROLE_CONTROL_UNP)) {
            TAUser taUser = userService.getUser(currentUser.getUsername());
            List<Integer> userDepartments = departmentService.findAllAvailableIds(taUser);
            if (filter.getDepartmentIds() == null) {
                filter.setDepartmentIds(userDepartments);
            } else {
                // ???????? ???????????????????????? ?????????? ?????????????????????????? ?????? ????????????????????, ???? ?????????????? ????, ?? ?????????????? ???????????????????????? ???? ?????????? ??????????????????
                List<Integer> departmentList = filter.getDepartmentIds();
                Iterator<Integer> it = departmentList.iterator();
                while (it.hasNext()) {
                    Integer departmentId = it.next();
                    if (!userDepartments.contains(departmentId)) {
                        it.remove();
                    }
                }
                // ???????? ???????????????????????? ???????????? ???????????? ???????????????????????? ?????? ???????? ??????????????????????????, ?????????? ???????????? ?????????????? ??????????????
                if (departmentList.isEmpty()) {
                    return new PagingResult<>(new ArrayList<TransportMessage>(), 0);
                }
            }
        }
        return findByFilter(filter, pagingParams);
    }

    @Override
    @Transactional
    public void create(final TransportMessage transportMessage) {
        transactionHelper.executeInNewTransaction(new TransactionLogic<Object>() {
            @Override
            public Object execute() {
                transportMessageDao.create(transportMessage);
                LOG.info("?????????????????? ???????????????????????? ??????????????????: " + transportMessage);
                return null;
            }
        });
    }

    @Override
    @Transactional
    public void update(final TransportMessage transportMessage) {
        transactionHelper.executeInNewTransaction(new TransactionLogic<Object>() {
            @Override
            public Object execute() {
                transportMessageDao.update(transportMessage);
                return null;
            }
        });
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
        if (user.hasRole(TARole.N_ROLE_CONTROL_NS)
                || user.hasRole(TARole.N_ROLE_CONTROL_UNP)
                || user.hasRole(TARole.ROLE_ADMIN)) {
            return true;
        }
        throw new AccessDeniedException("?????? ???????? ???? ???????????????? ???????????????????????? ??????????????????");
    }

    @Override
    public InputStream export(String headerDescription, TransportMessageFilter filter, TAUserInfo userInfo) throws IOException {
        checkExportAccess(userInfo);

        List<Long> transportMessageIds = getTransportMessageIds(filter);

        List<TransportMessage> transportMessages =
                CollectionUtils.isEmpty(transportMessageIds)
                        ? transportMessageDao.findByFilter(filter, null)
                        : transportMessageDao.findByIds(transportMessageIds);

        sortById(transportMessages);

        InputStream inputStream = null;
        try {
            inputStream = buildExcelStream(transportMessages, headerDescription);
        } catch (Exception e) {
            throw new ServiceException("???????????? ???????????????????????? Excel-?????????? ???????????? ???????????????????????? ??????????????????", e);
        }
        if (inputStream == null)
            throw new ServiceException("?????? ???????????? ?????? ???????????????????????? Excel-?????????? ???????????????????????? ??????????????????");
        return inputStream;
    }

    private void sortById(List<TransportMessage> transportMessages) {
        Collections.sort(transportMessages, new Comparator<TransportMessage>() {
            @Override
            public int compare(TransportMessage o1, TransportMessage o2) {
                return o2.getId().compareTo(o1.getId());
            }
        });
    }

    private List<Long> getTransportMessageIds(TransportMessageFilter filter) {
        return filter != null ? filter.getIds() : null;
    }


    private InputStream buildExcelStream(List<TransportMessage> transportMessages, String headerDescription) throws IOException {
        return printingService.generateExcelTransportMessages(transportMessages, headerDescription);
    }

}
