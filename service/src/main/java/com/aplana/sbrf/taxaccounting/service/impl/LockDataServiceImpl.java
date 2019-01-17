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

import java.util.*;


@Service
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
                        LockData lock = dao.get(key, false);
                        if (lock != null) {
                            LOG.info(String.format("Lock with key \"%s\" already exists: %s", key, lock));
                            return lock;
                        }
                        LOG.info(String.format("Set lock with key \"%s\" by user: %s", key, userId));
                        internalLock(key, userId, description);
                    }
                    return null;
                } catch (Exception e) {
                    throw new ServiceException("Не удалось установить блокировку объекта", e);
                }
            }
        });
    }

    /**
     * Блокировка без всяких проверок - позволяет сократить количество обращений к бд для вложенных вызовов методов
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
                        LockData lock = dao.get(key, false);
                        if (lock != null) {
                            LOG.info(String.format("Lock with key \"%s\" already exists: %s", key, lock));
                            return lock;
                        }
                        LOG.info(String.format("Set lock with key \"%s\" by user: %s", key, userId));
                        dao.lock(key, userId);
                    }
                    return null;
                } catch (Exception e) {
                    throw new ServiceException("Не удалось установить блокировку объекта", e);
                }
            }
        });
    }

    @Override
    public LockData findLock(String key) {
        synchronized (LockDataServiceImpl.class) {
            return dao.get(key, false);
        }
    }

    @Override
    public PagingResult<LockDataDTO> findAllByFilter(String filter, PagingParams pagingParams, TAUser user) {
        PagingResult<LockDataDTO> locks = dao.getLocks(filter, pagingParams, user);
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
            throw new ServiceException("При удалении блокировки \"%s\" возникла системная ошибка. Удаление блокировки невозможно. Обратитесь за разъяснениями к Администратору.", lockKey);
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
                        LockData lock = dao.get(key, false);
                        if (lock != null) {
                            if (!force && lock.getUserId() != userId) {
                                TAUser blocker = userDao.getUser(lock.getUserId());
                                throw new ServiceException(String.format("Невозможно удалить блокировку, так как она установлена " +
                                        "пользователем \"%s\"", blocker.getLogin()));
                            }
                            dao.unlockOld(key);
                            LOG.info(String.format("Lock with key \"%s\" was removed by user: %s", key, userId));
                        } else if (!force) {
                            LOG.warn(String.format("Нельзя снять несуществующую блокировку. key = \"%s\"", key));
                            return false;
                        }
                    }
                } catch (ServiceException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceException("Не удалось снять блокировку с объекта", e);
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
     * Проверка прав пользователя на удаление списка задач.
     *
     * @param lockKeys ключи задач
     * @param user     пользователь
     * @throws ServiceException если нет прав на удаление хотя бы одной задачи
     */
    private void checkPrivilegesToUnlock(List<String> lockKeys, TAUser user) throws ServiceException {
        for (String lockKey : lockKeys) {
            if (!userHasPrivilegesToUnlock(lockKey, user)) {
                throw new ServiceException("Блокировка %s не может быть удалена. Причина: \"недостаточно прав (обратитесь к администратору)\"", lockKey);
            }
        }
    }

    /**
     * Снятие блокировки, если для неё не запущена Асинхронная задача.
     * Создает оповещение пользователю о результате.
     */
    private void unlockIfNoTask(String lockKey, TAUser user, Logger logger) {
        LockData lock = findLock(lockKey);
        if (lock == null) {
            logger.error("Блокировка %s не может быть удалена. Причина: \"блокировка не существует\"", lockKey);
            return;
        }
        try {
            checkLockHasNoTask(lock);
            unlock(lockKey);
            logger.info("Блокировка: \"%s\" удалена пользователем: \"%s (%s)\"", lock.getDescription(), user.getName(), user.getLogin());
        } catch (ServiceException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Проверка блокировки на наличие Асинхронной задачи
     *
     * @throws ServiceException Если задача присутствует
     */
    private void checkLockHasNoTask(LockData lock) throws ServiceException {
        Long taskId = lock.getTaskId();
        if (taskId != null) {
            AsyncTaskData taskData = asyncManager.getLightTaskData(taskId);
            if (taskData != null) {
                int userId = taskData.getUserId();
                TAUser taskCreator = userService.getUser(userId);
                String errorMessage = "Удаление блокировки %s невозможно. Выполняется операция \"%s\". Пользователь: %s (%s)";
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
                LockData lockData = dao.get(key, false);
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
            LOG.error("Ошибка при логировании", e);
        }
    }


    @Override
    public void unlockAllByTask(long taskId) {
        dao.unlockAllByTask(taskId);
    }

    @Override
    public boolean lockExists(final String key, boolean like) {
        synchronized (LockDataServiceImpl.class) {
            return dao.get(key, like) != null;
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
    public List<LockDataDTO> fetchAllByKeySet(Set<String> keysBlocker) {
        List<LockData> lockDataList = dao.fetchAllByKeySet(keysBlocker);
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


    /**
     * Проверка, имеет ли пользователь права на снятие данной блокировки.
     */
    private boolean userHasPrivilegesToUnlock(String lockKey, TAUser user) {
        // Проверка ролей, имеющих полный доступ
        if (userHasAllLocksPermissions(user)) {
            return true;
        }
        // Контролёр НС (НДФЛ) может снимать блокировки только по ТБ, к которым у него есть доступ
        if (user.hasRole(TARole.N_ROLE_CONTROL_NS) && userHasAccessToLockByTerbank(user, lockKey)) {
            return true;
        }
        // Оператор (НДФЛ) может снимать только собственные блокировки
        // noinspection RedundantIfStatement // Можно упростить, но лучше, чтобы было видно, что в конце есть return false
        if (user.hasRole(TARole.N_ROLE_OPER) && isLockSetByUser(lockKey, user)) {
            return true;
        }
        return false;
    }

    /**
     * Имеет ли пользователь все права для работы с блокировками.
     */
    private boolean userHasAllLocksPermissions(TAUser user) {
        return (user.hasRole(TARole.ROLE_ADMIN) || user.hasRole(TARole.N_ROLE_CONTROL_UNP));
    }

    /**
     * Установлена ли блокировка этим пользователем
     */
    private boolean isLockSetByUser(String lockKey, TAUser user) {
        LockData lock = findLock(lockKey);
        return lock.getUserId() == user.getId();
    }

    /**
     * Проверка, имеет ли пользователь доступ к блокировке по её Тербанку
     */
    private boolean userHasAccessToLockByTerbank(TAUser user, String lockKey) {
        List<Integer> userAvailableTerbankIds = departmentService.findAllAvailableTBIds(user);
        Integer lockTerbankId = getLockTerbankId(lockKey);
        return (lockTerbankId != null && userAvailableTerbankIds.contains(lockTerbankId));
    }

    /**
     * Определение Тербанка блокировки
     */
    private Integer getLockTerbankId(String lockKey) {
        if (isLockByDeclaration(lockKey)) {
            return getLockDeclarationTerbankId(lockKey);
        } else {
            return getLockCreatorTerbankId(lockKey);
        }
    }

    /**
     * Проверка, поставлена ли блокировка по Налоговой Форме (declarationData)
     */
    private boolean isLockByDeclaration(String lockKey) {
        return lockKey.startsWith("DECLARATION_DATA");
    }

    /**
     * Определение ИД Тербанка Налоговой формы для блокировки.
     */
    private Integer getLockDeclarationTerbankId(String lockKey) {
        try {
            // Ключ блокировки по Налоговой Форме должен начинаться с DECLARATION_DATA_<ID>
            String[] lockKeyWords = lockKey.split("_");
            long declarationId = Long.parseLong(lockKeyWords[2]);
            DeclarationData declaration = declarationDataService.get(declarationId);

            if (declaration != null) {
                int declarationDepartmentId = declaration.getDepartmentId();
                return departmentService.getParentTBId(declarationDepartmentId);
            }
        } catch (Exception e) {
            LOG.warn("Получено исключение в ходе определения Налоговой формы и Тербанка блокировки " + lockKey);
            return null;
        }
        return null;
    }

    /**
     * Определение Тербанка пользователя, поставившего блокировку.
     */
    private Integer getLockCreatorTerbankId(String lockKey) {
        LockData lock = findLock(lockKey);
        int creatorId = lock.getUserId();
        TAUser taskCreator = userService.getUser(creatorId);
        return departmentService.getParentTBId(taskCreator.getDepartmentId());
    }
}