package com.aplana.sbrf.taxaccounting.model.ndfl;

/**
 * Класс инкапсулирующий информацию о КПП, ОКТМО и код налогового органа подразделения
 */
public class TripleKppOktmoTaxOrganCode {

    private String kpp;
    private String oktmo;
    private String taxOrganCode;

    public TripleKppOktmoTaxOrganCode (String kpp,String oktmo,String taxOrganCode) {
        this.kpp = kpp;
        this.oktmo = oktmo;
        this.taxOrganCode = taxOrganCode;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getOktmo() {
        return oktmo;
    }

    public void setOktmo(String oktmo) {
        this.oktmo = oktmo;
    }

    public String getTaxOrganCode() {
        return taxOrganCode;
    }

    public void setTaxOrganCode(String taxOrganCode) {
        this.taxOrganCode = taxOrganCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TripleKppOktmoTaxOrganCode that = (TripleKppOktmoTaxOrganCode) o;

        if (kpp != null ? !kpp.equals(that.kpp) : that.kpp != null) return false;
        return oktmo != null ? oktmo.equals(that.oktmo) : that.oktmo == null;
    }

    @Override
    public int hashCode() {
        int result = kpp != null ? kpp.hashCode() : 0;
        result = 31 * result + (oktmo != null ? oktmo.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TripleKppOktmoTaxOrganCode{" +
                "kpp='" + kpp + '\'' +
                ", oktmo='" + oktmo + '\'' +
                ", taxOrganCode='" + taxOrganCode + '\'' +
                '}';
    }
}
