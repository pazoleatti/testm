package com.aplana.sbrf.taxaccounting.model.refbook;

/**
 * Created by aokunev on 26.06.2017.
 */
public class FiasCheckInfo {
    /**
     * ID адреса в ФИАС
     */
    private Long fiasId;

    /**
     * Индекс найден в ФИАС
     */
    private boolean validIndex;

    /**
     * Субъект найден в ФИАС
     */
    private boolean validRegion;

    /**
     * Район найден в ФИАС
     */
    private boolean validArea;

    /**
     * Город найден в ФИАС
     */
    private boolean validCity;

    /**
     * Населенный пункт найден в ФИАС
     */
    private boolean validLoc;

    /**
     * Улица найдена в ФИАС
     */
    private boolean validStreet;

    public Long getFiasId() {
        return fiasId;
    }

    public void setFiasId(Long fiasId) {
        this.fiasId = fiasId;
    }

    public boolean isValidIndex() {
        return validIndex;
    }

    public void setValidIndex(boolean validIndex) {
        this.validIndex = validIndex;
    }

    public boolean isValidRegion() {
        return validRegion;
    }

    public void setValidRegion(boolean validRegion) {
        this.validRegion = validRegion;
    }

    public boolean isValidArea() {
        return validArea;
    }

    public void setValidArea(boolean validArea) {
        this.validArea = validArea;
    }

    public boolean isValidCity() {
        return validCity;
    }

    public void setValidCity(boolean validCity) {
        this.validCity = validCity;
    }

    public boolean isValidLoc() {
        return validLoc;
    }

    public void setValidLoc(boolean validLoc) {
        this.validLoc = validLoc;
    }

    public boolean isValidStreet() {
        return validStreet;
    }

    public void setValidStreet(boolean validStreet) {
        this.validStreet = validStreet;
    }
}
