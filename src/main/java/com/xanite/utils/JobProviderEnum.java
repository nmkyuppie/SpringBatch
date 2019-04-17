package com.xanite.utils;

import java.util.EnumSet;

public enum JobProviderEnum {

    INTEREST_CALCULATION_EVENT("Interest", JobEnum.INTEREST_CALCULATION_EVENT) {
    };

    private String value;

    private JobEnum jobEnum;

    JobProviderEnum(String value, JobEnum jobEnum) {
        this.value = value;
        this.jobEnum = jobEnum;
    }

    public static JobProviderEnum getJobProviderEnum(JobEnum jobEnum) {
        return EnumSet.allOf(JobProviderEnum.class).stream()
                .filter(jpe -> jpe.jobEnum.equals(jobEnum)).findFirst().orElse(null);
    }

    public String getValue() {
        return value;
    }

    public JobEnum getJobEnum() {
        return jobEnum;
    }

}
