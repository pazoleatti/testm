package com.aplana.sbrf.taxaccounting.async.persistence;

import com.aplana.sbrf.taxaccounting.async.entity.AsyncTaskTypeEntity;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskPersistenceException;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Local(AsyncTaskPersistenceServiceLocal.class)
@Stateless
public class AsyncTaskPersistenceServiceBean implements AsyncTaskPersistenceService {

    @PersistenceContext(unitName = "asyncPU")
    private EntityManager em;

    @Override
    public AsyncTaskTypeEntity getTaskTypeById(Long taskTypeId) throws AsyncTaskPersistenceException {
        Query query = em.createNamedQuery("AsyncTaskTypeEntity.findTaskTypeById").setParameter("taskTypeId", taskTypeId);
        try {
            return (AsyncTaskTypeEntity) query.getSingleResult();
        } catch (NoResultException e) {
            throw new AsyncTaskPersistenceException("Не найден тип асинхронной задачи с указанным идентификатором: " + taskTypeId);
        }
    }
}
