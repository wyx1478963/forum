package com.fc.model;

import java.math.BigDecimal;
import java.util.Date;

public class PostCorrelation {
    private Integer id;
    private Integer sourcePid;
    private Integer targetPid;
    private BigDecimal correlation;
    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSourcePid() {
        return sourcePid;
    }

    public void setSourcePid(Integer sourcePid) {
        this.sourcePid = sourcePid;
    }

    public Integer getTargetPid() {
        return targetPid;
    }

    public void setTargetPid(Integer targetPid) {
        this.targetPid = targetPid;
    }

    public BigDecimal getCorrelation() {
        return correlation;
    }

    public void setCorrelation(BigDecimal correlation) {
        this.correlation = correlation;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
