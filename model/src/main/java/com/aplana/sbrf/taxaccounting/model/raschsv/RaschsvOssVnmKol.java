package com.aplana.sbrf.taxaccounting.model.raschsv;

/**
 * Количество для расчета сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством
 */
public class RaschsvOssVnmKol {

    private Long raschsvOssVnmId;
    private String nodeName;

    // Сведения по количеству физических лиц
    private RaschsvKolLicTip raschsvKolLicTip;

    public static final String TABLE_NAME = "raschsv_oss_vnm_kol";
    public static final String COL_RASCHSV_OSS_VNM_ID = "raschsv_oss_vnm_id";
    public static final String COL_RASCHSV_KOL_LIC_TIP_ID = "raschsv_kol_lic_tip_id";
    public static final String COL_NODE_NAME = "node_name";

    public static final String[] COLUMNS = {COL_RASCHSV_OSS_VNM_ID, COL_RASCHSV_KOL_LIC_TIP_ID, COL_NODE_NAME};

    public Long getRaschsvOssVnmId() {
        return raschsvOssVnmId;
    }
    public void setRaschsvOssVnmId(Long raschsvOssVnmId) {
        this.raschsvOssVnmId = raschsvOssVnmId;
    }

    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public RaschsvKolLicTip getRaschsvKolLicTip() {
        return raschsvKolLicTip;
    }
    public void setRaschsvKolLicTip(RaschsvKolLicTip raschsvKolLicTip) {
        this.raschsvKolLicTip = raschsvKolLicTip;
    }
}
