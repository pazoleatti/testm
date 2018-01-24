package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
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
                            return lock;
                        }
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
                            return lock;
                        }
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
    public LockData getLock(String key) {
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
                                        "пользователем \"%s\"(%s).", blocker.getLogin(), blocker.getId()));
                            }
                            dao.unlock(key);
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
        dao.unlockAllByUserId(userInfo.getUser().getId(), ignoreError);
    }

    @Override
    public boolean isLockExists(final String key, boolean like) {
        synchronized (LockDataServiceImpl.class) {
            return dao.get(key, like) != null;
        }
    }

    @Override
    public PagingResult<LockDataItem> getLocks(String filter, PagingParams pagingParams) {
        PagingResult<LockData> lockDataList = dao.getLocks(filter, pagingParams);
        PagingResult<LockDataItem> result = new PagingResult<LockDataItem>();
        for (LockData lockData : lockDataList) {
            LockDataItem item = new LockDataItem();
            item.setDateLock(lockData.getDateLock());
            item.setDescription(lockData.getDescription());
            item.setKey(lockData.getKey());
            item.setId(lockData.getId());
            TAUser user = userDao.getUser(lockData.getUserId());
            item.setUser(TAUser.SYSTEM_USER_ID != user.getId() ? user.getName() + " (" + user.getLogin() + ")" : user.getName());
            result.add(item);
        }
        return result;
    }

    @Override
    public void unlockAll(TAUserInfo userInfo, List<String> keys) {
        for (String key : keys) {
            unlock(key, userInfo.getUser().getId(), userInfo.getUser().hasRole(TARole.ROLE_ADMIN));
        }
    }

    private void auditLockDeletion(TAUserInfo userInfo, LockData lockData, TaskInterruptCause cause) {
        try {
            String note = cause.getEventDescrition(lockData.getDateLock(), userDao.getUser(lockData.getUserId()), lockData.getDescription());
            auditService.add(FormDataEvent.DELETE_LOCK, userInfo, null, null, null, null, null, note, null);
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
                    dao.bindTask(lockKey, taskId);
                }
                return null;
            }
        });
    }

    @Override
    public List<LockDataItem> fetchAllByKeySet(Set<String> keysBlocker) {
        List<LockData> lockDataList = dao.fetchAllByKeySet(keysBlocker);
        List<LockDataItem> lockDataItems = new ArrayList<>(lockDataList.size());
        for(LockData lockData: lockDataList){
            LockDataItem lockDataItem = new LockDataItem();
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