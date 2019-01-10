package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Класс для отображения данных асинхронных задач на клиенте
 */
@Getter
@Setter
@ToString
public class AsyncTaskDTO {
    private long id;
    /* Идентификатор пользователя, запустившего задачу*/
    private String user;
    /* Дата создания/помещения в очередь задачи */
    private Date createDate;
    /* Узел кластера (название машины), на котором выполняется задача */
    private String node;
    /* Описание задачи */
    private String description;
    /* Состояние задачи */
    private String state;
    /* Дата последнего изменения состояния задачи */
    private Date stateDate;
    /* Очередь, в которой находится связанная асинхронная задача */
    private String queue;
    /* Положение задачи в очереди */
    private int queuePosition;
    /* Обладает ли запрашивающий пользователь правами для остановки задачи */
    private boolean allowedToInterrupt;
}
