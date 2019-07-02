package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.json.JsonDeclarationFormKindDeserializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Типы налоговых форм(declaration)
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonDeserialize(using = JsonDeclarationFormKindDeserializer.class)
public enum DeclarationFormKind {
    /**
     * Выходная форма
     * (содержит информацию, которая должна попасть в декларацию, но отсутствует в сводных формах)
     */
    ADDITIONAL(1, "Выходная"),
    /**
     * Консолидированная НФ
     */
    CONSOLIDATED(2, "Консолидированная"),
    /**
     * Первичная НФ
     */
    PRIMARY(3, "Первичная"),
    /**
     * Сводная НФ
     */
    SUMMARY(4, "Сводная"),
    /**
     * Форма УНП
     * (используется для проверки и корректировки данных в декларациях по налогу на прибыль)
     */
    UNP(5, "Форма УНП"),
    /**
     * Расеетная форма
     */
    CALCULATED(6, "Расчетная"),
    /**
     * Отчетная НФ(declaration)
     */
    REPORTS(7, "Отчетная"),
    /**
     * Отчетная ФЛ
     */
    REPORTS_FL(8, "Отчетная ФЛ");

    private final long id;
    private final String name;

    private DeclarationFormKind(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    /**
     * @deprecated см {@link #getName()}
     */
    @Deprecated
    public String getTitle() {
        return name;
    }

    public String getName() {
        return name;
    }

    public static DeclarationFormKind fromId(long kindId) {
        for (DeclarationFormKind kind : values()) {
            if (kind.id == kindId) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Wrong DeclarationFormKind id: " + kindId);
    }

    public static DeclarationFormKind fromName(String name) {
        for (DeclarationFormKind kind : values()) {
            if (kind.name.equals(name)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Wrong DeclarationFormKind id: " + name);
    }
}
