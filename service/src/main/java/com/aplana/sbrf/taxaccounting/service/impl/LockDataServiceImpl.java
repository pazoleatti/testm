package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Service("lockDataService")
@Transactional(propagation = Propagation.SUPPORTS)
public class LockDataServiceImpl implements LockDataService {

    private static final Log LOG = LogFactory.getLog(LockDataServiceImpl.class);

    @Autowired
    private LockDataDao dao;
    @Autowired
    private TAUserDao userDao;
    @Autowired
    private TransactionHelper tx;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private TAUserService userService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private LogEntryService logEntryService;

    @Override
    public LockData lock(final String key, final int userId, final String description) {
        return tx.executeInNewTransaction(new TransactionLogic<LockData>() {
            @Override
            public LockData execute() {
                try {
                    synchronized (LockDataServiceImpl.class) {
                        LockData lock = dao.findByKey(key);
                        if (lock != null) {
                            LOG.info(String.format("Lock with key \"%s\" already exists: %s", key, lock));
                            return lock;
                        }
                        LOG.info(String.format("Set lock with key \"%s\" by user: %s", key, userId));
                        internalLock(key, userId, description);
                    }
                    return null;
                } catch (Exception e) {
                    throw new ServiceException("???? ?????????????? ???????????????????? ???????????????????? ??????????????", e);
                }
            }
        });
    }

    /**
     * ???????????????????? ?????? ???????????? ???????????????? - ?????????????????? ?????????????????? ???????????????????? ?????????????????? ?? ???? ?????? ?????????????????? ?????????????? ??????????????
     */
    private void internalLock(String key, int userId, String description) {
        dao.lock(key, userId, description);
    }


    @Override
    public LockData lockAsync(final String key, final int userId) {
        return tx.executeInNewTransaction(new TransactionLogic<LockData>() {
            @Override
            public LockData execute() {
                try {
                    synchronized (LockDataServiceImpl.class) {
                        LockData lock = dao.findByKey(key);
                        if (lock != null) {
                            LOG.info(String.format("Lock with key \"%s\" already exists: %s", key, lock));
                            return lock;
                        }
                        LOG.info(String.format("Set lock with key \"%s\" by user: %s", key, userId));
                        dao.lock(key, userId);
                    }
                    return null;
                } catch (Exception e) {
                    throw new ServiceException("???? ?????????????? ???????????????????? ???????????????????? ??????????????", e);
                }
            }
        });
    }

    @Override
    public LockData findLock(String key) {
        synchronized (LockDataServiceImpl.class) {
            return dao.findByKey(key);
        }
    }

    @Override
    public PagingResult<LockDataDTO> findAllByFilter(String filter, PagingParams pagingParams, TAUser user) {
        PagingResult<LockDataDTO> locks = dao.getLocks(filter, pagingParams);
        for (LockDataDTO lock : locks) {
            if (userHasPrivilegesToUnlock(lock.getKey(), user)) {
                lock.setAllowedToUnlock(true);
            }
        }
        return locks;
    }

    @Override
    public void unlock(final String lockKey) {
        try {
            tx.executeInNewTransaction(new TransactionLogic<Boolean>() {
                @Override
                public Boolean execute() {
                    dao.unlock(lockKey);
                    return true;
                }
            });
        } catch (Exception e) {
            throw new ServiceException("?????? ???????????????? ???????????????????? \"%s\" ???????????????? ?????????????????? ????????????. ???????????????? ???????????????????? ????????????????????. ???????????????????? ???? ?????????????????????????? ?? ????????????????????????????.", lockKey);
        }
    }

    @Override
    public Boolean unlock(final String key, final int userId) {
        return unlock(key, userId, false);
    }

    @Override
    public Boolean unlock(final String key, final int userId, final boolean force) {
        return tx.executeInNewTransaction(new TransactionLogic<Boolean>() {
            @Override
            public Boolean execute() {
                try {
                    synchronized (LockDataServiceImpl.class) {
                        LockData lock = dao.findByKey(key);
                        if (lock != null) {
                            if (!force && lock.getUserId() != userId) {
                                TAUser blocker = userDao.getUser(lock.getUserId());
                                throw new ServiceException(String.format("???????????????????? ?????????????? ????????????????????, ?????? ?????? ?????? ?????????????????????? " +
                                        "?????????????????????????? \"%s\"", blocker.getLogin()));
                            }
                            dao.unlockOld(key);
                            LOG.info(String.format("Lock with key \"%s\" was removed by user: %s", key, userId));
                        } else if (!force) {
                            LOG.warn(String.format("???????????? ?????????? ???????????????????????????? ????????????????????. key = \"%s\"", key));
                            return false;
                        }
                    }
                } catch (ServiceException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceException("???? ?????????????? ?????????? ???????????????????? ?? ??????????????", e);
                }
                return true;
            }
        });
    }

    @Override
    public void unlockAll(TAUserInfo userInfo, boolean ignoreError) {
        LOG.info(String.format("All locks was removed by user: %s", userInfo.getUser().getId()));
        dao.unlockAllByUserId(userInfo.getUser().getId(), ignoreError);
    }

    @Override
    public ActionResult unlockAllWithoutTasks(List<String> lockKeys, TAUser user) {
        Logger logger = new Logger();
        ActionResult result = new ActionResult();
        try {
            checkPrivilegesToUnlock(lockKeys, user);
        } catch (ServiceException e) {
            logger.error(e.getMessage());
            String logsUuid = logEntryService.save(logger.getEntries());
            result.setUuid(logsUuid);
            return result;
        }

        for (String lockKey : lockKeys) {
            unlockIfNoTask(lockKey, user, logger);
        }

        String logsUuid = logEntryService.save(logger.getEntries());
        result.setUuid(logsUuid);
        return result;
    }

    /**
     * ???????????????? ???????? ???????????????????????? ???? ???????????????? ???????????? ??????????.
     *
     * @param lockKeys ?????????? ??????????
     * @param user     ????????????????????????
     * @throws ServiceException ???????? ?????? ???????? ???? ???????????????? ???????? ???? ?????????? ????????????
     */
    private void checkPrivilegesToUnlock(List<String> lockKeys, TAUser user) throws ServiceException {
        for (String lockKey : lockKeys) {
            if (!userHasPrivilegesToUnlock(lockKey, user)) {
                throw new ServiceException("???????????????????? %s ???? ?????????? ???????? ??????????????. ??????????????: \"???????????????????????? ???????? (???????????????????? ?? ????????????????????????????)\"", lockKey);
            }
        }
    }

    /**
     * ???????????? ????????????????????, ???????? ?????? ?????? ???? ???????????????? ?????????????????????? ????????????.
     * ?????????????? ???????????????????? ???????????????????????? ?? ????????????????????.
     */
    private void unlockIfNoTask(String lockKey, TAUser user, Logger logger) {
        LockData lock = findLock(lockKey);
        if (lock == null) {
            logger.error("???????????????????? %s ???? ?????????? ???????? ??????????????. ??????????????: \"???????????????????? ???? ????????????????????\"", lockKey);
            return;
        }
        try {
            checkLockHasNoTask(lock);
            unlock(lockKey);
            logger.info("????????????????????: \"%s\" ?????????????? ??????????????????????????: \"%s (%s)\"", lock.getDescription(), user.getName(), user.getLogin());
        } catch (ServiceException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * ???????????????? ???????????????????? ???? ?????????????? ?????????????????????? ????????????
     *
     * @throws ServiceException ???????? ???????????? ????????????????????????
     */
    private void checkLockHasNoTask(LockData lock) throws ServiceException {
        Long taskId = lock.getTaskId();
        if (taskId != null) {
            AsyncTaskData taskData = asyncManager.getLightTaskData(taskId);
            if (taskData != null) {
                int userId = taskData.getUserId();
                TAUser taskCreator = userService.getUser(userId);
                String errorMessage = "???????????????? ???????????????????? %s ????????????????????. ?????????????????????? ???????????????? \"%s\". ????????????????????????: %s (%s)";
                throw new ServiceException(errorMessage, lock.getKey(), taskData.getDescription(), taskCreator.getName(), taskCreator.getLogin());
            }
        }
    }


    @Override
    public int unlockIfOlderThan(long seconds) {
        TaskInterruptCause cause = TaskInterruptCause.SCHEDULER_OLD_LOCK_DELETE;
        List<String> keyList = dao.getLockIfOlderThan(seconds);

        if (keyList.size() > 0) {
            TAUserInfo systemUserInfo = new TAUserInfo();
            systemUserInfo.setUser(userDao.getUser(0));
            for (String key : keyList) {
                LockData lockData = dao.findByKey(key);
                dao.unlock(key);
                LOG.info(String.format("Lock was removed by scheduler as outdated: %s", key));
                auditLockDeletion(systemUserInfo, lockData, cause);
            }
        }

        return keyList.size();
    }

    private void auditLockDeletion(TAUserInfo userInfo, LockData lockData, TaskInterruptCause cause) {
        try {
            String note = cause.getEventDescrition(lockData.getDateLock(), userDao.getUser(lockData.getUserId()), lockData.getDescription());
            auditService.add(FormDataEvent.DELETE_LOCK, userInfo, null, null, null, null, null, null, note, null);
        } catch (Exception e) {
            LOG.error("???????????? ?????? ??????????????????????", e);
        }
    }


    @Override
    public void unlockAllByTask(long taskId) {
        dao.unlockAllByTaskId(taskId);
    }

    @Override
    public boolean lockExists(final String key) {
        synchronized (LockDataServiceImpl.class) {
            return dao.existsByKey(key);
        }
    }

    @Override
    public void bindTask(final String lockKey, final long taskId) {
        tx.executeInNewTransaction(new TransactionLogic<Boolean>() {
            @Override
            public Boolean execute() {
                synchronized (LockDataServiceImpl.class) {
                    LOG.info(String.format("Bind async %s with lock %s", taskId, lockKey));
                    dao.bindTask(lockKey, taskId);
                }
                return null;
            }
        });
    }

    @Override
    public List<LockDataDTO> fetchAllByKeyPrefixSet(Collection<String> keysBlocker) {
        List<LockData> lockDataList = dao.fetchAllByKeyPrefixSet(keysBlocker);
        List<LockDataDTO> lockDataItems = new ArrayList<>(lockDataList.size());
        for (LockData lockData : lockDataList) {
            LockDataDTO lockDataItem = new LockDataDTO();
            lockDataItem.setId(lockData.getId());
            lockDataItem.setKey(lockData.getKey());
            lockDataItem.setDateLock(lockData.getDateLock());
            lockDataItem.setUser(userDao.getUser(lockData.getUserId()).getName());
            lockDataItems.add(lockDataItem);
        }
        return lockDataItems;
    }

    @Override
    public void bindTaskToMultiKeys(final Collection<String> keys, final long taskId) {
        tx.executeInNewTransaction(new TransactionLogic<Object>() {
            @Override
            public Object execute() {
                dao.bindTaskToMultiKeys(keys, taskId);
                return null;
            }
        });
    }

    @Override
    public void unlockMultipleTasks(final Collection<String> keys) {
        tx.executeInNewTransaction(new TransactionLogic<Object>() {
            @Override
            public Object execute() {
                dao.unlockMultipleTasks(keys);
                return null;
            }
        });
    }

    @Override
    public long getDeclarationIdByLockKey(String lockKey) {
        String[] lockKeyWords = lockKey.split("_");
        return Long.parseLong(lockKeyWords[2]);
    }

    /**
     * ????????????????, ?????????? ???? ???????????????????????? ?????????? ???? ???????????? ???????????? ????????????????????.
     */
    private boolean userHasPrivilegesToUnlock(String lockKey, TAUser user) {
        // ???????????????? ??????????, ?????????????? ???????????? ????????????
        if (userHasAllLocksPermissions(user)) {
            return true;
        }
        // ?????????????????? ???? (????????) ?????????? ?????????????? ???????????????????? ???????????? ???? ????, ?? ?????????????? ?? ???????? ???????? ????????????
        if (user.hasRole(TARole.N_ROLE_CONTROL_NS) && userHasAccessToLockByTerbank(user, lockKey)) {
            return true;
        }
        // ???????????????? (????????) ?????????? ?????????????? ???????????? ?????????????????????? ????????????????????
        // noinspection RedundantIfStatement // ?????????? ??????????????????, ???? ??????????, ?????????? ???????? ??????????, ?????? ?? ?????????? ???????? return false
        if (user.hasRole(TARole.N_ROLE_OPER) && isLockSetByUser(lockKey, user)) {
            return true;
        }
        return false;
    }

    /**
     * ?????????? ???? ???????????????????????? ?????? ?????????? ?????? ???????????? ?? ????????????????????????.
     */
    private boolean userHasAllLocksPermissions(TAUser user) {
        return (user.hasRole(TARole.ROLE_ADMIN) || user.hasRole(TARole.N_ROLE_CONTROL_UNP));
    }

    /**
     * ?????????????????????? ???? ???????????????????? ???????? ??????????????????????????
     */
    private boolean isLockSetByUser(String lockKey, TAUser user) {
        LockData lock = findLock(lockKey);
        return lock.getUserId() == user.getId();
    }

    /**
     * ????????????????, ?????????? ???? ???????????????????????? ???????????? ?? ???????????????????? ???? ???? ????????????????
     */
    private boolean userHasAccessToLockByTerbank(TAUser user, String lockKey) {
        List<Integer> userAvailableTerbankIds = departmentService.findAllAvailableTBIds(user);
        Integer lockTerbankId = getLockTerbankId(lockKey);
        return (lockTerbankId != null && userAvailableTerbankIds.contains(lockTerbankId));
    }

    /**
     * ?????????????????????? ???????????????? ????????????????????
     */
    private Integer getLockTerbankId(String lockKey) {
        if (isLockByDeclaration(lockKey)) {
            return getLockDeclarationTerbankId(lockKey);
        } else {
            return getLockCreatorTerbankId(lockKey);
        }
    }

    /**
     * ????????????????, ???????????????????? ???? ???????????????????? ???? ?????????????????? ?????????? (declarationData)
     */
    private boolean isLockByDeclaration(String lockKey) {
        return lockKey.startsWith("DECLARATION_DATA");
    }

    /**
     * ?????????????????????? ???? ???????????????? ?????????????????? ?????????? ?????? ????????????????????.
     */
    private Integer getLockDeclarationTerbankId(String lockKey) {
        try {
            // ???????? ???????????????????? ???? ?????????????????? ?????????? ???????????? ???????????????????? ?? DECLARATION_DATA_<ID>
            long declarationId = getDeclarationIdByLockKey(lockKey);
            DeclarationData declaration = declarationDataService.get(declarationId);

            if (declaration != null) {
                int declarationDepartmentId = declaration.getDepartmentId();
                return departmentService.getParentTBId(declarationDepartmentId);
            }
        } catch (Exception e) {
            LOG.warn("???????????????? ???????????????????? ?? ???????? ?????????????????????? ?????????????????? ?????????? ?? ???????????????? ???????????????????? " + lockKey);
            return null;
        }
        return null;
    }

    /**
     * ?????????????????????? ???????????????? ????????????????????????, ???????????????????????? ????????????????????.
     */
    private Integer getLockCreatorTerbankId(String lockKey) {
        LockData lock = findLock(lockKey);
        int creatorId = lock.getUserId();
        TAUser taskCreator = userService.getUser(creatorId);
        return departmentService.getParentTBId(taskCreator.getDepartmentId());
    }
    //</editor-fold>
}