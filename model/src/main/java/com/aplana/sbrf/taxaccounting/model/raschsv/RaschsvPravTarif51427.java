package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.math.BigDecimal;

/**
 * Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 5 пункта 1 статьи 427 (ПравТариф5.1.427)
 */
public class RaschsvPravTarif51427 extends IdentityObject<Long> {

    private Long raschsvObyazPlatSvId;
    private Long doh346_15vs;
    private Long doh6_427;
    private BigDecimal dolDoh6_427;

    public static final String SEQ = "seq_raschsv_prav_tarif5_1_427";
    public static final String TABLE_NAME = "raschsv_prav_tarif5_1_427";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_OBYAZ_PLAT_SV_ID = "raschsv_obyaz_plat_sv_id";
    public static final String COL_DOH346_15VS = "doh346_15vs";
    public static final String COL_DOH6_427 = "doh6_427";
    public static final String COL_DOL_DOH6_427 = "dol_doh6_427";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_OBYAZ_PLAT_SV_ID, COL_DOH346_15VS, COL_DOH6_427, COL_DOL_DOH6_427};

    public Long getRaschsvObyazPlatSvId() { return raschsvObyazPlatSvId; }
    public void setRaschsvObyazPlatSvId(Long raschsvObyazPlatSvId) { this.raschsvObyazPlatSvId = raschsvObyazPlatSvId; }

    public Long getDoh346_15vs() {
        return doh346_15vs;
    }
    public void setDoh346_15vs(Long doh346_15vs) {
        this.doh346_15vs = doh346_15vs;
    }

    public Long getDoh6_427() {
        return doh6_427;
    }
    public void setDoh6_427(Long doh6_427) {
        this.doh6_427 = doh6_427;
    }

    public BigDecimal getDolDoh6_427() {
        return dolDoh6_427;
    }
    public void setDolDoh6_427(BigDecimal dolDoh6_427) {
        this.dolDoh6_427 = dolDoh6_427;
    }
}
