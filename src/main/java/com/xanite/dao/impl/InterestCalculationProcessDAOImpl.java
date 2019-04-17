package com.xanite.dao.impl;

import com.xanite.dao.InterestCalculationProcessDAO;
import com.xanite.dto.AccountBalanceBean;
import com.xanite.dto.ParticipantBean;
import com.xanite.repository.T5CashL2Repository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
@Service
public class InterestCalculationProcessDAOImpl implements InterestCalculationProcessDAO {

	private static final String REV_FLAG_N = "N";
	private static final String CREDIT_FLAG = "C";
	private static final String DEBIT_FLAG = "D";
	private static final String INCOME_ACCOUNT_CODE = "INCOME";

	@PersistenceContext
	EntityManager entityManager;

	@Autowired
	T5CashL2Repository t5CashL2Repository;

	/**
	 * To get the participant's count
	 */
	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS, isolation = Isolation.READ_UNCOMMITTED)
	public Long getParticipantsCount() {
		Long count = t5CashL2Repository.getParticipantsCount();
		log.info("Participants size : {}", count);
		return count;
	}

	/** 
	 * @see com.xanite.dao.InterestCalculationProcessDAO#getParticipants(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 * @param fromId	-	Offset
	 * @param page		-	Page number	
	 * @param range		-	Limit
	 * @return
	 */
	@Override
	@Transactional(readOnly = true,propagation = Propagation.SUPPORTS, isolation = Isolation.READ_UNCOMMITTED)
	public List<ParticipantBean> getParticipants(Integer fromId, Integer page, Integer range) {

		String queryString = "SELECT "
				+ "new com.xanite.dto.ParticipantBean(cl2.cl2Identity, cl2.cl2ClientNumber, cl2.cl2CurrencyUnit, cl2.cl2AccountCode, "
				+ "cl2.cl2PortfolioNumber, cl2.cl2AccountBalance, cl2.cl2SettledBalance, cl2.cl2Interest, cl2.cl2InterestDate, "
				+ "cli.cliManagerResp, cli.cliProductType, cli.cliParentManager ) FROM T5CashL2 cl2 "
				+ "INNER JOIN T5Client cli on cl2.cl2ClientNumber = cli.cliCode "
				+ "INNER JOIN T5Participant p on cl2.cl2AccountCode = p.participantID AND p.participantInterest = 'Y' "
				+ "WHERE  p.participantID <> '"+INCOME_ACCOUNT_CODE+"' ";

		TypedQuery<ParticipantBean> query = entityManager.createQuery(queryString, ParticipantBean.class)
														.setFirstResult(fromId-1)
														.setMaxResults(range);
		List<ParticipantBean> participantList = query.getResultList();

		log.info("Participant list loaded from {} range {} size {}", fromId, range, participantList.size());
		return participantList;
	}

	/** 
	 * Get sum of credits and debts to get the balance
	 * @see com.xanite.dao.InterestCalculationProcessDAO#calculateCashBalance(com.xanite.dto.ParticipantBean)
	 * @param participant
	 * @return
	 */
	@Override
	public Double calculateCashBalance(ParticipantBean participant) {

		Double sumOfCredits = getSumOfBalance(participant, CREDIT_FLAG);
		Double sumOfDebits = 0.00;
		if(sumOfCredits != null && sumOfCredits > 0) {
			sumOfDebits = getSumOfBalance(participant, DEBIT_FLAG);
		}
		sumOfCredits = sumOfCredits == null ? 0.00 : sumOfCredits;
		sumOfDebits = sumOfDebits == null ? 0.00 : sumOfDebits;
		
		return sumOfCredits - sumOfDebits;
		
	}

	/**
	 * Gets sum of credit / debt balance
	 * @param participant
	 * @param flag
	 * @return
	 */
	private Double getSumOfBalance(ParticipantBean participant, String flag) {
		
		Double sumOfBalance = null;
		if(null != participant.getCl2InterestDate() && !StringUtils.isEmpty(participant.getCl2InterestDate().trim())) {
			
			String creditQueryString = "SELECT SUM(CAST(ISNULL(cl3.cl3Value,0.00) double)) " + 
					"FROM T5CashL3 cl3 " + 
					"WHERE cl3.cl3ClientNumber = ?1 " + 
					"AND cl3.cl3CurrencyUnit = ?2 " + 
					"AND cl3.cl3AccountNumber  = ?3 " + 
					"AND cl3.cl3PortfolioNumber= ?4 " + 
					"AND (cl3.cl3Date2 > ?5 OR (LTRIM(RTRIM(cl3.cl3Date2)) = '' AND cl3.cl3Date1 > ?5 )) " + 
					"AND cl3.cl3ReversalFlag = ?6 " + 
					"AND cl3.cl3DebitCreditFlag= ?7 ";
			TypedQuery<Double> creditQuery = entityManager.createQuery(creditQueryString, Double.class);
			creditQuery.setParameter(1, participant.getCl2ClientNumber());
			creditQuery.setParameter(2, participant.getCl2CurrencyUnit());
			creditQuery.setParameter(3, participant.getCl2AccountCode());
			creditQuery.setParameter(4, participant.getCl2PortfolioNumber());
			creditQuery.setParameter(5, participant.getCl2InterestDate());
			creditQuery.setParameter(6, REV_FLAG_N);
			creditQuery.setParameter(7, flag);
			sumOfBalance = creditQuery.getSingleResult();
			if(null != sumOfBalance) {
				String creditDebitString = flag.equals("C")?"Credits":"Debits";
				log.info("Sum of {} is {} for {}}", creditDebitString, sumOfBalance, participant);
			}
		}
		return sumOfBalance;
	}

	/** 
	 * @see com.xanite.dao.InterestCalculationProcessDAO#calculateIncomeBalance(com.xanite.dto.ParticipantBean)
	 * @param participant
	 * @return
	 */
	@Override
	public Double calculateIncomeBalance(ParticipantBean participant) {
		Double sumOfCredits = getSumOfIncomeBalance(participant, CREDIT_FLAG);
		Double sumOfDebits = getSumOfIncomeBalance(participant, DEBIT_FLAG);
		sumOfCredits = sumOfCredits == null ? 0.00 : sumOfCredits;
		sumOfDebits = sumOfDebits == null ? 0.00 : sumOfDebits;
		
		return sumOfCredits - sumOfDebits;
	}

	/**
	 * @param participant
	 * @param flag
	 * @return
	 */
	private Double getSumOfIncomeBalance(ParticipantBean participant, String flag) {
		
		String creditQueryString = "SELECT CAST(ISNULL(RTRIM(LTRIM(cl2.cl2SettledBalance)),0.00) double) " + 
				"FROM T5CashL2 cl2 " + 
				"WHERE cl2.cl2ClientNumber = ?1 " + 
				"AND cl2.cl2CurrencyUnit = ?2 " + 
				"AND cl2.cl2AccountCode  = ?3 " + 
				"AND cl2.cl2PortfolioNumber= ?4 " ;
		TypedQuery<Double> creditQuery = entityManager.createQuery(creditQueryString, Double.class);
		creditQuery.setParameter(1, participant.getCl2ClientNumber());
		creditQuery.setParameter(2, participant.getCl2CurrencyUnit());
		creditQuery.setParameter(3, INCOME_ACCOUNT_CODE);
		creditQuery.setParameter(4, participant.getCl2PortfolioNumber());
		Double incomeBalance = null;
		try{
			incomeBalance = creditQuery.getSingleResult();
		}
		catch(NoResultException nre) {
			incomeBalance = 0.0;
		}
		return incomeBalance;
	}

	/** 
	 * @see com.xanite.dao.InterestCalculationProcessDAO#getBalanceDetails(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 * @param fromId
	 * @param page
	 * @param range
	 * @return
	 */
	@Override
	public List<AccountBalanceBean> getBalanceDetails() {

		String queryString = "SELECT " 
				+ "CL2CLIENTNUM, cl2currencyunit,cl2accountcode,cl2portfolionum, "  
				+ "SUM(CASE WHEN CL3DRCRFLAG='C' THEN cast(cl3value as numeric(20,6)) ELSE 0 END) AS creditbalance, " 
				+ "SUM(CASE WHEN CL3DRCRFLAG='D' THEN cast(cl3value as numeric(20,6)) ELSE 0 END) AS debitbalance "  
				+ "FROM T5CASHL2 " 
				+ "INNER JOIN T5CLIENT ON CLICODE = CL2CLIENTNUM " 
				+ "INNER JOIN (select parpdrparticipantid from  t5participant where parl3interest = 'Y' and parpdrparticipantid <> 'INCOME') A on CL2ACCOUNTCODE = A.parpdrparticipantid " 
				+ "INNER JOIN t5cashl3 on CL3CLIENTNUM = cl2clientnum " 
					+ "AND cl3currencyunit      = CL2CURRENCYUNIT " 
					+ "AND cl3accountnum        = CL2ACCOUNTCODE " 
					+ "AND cl3portfolionum      = CL2PORTFOLIONUM " 
					+ "AND (cl3date2             > CL2INTERESTDATE " 
						+ "OR  (cl3date2 = ' ' AND cl3date1 > CL2INTERESTDATE )) " 
				+ "WHERE " 
				+ "cl3revflag='N' " 
				+ "GROUP BY CL2CLIENTNUM,cl2currencyunit,cl2accountcode,cl2portfolionum";

		Query query = entityManager.createNativeQuery(queryString, Tuple.class);
		@SuppressWarnings("unchecked")
		List<Tuple> accountBalanceTempList = query.getResultList();
		List<AccountBalanceBean> accountBalanceList = new ArrayList<>();
		
		accountBalanceTempList.forEach(bal->{
			AccountBalanceBean abb = new AccountBalanceBean();
			abb.setCl2ClientNumber(bal.get(0, String.class));
			abb.setCl2CurrencyUnit(bal.get(1, String.class));
			abb.setCl2AccountCode(bal.get(2, String.class));
			abb.setCl2PortfolioNumber(bal.get(3, String.class));
			abb.setCreditBalance(""+bal.get(4, BigDecimal.class));
			abb.setDebitBalance(""+bal.get(5, BigDecimal.class));
			accountBalanceList.add(abb);
		});

		log.info("Balance loaded. Size {} ", accountBalanceList.size());
		return accountBalanceList;
	}
}