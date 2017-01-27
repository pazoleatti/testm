package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.math.BigDecimal;

/**
 * Сведения о сумме выплат и иных вознаграждений, исчисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу, по месяцу и коду тарифа (ВыплСВДопМТ)
 */
public class RaschsvVyplSvDopMt extends IdentityObject<Long> {

    private Long raschsvVyplSvDopId;
    private String mesyac;
    private String tarif;
    private BigDecimal vyplSv;
    private BigDecimal nachislSv;

    public static final String SEQ = "seq_raschsv_vypl_sv_dop_mt";
    public static final String TABLE_NAME = "raschsv_vypl_sv_dop_mt";

    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_VYPL_SV_DOP_ID = "raschsv_vypl_sv_dop_id";
    public static final String COL_MESYAC = "mesyac";
    public static final String COL_TARIF = "tarif";
    public static final String COL_VYPL_SV = "vypl_sv";
    public static final String COL_NACHISL_SV = "nachisl_sv";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_VYPL_SV_DOP_ID, COL_MESYAC, COL_TARIF,
            COL_VYPL_SV, COL_NACHISL_SV};

    public Long getRaschsvVyplSvDopId() { return raschsvVyplSvDopId; }
    public void setRaschsvVyplSvDopId(Long raschsvVyplSvDopId) { this.raschsvVyplSvDopId = raschsvVyplSvDopId; }

    public String getMesyac() { return mesyac; }
    public void setMesyac(String mesyac) { this.mesyac = mesyac; }

    public String getTarif() { return tarif; }
    public void setTarif(String tarif) { this.tarif = tarif; }

    public BigDecimal getVyplSv() { return vyplSv; }
    public void setVyplSv(BigDecimal vyplSv) { this.vyplSv = vyplSv; }

    public BigDecimal getNachislSv() { return nachislSv; }
    public void setNachislSv(BigDecimal nachislSv) { this.nachislSv = nachislSv; }
}
