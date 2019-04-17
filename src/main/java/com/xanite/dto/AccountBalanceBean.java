package com.xanite.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author nachimm
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountBalanceBean  implements Serializable{
	
	private static final long serialVersionUID = 3096466559620454516L;
	
	String cl2ClientNumber;
	String cl2CurrencyUnit;
	String cl2AccountCode;
	String cl2PortfolioNumber;
	String creditBalance;
	String debitBalance;
}
