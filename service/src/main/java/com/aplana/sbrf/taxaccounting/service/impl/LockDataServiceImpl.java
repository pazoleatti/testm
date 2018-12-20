package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.LockDataDTO;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaskInterruptCause;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 17:32
 */

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
    AuditService auditService;

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
                            dao.unlock(key);
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
    public boolean isLockExists(final String key, boolean like) {
        synchronized (LockDataServiceImpl.class) {
            return dao.get(key, like) != null;
        }
    }

    @Override
    public PagingResult<LockDataDTO> getLocks(String filter, PagingParams pagingParams, TAUser user) {
        return dao.getLocks(filter, pagingParams, user);
    }

    @Override
    public void unlockAll(TAUserInfo userInfo, List<String> keys) {
        boolean unlockForced = userInfo.getUser().hasRole(TARole.ROLE_ADMIN);
        for (String key : keys) {
            unlock(key, userInfo.getUser().getId(), unlockForced);
        }
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

    @Override
    public void unlockAllByTask(long taskId) {
        dao.unlockAllByTask(taskId);
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
     * Блокировка без всяких проверок - позволяет сократить количество обращений к бд для вложенных вызовов методов
     */
    private void internalLock(String key, int userId, String description) {
        dao.lock(key, userId, description);
    }
}