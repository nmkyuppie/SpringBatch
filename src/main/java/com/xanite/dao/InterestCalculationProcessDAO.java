package com.xanite.dao;

import com.xanite.dto.AccountBalanceBean;
import com.xanite.dto.ParticipantBean;

import java.util.List;

public interface InterestCalculationProcessDAO {

    Long getParticipantsCount();

    List<ParticipantBean> getParticipants(Integer fromId, Integer page, Integer range);

    Double calculateCashBalance(ParticipantBean participant);

	Double calculateIncomeBalance(ParticipantBean participant);

	List<AccountBalanceBean> getBalanceDetails();

}
