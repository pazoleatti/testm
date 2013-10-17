package com.aplana.sbrf.taxaccounting.scheduler.core.persistence;

import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskPersistenceException;
import com.aplana.sbrf.taxaccounting.scheduler.core.entity.TaskContextEntity;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.List;

@Local(TaskPersistenceServiceLocal.class)
@Stateless
public class TaskPersistenceServiceBean implements TaskPersistenceService {

    @PersistenceContext(unitName = "schedulerPU")
    private EntityManager em;

    @Override
    public void saveContext(TaskContextEntity context) throws TaskPersistenceException {
        try {
            Query query = em.createNativeQuery("select seq_task_context.nextval from dual");
            BigDecimal resultId = (BigDecimal) query.getSingleResult();
            context.setId(resultId.longValue());
            em.persist(context);
        } catch (Exception e) {
            throw new TaskPersistenceException("Не удалось сохранить контекст задачи", e);
        }
    }

    @Override
    public TaskContextEntity getContextByTaskId(Long taskId) throws TaskPersistenceException {
        Query query = em.createNamedQuery("TaskContextEntity.findContextByTaskId").setParameter("taskId", taskId);
        TaskContextEntity result = null;

        try {
            result = (TaskContextEntity) query.getSingleResult();
        } catch (NoResultException e) {
            throw new TaskPersistenceException("Не найден контекст для задачи с идентификатором " + taskId);
        }

        if (result.isCustomParamsExist()) {
            result.getSerializedParams();
        }
        return result;
    }

    @Override
    public List<TaskContextEntity> getAllContexts() throws TaskPersistenceException {
        Query query = em.createNamedQuery("TaskContextEntity.findAll");
        List<TaskContextEntity> result = query.getResultList();
        for (TaskContextEntity item : result) {
            if (item.isCustomParamsExist()) {
                item.getSerializedParams();
            }
        }
        return result;
    }

    @Override
    public void deleteContextByTaskId(Long taskId) throws TaskPersistenceException {
        try {
            Query query = em.createNamedQuery("TaskContextEntity.deleteContextByTaskId").setParameter("taskId", taskId);
            query.executeUpdate();
        } catch (Exception e) {
            throw new TaskPersistenceException("Не удалось удалить контекст задачи", e);
        }
    }
}
