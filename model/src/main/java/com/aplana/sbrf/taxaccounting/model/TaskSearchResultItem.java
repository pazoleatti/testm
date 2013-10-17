package com.aplana.sbrf.taxaccounting.model;


import java.io.Serializable;

/**
 * DTO-Класс, содержащий информацию о параметрах задач планировщика
 * @author dloshkarev
 */
public class TaskSearchResultItem implements Serializable {
    private static final long serialVersionUID = -2958384670394988526L;

    /** Идентификатор задачи */
    private Long id;

    /** Название задачи */
    private String name;

    /** Состояние задачи */
    private String state;

    /** Количество повторений задачи планировщиком */
    private int numberOfRepeats;

    /** Количество выполненных повторений задачи */
    private Integer repeatsLeft;

    /** Дата создания задачи */
    private String timeCreated;

    /** Дата следующего запуска задачи */
    private String nextFireTime;

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getNumberOfRepeats() {
        return numberOfRepeats;
    }

    public void setNumberOfRepeats(int numberOfRepeats) {
        this.numberOfRepeats = numberOfRepeats;
    }

    public Integer getRepeatsLeft() {
        return repeatsLeft;
    }

    public void setRepeatsLeft(Integer repeatsLeft) {
        this.repeatsLeft = repeatsLeft;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(String nextFireTime) {
        this.nextFireTime = nextFireTime;
    }
}
