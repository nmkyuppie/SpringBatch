package com.xanite.dto;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "t5interestrates")
@Data
public class T5InterestRates implements Serializable {

    private static final long serialVersionUID = 8274269114727351045L;

    @Column(name = "ITRBAND")
    private String interestRateBand;

    @Column(name = "ITRRANGEMIN")
    private String interestRateRangeMin;

    @Column(name = "ITRRATE")
    private String interestRate;

    @Column(name = "ITRCURRENCY")
    private String interestRateCurrency;

    @Column(name = "ITRACCOUNTCODE")
    private String interestRateAccountCode;

    @Column(name = "ITRDATE")
    private String interestRateDate;

    @Column(name = "ITRMARGIN")
    private String interestRateMargin;

    @Column(name = "ITRDAYSINYEAR")
    private String interestRateDaysInYear;

    @Column(name = "ITRPRODUCTTYPE")
    private String interestRateProductType;

    @Column(name = "ITRPARENTMANAGER")
    private String interestRateParentManager;

    @Column(name = "ITRMANAGERRESP")
    private String interestRateManagerResp;

    @Column(name = "ITRMINPAYMENT")
    private String interestRateMinPayment;

    @Column(name = "itrintrorate")
    private String interestRateIntroRate;

    @Column(name = "itrintroexpiry")
    private String interestRateIntroExpiry;

    @Column(name = "itrloyaltyrate")
    private String interestRateLoyaltyRate;

    @Column(name = "itrminbalance")
    private String interestRateMinBalance;

    @Column(name = "itrmaxwithdrawal")
    private String interestRateMaxWithdrawal;

    @Column(name = "itrloyaltyexpiry")
    private String interestRateLoyaltyExpiry;

    @Column(name = "itrstartdate")
    private String interestRateStartDate;

    @Id
    @Basic(optional = false)
    @Column(name = "ITRSQLTIMESTAMP", insertable = false, updatable = false)
    private String interestRateTimestamp;

    @Column(name = "itrintroperiod")
    private String interestRateIntroPeriod;
}
