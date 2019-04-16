package com.aplana.sbrf.taxaccounting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Абстрактный класс, представляющий объект, сохраняемый в БД и имеющий идентификатор
 * Этот класс должен являться базовым для классов, на которые предполагается накладывать блокировки
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class IdentityObject<IdType extends Number> implements Serializable {
    private static final long serialVersionUID = 3614498773660756556L;

    protected IdType id;
}
