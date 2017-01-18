package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Вид расчета (РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО)
 */
public class RaschsvSvOpsOmsRasch extends IdentityObject<Long> {

    private Long raschsvSvOpsOmsId;
    private String nodeName;
    private String prOsnSvDop;
    private String kodOsnov;
    private String osnovZap;
    private String klasUslTrud;
    private String prRaschSum;

    // Связь РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО и СвСум1Тип
    private List<RaschsvSvOpsOmsRaschSum> raschsvSvOpsOmsRaschSumList;

    // Связь РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО и КолЛицТип
    private List<RaschsvSvOpsOmsRaschKol> raschsvSvOpsOmsRaschKolList;

    public RaschsvSvOpsOmsRasch() {
        super();
        raschsvSvOpsOmsRaschSumList = new ArrayList<RaschsvSvOpsOmsRaschSum>();
        raschsvSvOpsOmsRaschKolList = new ArrayList<RaschsvSvOpsOmsRaschKol>();
    }

    public static final String SEQ = "seq_raschsv_sv_ops_oms_rasch";
    public static final String TABLE_NAME = "raschsv_sv_ops_oms_rasch";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_SV_OPS_OMS_ID = "raschsv_sv_ops_oms_id";
    public static final String COL_NODE_NAME = "node_name";
    public static final String COL_PR_OSN_SV_DOP = "pr_osn_sv_dop";
    public static final String COL_KOD_OSNOV = "kod_osnov";
    public static final String COL_OSNOV_ZAP = "osnov_zap";
    public static final String COL_KLAS_USL_TRUD = "klas_usl_trud";
    public static final String COL_PR_RASCH_SUM = "pr_rasch_sum";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_SV_OPS_OMS_ID, COL_NODE_NAME, COL_PR_OSN_SV_DOP,
            COL_KOD_OSNOV, COL_OSNOV_ZAP, COL_KLAS_USL_TRUD, COL_PR_RASCH_SUM
    };

    public Long getRaschsvSvOpsOmsId() { return raschsvSvOpsOmsId; }
    public void setRaschsvSvOpsOmsId(Long raschsvSvOpsOmsId) { this.raschsvSvOpsOmsId = raschsvSvOpsOmsId; }

    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getPrOsnSvDop() {
        return prOsnSvDop;
    }
    public void setPrOsnSvDop(String prOsnSvDop) {
        this.prOsnSvDop = prOsnSvDop;
    }

    public String getKodOsnov() {
        return kodOsnov;
    }
    public void setKodOsnov(String kodOsnov) {
        this.kodOsnov = kodOsnov;
    }

    public String getOsnovZap() {
        return osnovZap;
    }
    public void setOsnovZap(String osnovZap) {
        this.osnovZap = osnovZap;
    }

    public String getKlasUslTrud() {
        return klasUslTrud;
    }
    public void setKlasUslTrud(String klasUslTrud) {
        this.klasUslTrud = klasUslTrud;
    }

    public String getPrRaschSum() {
        return prRaschSum;
    }
    public void setPrRaschSum(String prRaschSum) {
        this.prRaschSum = prRaschSum;
    }

    public List<RaschsvSvOpsOmsRaschSum> getRaschsvSvOpsOmsRaschSumList() {
        return raschsvSvOpsOmsRaschSumList;
    }
    public void setRaschsvSvOpsOmsRaschSumList(List<RaschsvSvOpsOmsRaschSum> raschsvSvOpsOmsRaschSumList) {
        this.raschsvSvOpsOmsRaschSumList = raschsvSvOpsOmsRaschSumList;
    }
    public void addRaschsvSvOpsOmsRaschSum(RaschsvSvOpsOmsRaschSum raschsvSvOpsOmsRaschSum) {raschsvSvOpsOmsRaschSumList.add(raschsvSvOpsOmsRaschSum);}

    public List<RaschsvSvOpsOmsRaschKol> getRaschsvSvOpsOmsRaschKolList() {
        return raschsvSvOpsOmsRaschKolList;
    }
    public void setRaschsvSvOpsOmsRaschKolList(List<RaschsvSvOpsOmsRaschKol> raschsvSvOpsOmsRaschKolList) {
        this.raschsvSvOpsOmsRaschKolList = raschsvSvOpsOmsRaschKolList;
    }
    public void addRaschsvSvOpsOmsRaschKol(RaschsvSvOpsOmsRaschKol raschsvSvOpsOmsRaschKol) {raschsvSvOpsOmsRaschKolList.add(raschsvSvOpsOmsRaschKol);}
}
