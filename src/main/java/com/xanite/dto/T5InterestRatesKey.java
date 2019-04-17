package com.xanite.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class T5InterestRatesKey implements Serializable {

    private static final long serialVersionUID = -5144820044291472594L;

    private String interestRateCurrency;

    private String interestRateAccountCode;

    private String interestRateProductType;

    private String interestRateParentManager;

    private String interestRateManagerResp;

    private String interestRateStartDate;

}
