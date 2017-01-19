package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Сведения об обучающихся (СведОбуч)
 */
public class RaschsvSvedObuch extends IdentityObject<Long> {

    private Long raschsvSvPrimTarif1422Id;
    private String unikNomer;
    private String familia;
    private String imya;
    private String middleName;
    private String spravNomer;
    private Date spravData;
    private String spravNodeName;
    private Long raschsvSvSum1TipId;

    // СвСум1Тип
    private RaschsvSvSum1Tip raschsvSvSum1Tip;

    // СвРеестрМДО
    private List<RaschsvSvReestrMdo> raschsvSvReestrMdoList;

    public RaschsvSvedObuch() {
        super();
        raschsvSvReestrMdoList = new ArrayList<RaschsvSvReestrMdo>();
    }

    public static final String SEQ = "seq_raschsv_sved_obuch";
    public static final String TABLE_NAME = "raschsv_sved_obuch";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_SV_PRIM_TARIF1_422_ID = "raschsv_sv_prim_tarif1_422_id";
    public static final String COL_RASCHSV_SV_SUM1_TIP_ID = "raschsv_sv_sum1_tip_id";
    public static final String COL_UNIK_NOMER = "unik_nomer";
    public static final String COL_FAMILIA = "familia";
    public static final String COL_IMYA = "imya";
    public static final String COL_MIDDLE_NAME = "middle_name";
    public static final String COL_SPRAV_NOMER = "sprav_nomer";
    public static final String COL_SPRAV_DATA = "sprav_data";
    public static final String COL_SPRAV_NODE_NAME = "sprav_node_name";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_SV_PRIM_TARIF1_422_ID, COL_RASCHSV_SV_SUM1_TIP_ID,
            COL_UNIK_NOMER, COL_FAMILIA, COL_IMYA, COL_MIDDLE_NAME, COL_SPRAV_NOMER, COL_SPRAV_DATA, COL_SPRAV_NODE_NAME};

    public Long getRaschsvSvPrimTarif1422Id() {
        return raschsvSvPrimTarif1422Id;
    }
    public void setRaschsvSvPrimTarif1422Id(Long raschsvSvPrimTarif1422Id) {
        this.raschsvSvPrimTarif1422Id = raschsvSvPrimTarif1422Id;
    }

    public String getUnikNomer() {
        return unikNomer;
    }
    public void setUnikNomer(String unikNomer) {
        this.unikNomer = unikNomer;
    }

    public String getFamilia() {
        return familia;
    }
    public void setFamilia(String familia) {
        this.familia = familia;
    }

    public String getImya() {
        return imya;
    }
    public void setImya(String imya) {
        this.imya = imya;
    }

    public String getMiddleName() {
        return middleName;
    }
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getSpravNomer() {
        return spravNomer;
    }
    public void setSpravNomer(String spravNomer) {
        this.spravNomer = spravNomer;
    }

    public Date getSpravData() {
        return spravData;
    }
    public void setSpravData(Date spravData) {
        this.spravData = spravData;
    }

    public String getSpravNodeName() {
        return spravNodeName;
    }
    public void setSpravNodeName(String spravNodeName) {
        this.spravNodeName = spravNodeName;
    }

    public RaschsvSvSum1Tip getRaschsvSvSum1Tip() {
        return raschsvSvSum1Tip;
    }
    public void setRaschsvSvSum1Tip(RaschsvSvSum1Tip raschsvSvSum1Tip) {
        this.raschsvSvSum1Tip = raschsvSvSum1Tip;
    }

    public List<RaschsvSvReestrMdo> getRaschsvSvReestrMdoList() {
        return raschsvSvReestrMdoList;
    }
    public void setRaschsvSvReestrMdoList(List<RaschsvSvReestrMdo> raschsvSvReestrMdoList) {
        this.raschsvSvReestrMdoList = raschsvSvReestrMdoList;
    }
    public void addRaschsvSvReestrMdo(RaschsvSvReestrMdo raschsvSvReestrMdo) {raschsvSvReestrMdoList.add(raschsvSvReestrMdo);}

    public Long getRaschsvSvSum1TipId() {
        return raschsvSvSum1TipId;
    }
    public void setRaschsvSvSum1TipId(Long raschsvSvSum1TipId) {
        this.raschsvSvSum1TipId = raschsvSvSum1TipId;
    }
}
