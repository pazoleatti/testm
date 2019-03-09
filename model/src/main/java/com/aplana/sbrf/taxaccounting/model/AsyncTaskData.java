package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Данные конкретной асинхронной задачи
 *
 * @author dloshkarev
 */
@Getter @Setter @ToString
public class AsyncTaskData {
    private long id;
    /* Тип задачи - хранит класс-исполнитель задачи */
    private AsyncTaskType type;
    /* Идентификатор пользователя, запустившего задачу*/
    private int userId;
    /* Дата создания/помещения в очередь задачи */
    private Date createDate;
    /* Описание задачи */
    private String description;
    /* Состояние задачи */
    private AsyncTaskState state;
    /**
     * Название узла
     */
    private String node;
    /**
     * Параметры для выполнения конкретной задачи
     */
    private Map<String, Object> params = new HashMap<String, Object>(0);

}
