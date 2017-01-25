package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.math.BigDecimal;

/**
 * Сумма страховых взносов, подлежащая к уплате (сумма превышения произведенных расходов над исчисленными страховыми взносами) (УплСВПрев)
 */
public class RaschsvUplSvPrev extends IdentityObject<Long> {

    private Long raschsvOssVnmId;
    private String nodeName;
    private String priznak;
    private BigDecimal svSum;

    public static final String SEQ = "seq_raschsv_upl_sv_prev";
    public static final String TABLE_NAME = "raschsv_upl_sv_prev";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_OSS_VNM_ID = "raschsv_oss_vnm_id";
    public static final String COL_NODE_NAME = "node_name";
    public static final String COL_PRIZNAK = "priznak";
    public static final String COL_SV_SUM = "sv_sum";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_OSS_VNM_ID, COL_NODE_NAME, COL_PRIZNAK, COL_SV_SUM};

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

    public String getPriznak() {
        return priznak;
    }
    public void setPriznak(String priznak) {
        this.priznak = priznak;
    }

    public BigDecimal getSvSum() {
        return svSum;
    }
    public void setSvSum(BigDecimal svSum) {
        this.svSum = svSum;
    }
}
