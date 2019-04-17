package com.xanite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "T5CASHL3")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class T5CashL3 implements Serializable {

    private static final long serialVersionUID = -1289545927782051424L;

    @Id
    @Column(name = "cl3identity")
    private Long cl3Identity;

    @Column(name = "CL3CLIENTNUM")
    private String cl3ClientNumber;

    @Column(name = "CL3CURRENCYUNIT")
    private String cl3CurrencyUnit;

    @Column(name = "CL3ACCOUNTNUM")
    private String cl3AccountNumber;

    @Column(name = "CL3PORTFOLIONUM")
    private String cl3PortfolioNumber;

    @Column(name = "CL3DATE1")
    private String cl3Date1;

    @Column(name = "CL3DATE2")
    private String cl3Date2;

    @Column(name = "CL3REVFLAG")
    private String cl3ReversalFlag;

    @Column(name = "CL3DRCRFLAG")
    private String cl3DebitCreditFlag;

    @Column(name = "CL3VALUE")
    private String cl3Value;
}
