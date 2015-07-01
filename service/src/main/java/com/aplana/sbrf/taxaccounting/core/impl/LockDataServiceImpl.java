package com.aplana.sbrf.taxaccounting.core.impl;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncInterruptionManager;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 17:32
 */

@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class LockDataServiceImpl implements LockDataService {
    private static final Log log = LogFactory.getLog(LockDataServiceImpl.class);

	private static final long SLEEP_TIME = 500; //шаг времени между проверками освобождения блокировки, миллисекунды

	@Autowired
	private LockDataDao dao;

	@Autowired
	private TAUserDao userDao;

    @Autowired
    AsyncInterruptionManager asyncInterruptionManager;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TransactionHelper tx;

	@Override
	public LockData lock(final String key, final int userId, final String description, final long age) {
        return tx.returnInNewTransaction(new TransactionLogic<LockData>() {
            @Override
            public LockData executeWithReturn() {
                try {
                    synchronized(LockDataServiceImpl.class) {
                        LockData lock = validateLock(dao.get(key, false));
                        if (lock != null) {
                            return lock;
                        }
                        internalLock(key, userId, age, description, null);
                    }
                    return null;
                } catch (Exception e) {
                    throw new ServiceException("Не удалось установить блокировку объекта", e);
                }
            }

            @Override
            public void execute() {}
        });
	}

    @Override
    public LockData lock(final String key, final int userId, final String description, final String state, final long age) {
        return tx.returnInNewTransaction(new TransactionLogic<LockData>() {
            @Override
            public LockData executeWithReturn() {
                try {
                    synchronized(LockDataServiceImpl.class) {
                        LockData lock = validateLock(dao.get(key, false));
                        if (lock != null) {
                            return lock;
                        }
                        internalLock(key, userId, age, description, state);
                    }
                    return null;
                } catch (Exception e) {
                    throw new ServiceException("Не удалось установить блокировку объекта", e);
                }
            }

            @Override
            public void execute() {}
        });
    }

    @Override
    public LockData getLock(String key) {
        synchronized(LockDataServiceImpl.class) {
            return validateLock(dao.get(key, false));
        }
    }

	@Override
	public void lockWait(final String key, final int userId, final long age, final String description, final long timeout) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                long startTime = new Date().getTime();
                while (lock(key, userId, description, age) != null) {
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                    }
                    if (Math.abs(new Date().getTime() - startTime) > timeout) {
                        throw new ServiceException(String.format("Время ожидания (%s мс) для установки блокировки истекло", timeout));
                    }
                }
            }

            @Override
            public Object executeWithReturn() {
                return null;
            }
        });
	}

	@Override
	public void unlock(final String key, final int userId) {
        unlock(key, userId, false);
    }

    @Override
    public void unlock(final String key, final int userId, final boolean force) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                try {
                    synchronized(LockDataServiceImpl.class) {
                        LockData lock = validateLock(dao.get(key, false));
                        if (lock != null) {
                            if (!force && lock.getUserId() != userId) {
                                TAUser blocker = userDao.getUser(lock.getUserId());
                                throw new ServiceException(String.format("Невозможно удалить блокировку, так как она установлена " +
                                        "пользователем \"%s\"(%s).", blocker.getLogin(), blocker.getId()));
                            }
                            dao.deleteLock(key);
                        } else if (!force) {
                            log.warn(String.format("Нельзя снять несуществующую блокировку. key = \"%s\"", key));
                        }
                    }
                } catch (ServiceException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceException("Не удалось снять блокировку с объекта", e);
                }
            }

            @Override
            public Object executeWithReturn() {
                return null;
            }
        });
	}

	@Override
	public void extend(final String key, final int userId, final long age) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                try {
                    synchronized(LockDataServiceImpl.class) {
                        LockData lock = dao.get(key, false);
                        String description = lock != null ? lock.getDescription() : null;
                        String state = lock != null ? lock.getState() : null;
                        lock = validateLock(lock);
                        if (lock != null) {
                            if (lock.getUserId() != userId) {
                                TAUser blocker = userDao.getUser(lock.getUserId());
                                throw new ServiceException(String.format("Невозможно продлить блокировку, так как она установлена " +
                                        "пользователем \"$s\"(id = %s). Текущий пользователь id = %s", blocker.getLogin(), blocker.getId()));
                            }
                            Date dateBefore = new Date();
                            dao.updateLock(key, new Date(dateBefore.getTime() + age));
                        } else {
                            internalLock(key, userId, age, description, state); // создаем блокировку, если ее не было
                        }
                    }
                } catch (ServiceException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceException("Не удалось продлить блокировку объекта", e);
                }
            }

            @Override
            public Object executeWithReturn() {
                return null;
            }
        });
	}

    @Override
    public void unlockAll(TAUserInfo userInfo) {
        dao.unlockAllByUserId(userInfo.getUser().getId(), false);
    }

    @Override
    public void unlockAll(TAUserInfo userInfo, boolean ignoreError) {
        dao.unlockAllByUserId(userInfo.getUser().getId(), ignoreError);
    }

    @Override
    public void unlockIfOlderThan(int sec) {
        dao.unlockIfOlderThan(sec);
    }

    @Override
    public boolean isLockExists(final String key, boolean like) {
        synchronized(LockDataServiceImpl.class) {
            return validateLock(dao.get(key, like)) != null;
        }
    }

    @Override
    public boolean isLockExists(String key, Date lockDate) {
        synchronized(LockDataServiceImpl.class) {
            return validateLock(dao.get(key, lockDate)) != null;
        }
    }

    @Override
    public void addUserWaitingForLock(final String key, final int userId) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                try {
                    synchronized(LockDataServiceImpl.class) {
                        LockData lock = validateLock(dao.get(key, false));
                        if (lock != null) {
                            dao.addUserWaitingForLock(key, userId);
                        } else {
                            throw new ServiceException(String.format("Нельзя ожидать несуществующий объект блокировки. key = \"%s\"", key));
                        }
                    }
                } catch (ServiceException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceException("Не удалось добавить пользователя в список ожидающих объект блокировки", e);
                }
            }

            @Override
            public Object executeWithReturn() {
                return null;
            }
        });
    }

    @Override
    public List<Integer> getUsersWaitingForLock(String key) {
        synchronized(LockDataServiceImpl.class) {
            try {
                return dao.getUsersWaitingForLock(key);
            } catch (Exception e) {
                throw new ServiceException("Не удалось получить список пользователей ожидающих объект блокировки", e);
            }
        }
    }

    @Override
    public int getLockTimeout(LockData.LockObjects lockObject) {
        try {
            return dao.getLockTimeout(lockObject);
        } catch (Exception e) {
            throw new ServiceException(String.format("Не удалось получить таймаут для блокировки объекта с ключом = %s",lockObject.name()), e);
        }
    }

    @Override
    public PagingResult<LockData> getLocks(String filter, LockData.LockQueues queues, PagingParams pagingParams) {
        return dao.getLocks(filter, queues, pagingParams);
    }

    @Override
    public void unlockAll(List<String> keys) {
        dao.unlockAll(keys);
    }

    @Override
    public void extendAll(List<String> keys, int hours) {
        dao.extendAll(keys, hours);
    }

    @Override
    public void updateState(final String key, final Date lockDate, final String state) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                try {
                    synchronized (LockDataServiceImpl.class) {
                        dao.updateState(key, lockDate, state);
                    }
                } catch (ServiceException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceException("Не удалось обновить статус блокировки объекта", e);
                }
            }

            @Override
            public Object executeWithReturn() {
                return null;
            }
        });
    }

    @Override
    public void updateQueue(final String key, final Date lockDate, final BalancingVariants queue) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                try {
                    synchronized (LockDataServiceImpl.class) {
                        dao.updateQueue(key, lockDate, queue);
                    }
                } catch (ServiceException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceException("Не удалось обновить статус блокировки объекта", e);
                }
            }

            @Override
            public Object executeWithReturn() {
                return null;
            }
        });
    }

    /**
	 * Проверяет блокировку. Если срок ее действия вышел, то она удаляется
	 */
	private LockData validateLock(LockData lock) {
		if (lock == null) {
			return null;
		}
		if (new Date().after(lock.getDateBefore())) {
			dao.deleteLock(lock.getKey());
			return null;
		}
		return lock;
	}

	/**
	 * Блокировка без всяких проверок - позволяет сократить количество обращений к бд для вложенных вызовов методов
	 */
	private void internalLock(String key, int userId, long age, String description, String state) {
		Date dateBefore = new Date();
		dao.createLock(key, userId, new Date(dateBefore.getTime() + age), description, state);
	}

    @Override
    public void interruptTask(final LockData lockData, final int userId, final boolean force) {
        if (lockData != null) {
            log.info(String.format("Останавливается асинхронная задача с ключом %s", lockData.getKey()));
            tx.executeInNewTransaction(new TransactionLogic() {
                                           @Override
                                           public void execute() {
                                               try {
                                                   TAUser user = userDao.getUser(userId);
                                                   List<Integer> waitingUsers = getUsersWaitingForLock(lockData.getKey());
                                                   unlock(lockData.getKey(), userId, force);
                                                   //asyncInterruptionManager.interruptAll(Arrays.asList(lockData.getKey()));
                                                   String msg = String.format(LockData.CANCEL_TASK, user.getName(), lockData.getDescription());
                                                   List<Notification> notifications = new ArrayList<Notification>();
                                                   //Создаем оповещение для каждого пользователя из списка
                                                   if (!waitingUsers.isEmpty()) {
                                                       for (Integer waitingUser : waitingUsers) {
                                                           Notification notification = new Notification();
                                                           notification.setUserId(waitingUser);
                                                           notification.setCreateDate(new Date());
                                                           notification.setText(msg);
                                                           notifications.add(notification);
                                                       }
                                                       notificationService.saveList(notifications);
                                                   }
                                               } catch (Exception e) {
                                                   throw new ServiceException("Не удалось прервать задачу", e);
                                               }
                                           }

                                           @Override
                                           public Object executeWithReturn() {
                                               return null;
                                           }
                                       }
            );
        }
    }

    @Override
    public void interuptAllTasks(List<String> lockKeys, int userId) {
        for (String key : lockKeys) {
            interruptTask(getLock(key), userId, true);
        }
    }

    public void lockInfo(LockData lockData, Logger logger) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm z");
        if (LockData.State.IN_QUEUE.getText().equals(lockData.getState())) {
            logger.info("\"%s\" пользователем \"%s\" запущена операция \"%s\"(статус \"%s\")",
                    formatter.format(lockData.getDateLock()),
                    userDao.getUser(lockData.getUserId()).getName(),
                    lockData.getDescription(),
                    lockData.getState());
        } else {
            logger.info("\"%s\" пользователем \"%s\" запущена операция \"%s\". Данная операция уже выполняется Системой(статус \"%s\")",
                    formatter.format(lockData.getDateLock()),
                    userDao.getUser(lockData.getUserId()).getName(),
                    lockData.getDescription(),
                    lockData.getState());
        }
    }
}