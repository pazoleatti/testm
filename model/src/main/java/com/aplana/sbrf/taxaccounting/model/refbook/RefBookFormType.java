package com.aplana.sbrf.taxaccounting.model.refbook;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Виды налоговых форм
 *
 * @author dloshkarev
 */
@Getter
@Setter
@NoArgsConstructor
public class RefBookFormType extends RefBookSimple<Long> {

    //РНУ_НДФЛ
    public final static RefBookFormType NDFL_PRIMARY = new RefBookFormType(2);

    //2-НДФЛ (1)
    public final static RefBookFormType NDFL_2_1 = new RefBookFormType(3);

    //2-НДФЛ (2)
    public final static RefBookFormType NDFL_2_2 = new RefBookFormType(4);

    //6-НДФЛ
    public final static RefBookFormType NDFL_6 = new RefBookFormType(5);

    //Приложение 2
    public final static RefBookFormType APPLICATION_2 = new RefBookFormType(6); //todo 6?

    public RefBookFormType(long id) {
        super(id);
    }

    private String name;

    private String code;
}
