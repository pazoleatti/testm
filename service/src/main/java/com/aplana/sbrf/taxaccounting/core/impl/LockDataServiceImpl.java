package com.aplana.sbrf.taxaccounting.core.impl;

import com.aplana.sbrf.taxaccounting.core.api.ServerInfo;
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

    private static final Log LOG = LogFactory.getLog(LockDataServiceImpl.class);
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
    @Autowired
    private ServerInfo serverInfo;

	@Override
	public LockData lock(final String key, final int userId, final String description) {
        return tx.executeInNewTransaction(new TransactionLogic<LockData>() {
            @Override
            public LockData execute() {
				try {
					synchronized(LockDataServiceImpl.class) {
						LockData lock = dao.get(key, false);
						if (lock != null) {
							return lock;
						}
						internalLock(key, userId, description, null, serverInfo.getServerName());
					}
					return null;
				} catch (Exception e) {
					throw new ServiceException("Не удалось установить блокировку объекта", e);
				}
			}
        });
	}

    @Override
    public LockData lock(final String key, final int userId, final String description, final String state) {
        return tx.executeInNewTransaction(new TransactionLogic<LockData>() {
            @Override
            public LockData execute() {
                try {
                    synchronized(LockDataServiceImpl.class) {
                        LockData lock = dao.get(key, false);
                        if (lock != null) {
                            return lock;
                        }
                        internalLock(key, userId, description, state, serverInfo.getServerName());
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
        synchronized(LockDataServiceImpl.class) {
            return dao.get(key, false);
        }
    }

    @Override
    public List<LockData> getLockStartsWith(String key) {
        return dao.getStartsWith(key);
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
                    synchronized(LockDataServiceImpl.class) {
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
    public void unlockAll(TAUserInfo userInfo) {
        dao.unlockAllByUserId(userInfo.getUser().getId(), false);
    }

    @Override
    public void unlockAll(TAUserInfo userInfo, boolean ignoreError) {
        dao.unlockAllByUserId(userInfo.getUser().getId(), ignoreError);
    }

    @Override
    public boolean isLockExists(final String key, boolean like) {
        synchronized(LockDataServiceImpl.class) {
            return dao.get(key, like) != null;
        }
    }

    @Override
    public boolean isLockExists(final String key, final Date lockDate) {
        return tx.executeInNewTransaction(new TransactionLogic<Boolean>() {
            @Override
            public Boolean execute() {
                synchronized (LockDataServiceImpl.class) {
                    return dao.get(key, lockDate) != null;
                }
            }
        });
    }

    @Override
    public void addUserWaitingForLock(final String key, final int userId) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public Object execute() {
                try {
                    synchronized(LockDataServiceImpl.class) {
                        LockData lock = dao.get(key, false);
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
    public PagingResult<LockData> getLocks(String filter, LockData.LockQueues queues, PagingParams pagingParams) {
        return dao.getLocks(filter, queues, pagingParams);
    }

    @Override
    public void unlockAll(List<String> keys) {
        dao.unlockAll(keys);
    }

    @Override
    public void updateState(final String key, final Date lockDate, final String state) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public Object execute() {
                try {
                    synchronized (LockDataServiceImpl.class) {
                        dao.updateState(key, lockDate, state, serverInfo.getServerName());
                    }
                } catch (ServiceException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceException("Не удалось обновить статус блокировки объекта", e);
                }
				return null;
            }
        });
    }

    @Override
    public void updateQueue(final String key, final Date lockDate, final LockData.LockQueues queue) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public Object execute() {
                try {
                    synchronized (LockDataServiceImpl.class) {
                        dao.updateQueue(key, lockDate, queue);
                    }
                } catch (ServiceException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServiceException("Не удалось обновить статус блокировки объекта", e);
                }
				return null;
            }
        });
    }

	@Override
	public int unlockIfOlderThan(long seconds) {
		return dao.unlockIfOlderThan(seconds);
	}

	/**
	 * Блокировка без всяких проверок - позволяет сократить количество обращений к бд для вложенных вызовов методов
	 */
	private void internalLock(String key, int userId, String description, String state, String serverNode) {
		dao.lock(key, userId, description, state, serverNode);
	}

    @Override
    public void interruptTask(final LockData lockData, final int userId, final boolean force, final String cause) {
        if (lockData != null) {
            LOG.info(String.format("Останавливается асинхронная задача с ключом %s", lockData.getKey()));
            tx.executeInNewTransaction(new TransactionLogic() {
				   @Override
				   public Object execute() {
					   try {
						   TAUser user = userDao.getUser(userId);
						   List<Integer> waitingUsers = getUsersWaitingForLock(lockData.getKey());
                           if (!waitingUsers.contains(userId))
                               waitingUsers.add(userId);
						   unlock(lockData.getKey(), userId, force);
						   //asyncInterruptionManager.interruptAll(Arrays.asList(lockData.getKey()));
						   String msg = String.format(LockData.CANCEL_TASK, user.getName(), lockData.getDescription(), cause);
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
					   return null;
				   }
			   }
            );
        }
    }

    @Override
    public void interruptAllTasks(List<String> lockKeys, int userId, String cause) {
        for (String key : lockKeys) {
            interruptTask(getLock(key), userId, true, cause);
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