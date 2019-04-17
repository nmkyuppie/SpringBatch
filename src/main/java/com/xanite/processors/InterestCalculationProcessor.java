package com.xanite.processors;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.xanite.dao.InterestCalculationProcessDAO;
import com.xanite.dto.AccountBalanceBean;
import com.xanite.dto.ParticipantBean;
import com.xanite.dto.T5CashL2;
import com.xanite.dto.T5InterestRates;
import com.xanite.repository.T5InterestRatesRepository;
import com.xanite.utils.DateUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InterestCalculationProcessor implements ItemProcessor<ParticipantBean, T5CashL2> {

	@Autowired
	T5InterestRatesRepository t5InterestRatesRepository;

	@Autowired
	InterestCalculationProcessDAO interestCalculationProcessDAO;

	List<AccountBalanceBean> bulkAccountBalanceList;

	/**
	 * @param accountBalanceList
	 */
	public InterestCalculationProcessor(List<AccountBalanceBean> accountBalanceList) {
		this.bulkAccountBalanceList = accountBalanceList;
	}

	/** 
	 * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
	 * @param participant
	 * @return
	 * @throws Exception
	 */
	@Override
	public T5CashL2 process(ParticipantBean participant) throws Exception {

		T5CashL2 t5CashL2 = null;

		if(null != participant.getCl2InterestDate() && !StringUtils.isEmpty(participant.getCl2InterestDate().trim())) {
			
			Predicate<AccountBalanceBean> clientNumberPredicate = b->b.getCl2ClientNumber().trim().equals(participant.getCl2ClientNumber().trim());
			Predicate<AccountBalanceBean> currencyUnitPredicate = b->b.getCl2CurrencyUnit().trim().equals(participant.getCl2CurrencyUnit().trim());
			Predicate<AccountBalanceBean> accountCodePredicate = b->b.getCl2AccountCode().trim().equals(participant.getCl2AccountCode().trim());
			Predicate<AccountBalanceBean> portfolioPredicate = b->b.getCl2PortfolioNumber().trim().equals(participant.getCl2PortfolioNumber().trim());
			
			List<AccountBalanceBean> accoutBalanceList = bulkAccountBalanceList.stream().filter(clientNumberPredicate
					.and(currencyUnitPredicate)
					.and(accountCodePredicate)
					.and(portfolioPredicate)).collect(Collectors.toList());

			if(!accoutBalanceList.isEmpty()) {
				//t5cashl2 table has unique index on client number, portfolio, currency and account code. So fetching 1st index will not be an issue
				Double balance = calculateBalance(accoutBalanceList.get(0));

				log.info("Balance --- {}",balance);
				if(balance > 0.00) {
					T5InterestRates interestRateBean = getInterestBand(balance, participant);
					Optional<String> interestRateOptional = Optional.ofNullable(interestRateBean.getInterestRate());
					Double baseInterestRate = Double.parseDouble(interestRateOptional.orElse("0.00"));
					Double percentage = new Double("100");
					Double noOfDaysInCurrentYear = (double) DateUtils.noOfDaysInCurrentYear();
					Double noOfDaysToPay = (double) DateUtils.calculateNoOfDays(participant.getCl2InterestDate().trim());
					noOfDaysToPay = (noOfDaysToPay <= 0L) ? 1L : noOfDaysToPay;
					Optional<Double> incomeBalanceOptional = Optional.ofNullable(interestCalculationProcessDAO.calculateIncomeBalance(participant));
					Double incomeBalance = incomeBalanceOptional.orElse(new Double("0.00"));

					log.info("No. of days to pay: {} Balance: {} Income Balance: {} Base Interest Rate: {}",noOfDaysToPay, balance, incomeBalance, baseInterestRate);
					Double interest = balance * (baseInterestRate/percentage) * (noOfDaysToPay/noOfDaysInCurrentYear);
					log.info("Calculated interest is {}", interest);

					Double totalBalance = balance + incomeBalance;

					t5CashL2 = new T5CashL2( participant.getCl2Identity(), participant.getCl2ClientNumber(), participant.getCl2CurrencyUnit(), participant.getCl2AccountCode(), 
							participant.getCl2PortfolioNumber(), String.format("%.2f", totalBalance), participant.getCl2AccountBalance(), String.format("%.2f", interest),
							DateUtils.getCurrentDate(), null);
				}
			}
		}

		return t5CashL2;
	}

	/**
	 * @param accountBalanceList1
	 * @return
	 */
	private Double calculateBalance(AccountBalanceBean abb) {
		Double sumOfCredits = abb.getCreditBalance() == null ? 0.00 : new Double(abb.getCreditBalance());
		Double sumOfDebits = abb.getDebitBalance() == null ? 0.00 : new Double(abb.getDebitBalance());

		return sumOfCredits - sumOfDebits;
	}

	/**
	 * Get Interest band based in the balance and the participant.
	 * @param balance
	 * @param participant
	 * @return
	 */
	private T5InterestRates getInterestBand(Double balance, ParticipantBean participant) {
		List<T5InterestRates> t5InterestRates = t5InterestRatesRepository.findAll();

		Predicate<T5InterestRates> currencyPredicate = ir->ir.getInterestRateCurrency().trim().equals(participant.getCl2CurrencyUnit().trim());
		Predicate<T5InterestRates> accountCodePredicate = ir->ir.getInterestRateAccountCode().trim().equals(participant.getCl2AccountCode().trim());
		Predicate<T5InterestRates> productTypePredicate = ir->ir.getInterestRateProductType().trim().equals(participant.getCliProductType().trim());
		Predicate<T5InterestRates> parentManagerPredicate = ir->ir.getInterestRateParentManager().trim().equals(participant.getCliParentManager().trim());
		Predicate<T5InterestRates> managerRespPredicate = ir->ir.getInterestRateManagerResp().trim().equals(participant.getCliManagerResp().trim());
		Predicate<T5InterestRates> interestRangePredicate = ir-> Double.parseDouble(ir.getInterestRateRangeMin()) <= balance ;
		Comparator<T5InterestRates> interestRangeComparator = (ir1, ir2) -> Double.compare(Double.parseDouble(ir1.getInterestRateRangeMin()), Double.parseDouble(ir2.getInterestRateRangeMin()));


		T5InterestRates interestRate = null;

		try {
			interestRate = t5InterestRates.stream()
					.filter(currencyPredicate
							.and(accountCodePredicate)
							.and(productTypePredicate)
							.and(parentManagerPredicate)
							.and(managerRespPredicate)
							.and(interestRangePredicate))
					.max(interestRangeComparator).orElse(new T5InterestRates());
			log.info("Final InterestRates {}",interestRate);

		}catch(NoSuchElementException e) {
			interestRate = new T5InterestRates();
			log.info("No interest rate band found for {}", participant);
		}

		// Setting default value to zero if the Interest Rate is null.
		if (StringUtils.isEmpty(interestRate.getInterestRate())){
			interestRate.setInterestRate("0");
		}

		return interestRate;
	}

}
