/**
 * 
 */
package com.xanite.tasklet;

import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import com.xanite.dao.InterestCalculationProcessDAO;
import com.xanite.dto.AccountBalanceBean;

import lombok.extern.slf4j.Slf4j;

/**
 * @author nachimm
 *
 */
@Slf4j
public class BalanceCalculationTasklet implements Tasklet, StepExecutionListener{

	@Autowired
	InterestCalculationProcessDAO interestCalculationProcessDAO;

	/** 
	 * @see org.springframework.batch.core.StepExecutionListener#afterStep(org.springframework.batch.core.StepExecution)
	 * @param arg0
	 * @return
	 */
	@Override
	public ExitStatus afterStep(StepExecution arg0) {
		return null;
	}

	/** 
	 * @see org.springframework.batch.core.StepExecutionListener#beforeStep(org.springframework.batch.core.StepExecution)
	 * @param arg0
	 */
	@Override
	public void beforeStep(StepExecution arg0) {
		//Before step process
	}

	/** 
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.springframework.batch.core.StepContribution, org.springframework.batch.core.scope.context.ChunkContext)
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws Exception
	 */
	@Override
	public RepeatStatus execute(StepContribution arg0, ChunkContext chunkContext) throws Exception {
		log.info("Balance calculation started.");
		ExecutionContext executionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
		List<AccountBalanceBean> accountBalanceBeans = interestCalculationProcessDAO.getBalanceDetails();
		executionContext.put("BALANCE_DETAILS", accountBalanceBeans);
		return RepeatStatus.FINISHED;
	}
}
