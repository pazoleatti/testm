package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.ObjectLockDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.LockException;
import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.ObjectLock;
import com.aplana.sbrf.taxaccounting.model.TAUser;

@Repository
@Transactional
public class ObjectLockDaoImpl extends AbstractDao implements ObjectLockDao{
	/**
	 * Период (в секундах), по истечении которого блокировка считается недействительной
	 */
	private final static int LOCK_TIMEOUT = 3600;
	
	@Autowired
	private TAUserDao userDao;
	
	private static class ObjectLockRowMapper<IdType extends Number> implements RowMapper<ObjectLock<IdType>> {
		@Override
		@SuppressWarnings("unchecked")
		public ObjectLock<IdType> mapRow(ResultSet rs, int index) throws SQLException {
			ObjectLock<IdType> lock = new ObjectLock<IdType>();
			lock.setObjectId(rs.getLong("object_id"));
			String className = rs.getString("class");
			try {
				Class<? extends IdentityObject<IdType>> clazz = (Class<? extends IdentityObject<IdType>>)Class.forName(className);
				lock.setObjectClass(clazz);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Failed to load class: " + className);
			}
			
			Timestamp lockTime = rs.getTimestamp("lock_time");
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(lockTime.getTime());
			lock.setLockTime(cal.getTime());
			lock.setUserId(rs.getInt("user_id"));
			return lock;
		}

	}
	
	@Override
	public <IdType extends Number> ObjectLock<IdType> getObjectLock(IdType id,	Class<? extends IdentityObject<IdType>> clazz) {
		return getObjectLock(id, clazz, false);
	}
	
	public <IdType extends Number> ObjectLock<IdType> getObjectLock(IdType id,	Class<? extends IdentityObject<IdType>> clazz, boolean forUpdate) {
		StringBuilder sql = new StringBuilder("select * from object_lock where object_id = ? and class = ?");
		if (forUpdate) {
			sql.append(" for update");
		}
		try {
			return getJdbcTemplate().queryForObject(
				sql.toString(), 
				new ObjectLockRowMapper<IdType>(),
				id,
				clazz.getName()
			);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public <IdType extends Number> void lockObject(IdType id, Class<? extends IdentityObject<IdType>> clazz, int userId) {
		ObjectLock<IdType> lock = getObjectLock(id, clazz, true);
		String className = clazz.getName();
		
		JdbcTemplate jt = getJdbcTemplate();

		Date currentTime = new Date();
		
		if (lock != null) {
			Date timeoutTime = getLockTimeoutTime(lock);
			
			if (timeoutTime.before(currentTime)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Object of type " + className + " with id = " + id + " was locked by user with id = " + userId + " but lock was timed out at " + timeoutTime);
				}
			} else if (lock.getUserId() == userId) {
				if (logger.isWarnEnabled()) {
					logger.warn("Object of type " + className + " with id = " + id + " is already locked by user with id = " + userId + ". Lock time will be updated to current time.");
				}
			} else {
				TAUser user = userDao.getUser(lock.getUserId());
				throw new LockException("Объект типа " + className + " с идентификатором " + id + " заблокирован пользователем " + user.getName() + "(id = " + user.getId() + ")");
			}
			
			jt.update(
				"update object_lock set lock_time = ?, user_id = ? where object_id = ? and class = ?",
				currentTime,
				userId,
				id,
				className
			);
		} else {
			try {
				jt.update(
					"insert into object_lock(object_id, class, lock_time, user_id) values (?, ?, ?, ?)",
					id,
					clazz.getName(),
					currentTime,
					userId
				);
			} catch (DataIntegrityViolationException e) {
				// Такое возможно если другая транзация вставит запись в таблицу сразу же после того,
				// как мы проверим отсутствие записи в таблице (вероятность такого события очень низка)
				throw new LockException("Не удалось заблокировать объект типа " + clazz.getName() + " с идентификатором " + id);
			}
		}		
	}
	
	private Date getLockTimeoutTime(ObjectLock<?> lock) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(lock.getLockTime());
		cal.add(Calendar.SECOND, LOCK_TIMEOUT);
		return cal.getTime();
	}

	@Override
	public <IdType extends Number> void unlockObject(IdType id, Class<? extends IdentityObject<IdType>> clazz, int userId) {
		String className = clazz.getName();		
		ObjectLock<IdType> lock = getObjectLock(id, clazz, true);
		if (lock == null) {
			throw new LockException("Невозможно разблокировать объект типа %s с идентификатором %d, так как он не заблокирован", className, id);
		} else {
			Date currentTime = new Date();
			Date timeoutTime = getLockTimeoutTime(lock);
			
			if (lock.getUserId() != userId) {			
				throw new LockException(
					"Невозможно разблокировать объект типа %s с идентификатором %d, так как он заблокирован другим пользователем",
					className,
					id
				);
			} else {
				if (currentTime.after(timeoutTime)) {
					if (logger.isWarnEnabled()) {
						logger.warn("Trying to unlock object with timed-out lock: className = " + className + ", id = " + id + ", lock_time = " + lock.getLockTime());
					}
				}
				getJdbcTemplate().update(
					"delete from object_lock where object_id = ? and class = ?",
					id,
					clazz.getName()
				);
				
			}
		}
	}
}
