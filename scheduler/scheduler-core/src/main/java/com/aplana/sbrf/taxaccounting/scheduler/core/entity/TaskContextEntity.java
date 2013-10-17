package com.aplana.sbrf.taxaccounting.scheduler.core.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Сущность контекста задачи для сохранения в бд
 *
 * @author dloshkarev
 */

@NamedQueries({
        @NamedQuery(
                name = "TaskContextEntity.findContextByTaskId",
                query = "select c from TaskContextEntity c where c.taskId = :taskId"
        ),
        @NamedQuery(
                name = "TaskContextEntity.deleteContextByTaskId",
                query = "delete from TaskContextEntity c where c.taskId = :taskId"
        ) ,
        @NamedQuery(
                name = "TaskContextEntity.findAll",
                query = "select c from TaskContextEntity c"
        )
})
@Entity
@Table(name = "task_context")
public class TaskContextEntity implements Serializable {
    private static final long serialVersionUID = 7542671868744372130L;

    /**
     * Идентификатор записи
     */
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Идентификатор задачи
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    /**
     * Имя задачи
     */
    @Column(name = "task_name", nullable = false)
    private String taskName;

    /**
     * JNDI класса-обработчика задачи
     */
    @Column(name = "user_task_jndi", nullable = false)
    private String userTaskJndi;

    /**
     * Признак наличия пользовательских параметров
     */
    @Column(name = "custom_params_exist", nullable = false)
    private Boolean isCustomParamsExist;

    /**
     * Сериализованный контекст задачи
     */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "serialized_params", nullable = true)
    private byte[] serializedParams;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getUserTaskJndi() {
        return userTaskJndi;
    }

    public void setUserTaskJndi(String userTaskJndi) {
        this.userTaskJndi = userTaskJndi;
    }

    public Boolean isCustomParamsExist() {
        return isCustomParamsExist;
    }

    public void setCustomParamsExist(Boolean customParamsExist) {
        isCustomParamsExist = customParamsExist;
    }

    public byte[] getSerializedParams() {
        return serializedParams;
    }

    public void setSerializedParams(byte[] serializedParams) {
        this.serializedParams = serializedParams;
    }
}
