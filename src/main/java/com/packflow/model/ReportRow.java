package com.packflow.model;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * Flexible DTO holding aggregated report row data for reports.
 */
public class ReportRow {
    private String col1; // e.g. Order # / Customer Name / Material Code / Service Name
    private String col2; // e.g. Customer / Contact / Material Name / Description
    private String col3; // e.g. Status / Phone / Unit / Status
    private Date dateCol;
    private int countVal;
    private int quantityVal;
    private BigDecimal amountVal = BigDecimal.ZERO;
    private BigDecimal secondaryAmountVal = BigDecimal.ZERO;

    public ReportRow() {
    }

    public String getCol1() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }

    public String getCol2() {
        return col2;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }

    public String getCol3() {
        return col3;
    }

    public void setCol3(String col3) {
        this.col3 = col3;
    }

    public Date getDateCol() {
        return dateCol;
    }

    public void setDateCol(Date dateCol) {
        this.dateCol = dateCol;
    }

    public int getCountVal() {
        return countVal;
    }

    public void setCountVal(int countVal) {
        this.countVal = countVal;
    }

    public int getQuantityVal() {
        return quantityVal;
    }

    public void setQuantityVal(int quantityVal) {
        this.quantityVal = quantityVal;
    }

    public BigDecimal getAmountVal() {
        return amountVal;
    }

    public void setAmountVal(BigDecimal amountVal) {
        this.amountVal = amountVal != null ? amountVal : BigDecimal.ZERO;
    }

    public BigDecimal getSecondaryAmountVal() {
        return secondaryAmountVal;
    }

    public void setSecondaryAmountVal(BigDecimal secondaryAmountVal) {
        this.secondaryAmountVal = secondaryAmountVal != null ? secondaryAmountVal : BigDecimal.ZERO;
    }
}
