package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Сводные данные об обязательствах плательщика страховых взносов
 */
public class RaschsvObyazPlatSv extends IdentityObject<Long> {

    private Long declarationDataId;
    private String oktmo;

    public RaschsvObyazPlatSv() {
        super();
    }

    public static final String SEQ = "seq_raschsv_obyaz_plat_sv";
    public static final String TABLE_NAME = "raschsv_obyaz_plat_sv";
    public static final String COL_ID = "id";
    public static final String COL_DECLARATION_DATA_ID = "declaration_data_id";
    public static final String COL_OKTMO = "oktmo";

    public static final String[] COLUMNS = {COL_ID, COL_DECLARATION_DATA_ID, COL_OKTMO};

    public Long getDeclarationDataId() { return declarationDataId; }
    public void setDeclarationDataId(Long declarationDataId) { this.declarationDataId = declarationDataId; }

    public String getOktmo() { return oktmo; }
    public void setOktmo(String oktmo) { this.oktmo = oktmo; }
}
