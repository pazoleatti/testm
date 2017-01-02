package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, по месяцу и коду категории застрахованного лица
 */
public class RaschsvSvVyplMt extends IdentityObject<Long> {

    private Long raschsvSvVyplId;
    private String mesyac;
    private String kodKatLic;
    private Double sumVypl;
    private Double vyplOps;
    private Double vyplOpsDog;
    private Double nachislSv;

    public static final String SEQ = "seq_raschsv_sv_vypl_mk";
    public static final String TABLE_NAME = "raschsv_sv_vypl_mk";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_SV_VYPL_ID = "raschsv_sv_vypl_id";
    public static final String COL_MESYAC = "mesyac";
    public static final String COL_KOD_KAT_LIC = "kod_kat_lic";
    public static final String COL_SUM_VYPL = "sum_vypl";
    public static final String COL_VYPL_OPS = "vypl_ops";
    public static final String COL_VYPL_OPS_DOG = "vypl_ops_dog";
    public static final String COL_NACHISL_SV = "nachisl_sv";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_SV_VYPL_ID, COL_MESYAC, COL_KOD_KAT_LIC, COL_SUM_VYPL,
            COL_VYPL_OPS, COL_VYPL_OPS_DOG, COL_NACHISL_SV
    };

    public Long getRaschsvSvVyplId() { return raschsvSvVyplId; }
    public void setRaschsvSvVyplId(Long raschsvSvVyplId) { this.raschsvSvVyplId = raschsvSvVyplId; }

    public String getMesyac() { return mesyac; }
    public void setMesyac(String mesyac) { this.mesyac = mesyac; }

    public String getKodKatLic() { return kodKatLic; }
    public void setKodKatLic(String kodKatLic) { this.kodKatLic = kodKatLic; }

    public Double getSumVypl() { return sumVypl; }
    public void setSumVypl(Double sumVypl) { this.sumVypl = sumVypl; }

    public Double getVyplOps() { return vyplOps; }
    public void setVyplOps(Double vyplOps) { this.vyplOps = vyplOps; }

    public Double getVyplOpsDog() { return vyplOpsDog; }
    public void setVyplOpsDog(Double vyplOpsDog) { this.vyplOpsDog = vyplOpsDog; }

    public Double getNachislSv() { return nachislSv; }
    public void setNachislSv(Double nachislSv) { this.nachislSv = nachislSv; }
}
