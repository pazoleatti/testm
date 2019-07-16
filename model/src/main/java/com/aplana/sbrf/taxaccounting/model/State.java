package com.aplana.sbrf.taxaccounting.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

/**
 * Стадии жизненного цикла налоговой формы(declaration)
 */
@Getter
public enum State {
    /**
     * Статус "Не создана"
     */
    NOT_EXIST(null, "Не создана"),
    /**
     * Статус "Создана"
     */
    CREATED(1, "Создана"),
    /**
     * Статус "Подготовлена"
     */
    PREPARED(2, "Подготовлена"),
    /**
     * Статус "Принята"
     */
    ACCEPTED(3, "Принята"),
    /**
     * Статус "Выдана"
     */
    ISSUED(4, "Выдана");

    private final Integer id;
    private final String title;

    State(Integer id, String title) {
        this.id = id;
        this.title = title;
    }

    @JsonCreator
    public static State fromId(Integer id) {
        if (id == null) {
            return NOT_EXIST;
        }
        for (State state : values()) {
            if (id.equals(state.id)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Wrong State id: " + id);
    }
}
