package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.Date;

/**
 * Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 3 пункта 1 статьи 427 (ПравТариф3.1.427)
 */
public class RaschsvPravTarif31427 extends IdentityObject<Long> {

    private Long raschsvObyazPlatSvId;
    private Integer srChisl9mpr;
    private Integer srChislPer;
    private Long doh2489mpr;
    private Long doh248Per;
    private Long dohKr54279mpr;
    private Long dohKr5427Per;
    private Double dohDoh54279mpr;
    private Double dohDoh5427per;
    private Date dataZapAkOrg;
    private String nomZapAkOrg;

    public static final String SEQ = "seq_raschsv_prav_tarif3_1_427";
    public static final String TABLE_NAME = "raschsv_prav_tarif3_1_427";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_OBYAZ_PLAT_SV_ID = "raschsv_obyaz_plat_sv_id";
    public static final String COL_SR_CHISL_9MPR = "sr_chisl_9mpr";
    public static final String COL_SR_CHISL_PER = "sr_chisl_per";
    public static final String COL_DOH248_9MPR = "doh248_9mpr";
    public static final String COL_DOH248_PER = "doh248_per";
    public static final String COL_DOH_KR5_427_9MPR = "doh_kr5_427_9mpr";
    public static final String COL_DOH_KR5_427_PER = "doh_kr5_427_per";
    public static final String COL_DOH_DOH5_427_9MPR = "doh_doh5_427_9mpr";
    public static final String COL_DOH_DOH5_427_PER = "doh_doh5_427_per";
    public static final String COL_DATA_ZAP_AK_ORG = "data_zap_ak_org";
    public static final String COL_NOM_ZAP_AK_ORG = "nom_zap_ak_org";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_OBYAZ_PLAT_SV_ID, COL_SR_CHISL_9MPR, COL_SR_CHISL_PER,
            COL_DOH248_9MPR, COL_DOH248_PER, COL_DOH_KR5_427_9MPR, COL_DOH_KR5_427_PER, COL_DOH_DOH5_427_9MPR,
            COL_DOH_DOH5_427_PER, COL_DATA_ZAP_AK_ORG, COL_NOM_ZAP_AK_ORG};

    public Long getRaschsvObyazPlatSvId() { return raschsvObyazPlatSvId; }
    public void setRaschsvObyazPlatSvId(Long raschsvObyazPlatSvId) { this.raschsvObyazPlatSvId = raschsvObyazPlatSvId; }

    public Integer getSrChisl9mpr() {
        return srChisl9mpr;
    }
    public void setSrChisl9mpr(Integer srChisl9mpr) {
        this.srChisl9mpr = srChisl9mpr;
    }

    public Integer getSrChislPer() {
        return srChislPer;
    }
    public void setSrChislPer(Integer srChislPer) {
        this.srChislPer = srChislPer;
    }

    public Long getDoh2489mpr() {
        return doh2489mpr;
    }
    public void setDoh2489mpr(Long doh2489mpr) {
        this.doh2489mpr = doh2489mpr;
    }

    public Long getDoh248Per() {
        return doh248Per;
    }
    public void setDoh248Per(Long doh248Per) {
        this.doh248Per = doh248Per;
    }

    public Long getDohKr54279mpr() {
        return dohKr54279mpr;
    }
    public void setDohKr54279mpr(Long dohKr54279mpr) {
        this.dohKr54279mpr = dohKr54279mpr;
    }

    public Long getDohKr5427Per() {
        return dohKr5427Per;
    }
    public void setDohKr5427Per(Long dohKr5427Per) {
        this.dohKr5427Per = dohKr5427Per;
    }

    public Double getDohDoh54279mpr() {
        return dohDoh54279mpr;
    }
    public void setDohDoh54279mpr(Double dohDoh54279mpr) {
        this.dohDoh54279mpr = dohDoh54279mpr;
    }

    public Double getDohDoh5427per() {
        return dohDoh5427per;
    }
    public void setDohDoh5427per(Double dohDoh5427per) {
        this.dohDoh5427per = dohDoh5427per;
    }

    public Date getDataZapAkOrg() {
        return dataZapAkOrg;
    }
    public void setDataZapAkOrg(Date dataZapAkOrg) {
        this.dataZapAkOrg = dataZapAkOrg;
    }

    public String getNomZapAkOrg() {
        return nomZapAkOrg;
    }
    public void setNomZapAkOrg(String nomZapAkOrg) {
        this.nomZapAkOrg = nomZapAkOrg;
    }
}
