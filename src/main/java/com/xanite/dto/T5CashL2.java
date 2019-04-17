package com.xanite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "T5CASHL2")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class T5CashL2 implements Serializable {

    private static final long serialVersionUID = -1289545927782051424L;

    @Id
    @Column(name = "cl2identity")
    private Long cl2Identity;

    @Column(name = "CL2CLIENTNUM")
    private String cl2ClientNumber;

    @Column(name = "CL2CURRENCYUNIT")
    private String cl2CurrencyUnit;

    @Column(name = "CL2ACCOUNTCODE")
    private String cl2AccountCode;

    @Column(name = "CL2PORTFOLIONUM")
    private String cl2PortfolioNumber;

    @Column(name = "CL2SETTLEDBALANCE")
    private String cl2SettledBalance;

    @Column(name = "CL2ACCOUNTBALANCE")
    private String cl2AccountBalance;

    @Column(name = "CL2INTEREST")
    private String cl2Interest;

    @Column(name = "CL2INTERESTDATE")
    private String cl2InterestDate;

    @ManyToOne
    @JoinColumn(name = "CL2CLIENTNUM", referencedColumnName = "cliCode", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    private T5Client t5Client;
}
