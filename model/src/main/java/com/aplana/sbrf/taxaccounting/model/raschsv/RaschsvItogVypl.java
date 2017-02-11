package com.aplana.sbrf.taxaccounting.model.raschsv;

import java.math.BigDecimal;

/**
 * Сводные сведения о выплатах
 */
public class RaschsvItogVypl {

    /**
     * Уникальный идентификатор
     */
    private Long id;

    /**
     * Внешний ключ на НФ вида 1151111: Сводные показатели формы
     */
    private Long raschsvItogStrahLicId;

    /**
     * Месяц
     */
    private String mesyac;

    /**
     * Категория
     */
    private String kodKatLic;

    /**
     * Количество ФЛ
     */
    private Long kolFl;

    /**
     * Сумма выплат
     */
    private BigDecimal sumVypl;

    /**
     * База ОПС
     */
    private BigDecimal vyplOps;

    /**
     * База ОПС Договор
     */
    private BigDecimal vyplOpsDog;

    /**
     * Сумма начислено
     */
    private BigDecimal sumNachisl;

    public static final String SEQ = "seq_raschsv_kol_lic_tip";
    public static final String TABLE_NAME = "RASCHSV_ITOG_VYPL";
    public static final String COL_ID = "ID";
    public static final String COL_RASCHSV_ITOG_STRAH_LIC_ID = "RASCHSV_ITOG_STRAH_LIC_ID";
    public static final String COL_MESYAC = "MESYAC";
    public static final String COL_KOD_KAT_LIC = "KOD_KAT_LIC";
    public static final String COL_KOL_FL = "KOL_FL";
    public static final String COL_SUM_VYPL = "SUM_VYPL";
    public static final String COL_VYPL_OPS = "VYPL_OPS";
    public static final String COL_VYPL_OPS_DOG = "VYPL_OPS_DOG";
    public static final String COL_SUM_NACHISL = "SUM_NACHISL";

    public static final String[] COLUMNS = {
            COL_ID,
            COL_RASCHSV_ITOG_STRAH_LIC_ID,
            COL_MESYAC,
            COL_KOD_KAT_LIC,
            COL_KOL_FL,
            COL_SUM_VYPL,
            COL_VYPL_OPS,
            COL_VYPL_OPS_DOG,
            COL_SUM_NACHISL
    };

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRaschsvItogStrahLicId() {
        return raschsvItogStrahLicId;
    }

    public void setRaschsvItogStrahLicId(Long raschsvItogStrahLicId) {
        this.raschsvItogStrahLicId = raschsvItogStrahLicId;
    }

    public String getMesyac() {
        return mesyac;
    }

    public void setMesyac(String mesyac) {
        this.mesyac = mesyac;
    }

    public String getKodKatLic() {
        return kodKatLic;
    }

    public void setKodKatLic(String kodKatLic) {
        this.kodKatLic = kodKatLic;
    }

    public Long getKolFl() {
        return kolFl;
    }

    public void setKolFl(Long kolFl) {
        this.kolFl = kolFl;
    }

    public BigDecimal getSumVypl() {
        return sumVypl;
    }

    public void setSumVypl(BigDecimal sumVypl) {
        this.sumVypl = sumVypl;
    }

    public BigDecimal getVyplOps() {
        return vyplOps;
    }

    public void setVyplOps(BigDecimal vyplOps) {
        this.vyplOps = vyplOps;
    }

    public BigDecimal getVyplOpsDog() {
        return vyplOpsDog;
    }

    public void setVyplOpsDog(BigDecimal vyplOpsDog) {
        this.vyplOpsDog = vyplOpsDog;
    }

    public BigDecimal getSumNachisl() {
        return sumNachisl;
    }

    public void setSumNachisl(BigDecimal sumNachisl) {
        this.sumNachisl = sumNachisl;
    }

    @Override
    public String toString() {
        return "RaschsvItogVypl{" +
                "id=" + id +
                ", raschsvItogStrahLicId=" + raschsvItogStrahLicId +
                ", mesyac='" + mesyac + '\'' +
                ", kodKatLic='" + kodKatLic + '\'' +
                ", kolFl=" + kolFl +
                ", sumVypl=" + sumVypl +
                ", vyplOps=" + vyplOps +
                ", vyplOpsDog=" + vyplOpsDog +
                ", sumNachisl=" + sumNachisl +
                '}';
    }
}
