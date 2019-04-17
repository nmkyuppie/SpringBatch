package com.xanite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParticipantBean {
	
	Long cl2Identity;
	String cl2ClientNumber;
	String cl2CurrencyUnit;
	String cl2AccountCode;
	String cl2PortfolioNumber;
	String cl2AccountBalance;
	String cl2SettledBalance;
	String cl2Interest;
	String cl2InterestDate;
	String cliManagerResp;
	String cliProductType;
	String cliParentManager; 
}
