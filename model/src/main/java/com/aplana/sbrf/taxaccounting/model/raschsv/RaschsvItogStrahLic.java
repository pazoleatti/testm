package com.aplana.sbrf.taxaccounting.model.raschsv;

/**
 * НФ вида 1151111: Сводные показатели формы
 */
public class RaschsvItogStrahLic {

    /**
     * Уникальный идентификатор
     */
    private Long id;

    /**
     * Идентификатор декларации
     */
    private Long declarationDataId;

    /**
     * Количество ФЛ
     */
    private Long kolLic;

    public static final String SEQ = "seq_raschsv_kol_lic_tip";
    public static final String TABLE_NAME = "RASCHSV_ITOG_STRAH_LIC";
    public static final String COL_ID = "ID";
    public static final String COL_DECLARATION_DATA_ID = "DECLARATION_DATA_ID";
    public static final String COL_KOL_LIC = "KOL_LIC";

    public static final String[] COLUMNS = {
            COL_ID,
            COL_DECLARATION_DATA_ID,
            COL_KOL_LIC
    };

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(Long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public Long getKolLic() {
        return kolLic;
    }

    public void setKolLic(Long kolLic) {
        this.kolLic = kolLic;
    }
}
