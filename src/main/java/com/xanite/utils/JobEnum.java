package com.xanite.utils;

import java.util.EnumSet;

/**
 * This enum will keep the jobs used in the application. The enumeration is a
 * simple way to control that the paremeter given as job is correct.
 */
public enum JobEnum {

    /**
     * Process job. The constant value must match the Spring Bean name.
     */

    INTEREST_CALCULATION_EVENT("interestCalculationEvent") {
    };

    private String value;

    JobEnum(String value) {
        this.value = value;
    }

    /**
     * Getter for the enum instance
     *
     * @param job
     * @return
     */
    public static JobEnum get(String job) {

        return EnumSet.allOf(JobEnum.class).stream().filter(action -> action.value.equals(job))
                .findFirst().orElse(null);

    }

    public String getValue() {
        return value;
    }

}
