package com.aplana.sbrf.taxaccounting.async.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Сущность представляющая типы асинхронных задач, связывающие их идентификаторы и классы-обработчики
 * @author dloshkarev
 */

@NamedQueries({
        @NamedQuery(
                name = "AsyncTaskTypeEntity.findTaskTypeById",
                query = "select c from AsyncTaskTypeEntity c where c.id = :taskTypeId"
        )
})
@Entity
@Table(name = "async_task_type")
public class AsyncTaskTypeEntity implements Serializable {
    private static final long serialVersionUID = 7542671868744372130L;

    /**
     * Идентификатор типа задачи
     */
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Имя типа задачи
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * JNDI класса-обработчика задачи
     */
    @Column(name = "handler_jndi", nullable = false)
    private String handlerJndi;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHandlerJndi() {
        return handlerJndi;
    }

    public void setHandlerJndi(String handlerJndi) {
        this.handlerJndi = handlerJndi;
    }
}
