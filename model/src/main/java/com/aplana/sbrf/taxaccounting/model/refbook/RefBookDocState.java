package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Состояние ЭД
 */
@Getter
@Setter
@NoArgsConstructor
public class RefBookDocState extends RefBookSimple<Long> {
    public final static RefBookDocState NOT_SENT = new RefBookDocState(1, "Не отправлен в ФНС");
    public final static RefBookDocState EXPORTED = new RefBookDocState(2, "Выгружен для отправки в ФНС");
    public final static RefBookDocState ACCEPTED = new RefBookDocState(3, "Принят");
    public final static RefBookDocState REJECTED = new RefBookDocState(4, "Отклонен");
    public final static RefBookDocState WORKED_OUT = new RefBookDocState(5, "Успешно отработан");
    public final static RefBookDocState REQUIRES_CLARIFICATION = new RefBookDocState(6, "Требует уточнения");
    public final static RefBookDocState ERROR = new RefBookDocState(7, "Ошибка");
    public final static RefBookDocState SENDING_TO_EDO = new RefBookDocState(8, "Отправка в ЭДО");
    public final static RefBookDocState SENT_TO_EDO = new RefBookDocState(9, "Отправлен в ЭДО");
    public final static RefBookDocState NOT_SENT_TO_NP = new RefBookDocState(10, "Не отправлен в НП");
    public final static RefBookDocState EXPORTED_TO_NP = new RefBookDocState(11, "Выгружен для отправки в НП");
    public final static RefBookDocState UPLOADED_TO_NP = new RefBookDocState(12, "Загружен в НП");

    //Наименование состояния
    private String name;
    //Код формы по КНД
    private String knd;

    public RefBookDocState(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
