package com.xanite.process;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import com.xanite.config.ConfigProperties;
import com.xanite.dao.InterestCalculationProcessDAO;
import com.xanite.dto.AccountBalanceBean;
import com.xanite.dto.ParticipantBean;
import com.xanite.dto.T5CashL2;
import com.xanite.partitioners.InterestCalculationPartitioner;
import com.xanite.processors.InterestCalculationProcessor;
import com.xanite.repository.T5CashL2Repository;
import com.xanite.tasklet.BalanceCalculationTasklet;
import com.xanite.utils.JobEnum;
import com.xanite.writers.InterestCalculationWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class InterestCalculationProcess {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private JobExecutionListener jobExecutionListener;

	@Autowired
	private ExceptionHandler taskletExHandler;

	@Autowired
	private TaskExecutor taskExecutor;
	
	@Autowired
	ConfigProperties configProperties;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	InterestCalculationProcessDAO interestCalculationProcessDAO;
	
	@Autowired
	T5CashL2Repository t5CashL2Repository;

	/**
	 * Job builder for interest calculation event
	 * @return
	 */
	@Bean
	public Job interestCalculationEvent() {
		return jobBuilderFactory.get(JobEnum.INTEREST_CALCULATION_EVENT.name())
				.incrementer(new RunIdIncrementer())
				.listener(jobExecutionListener)
				.start(balanceCalculation()).on("FAILED").end().on("COMPLETED").to(processInterestCalculation()).end().build();	
	}
	
	/**
	 * Step to configure balance calculation tasklet 
	 * @return
	 */
	@Bean
    public Step balanceCalculation() {
        return stepBuilderFactory.get("balanceCalculation").tasklet(balanceCalculationTasklet())
                .exceptionHandler(taskletExHandler).build();
    }
	
	/**
	 * Calculates credit debt balance of the participants.
	 * @return
	 */
	@Bean
    public BalanceCalculationTasklet balanceCalculationTasklet() {
		BalanceCalculationTasklet fileValidationTasklet = new BalanceCalculationTasklet();
        return fileValidationTasklet;
    }

	/**
	 * Partitioner configuration and grid size specification 
	 * @return
	 */
	@Bean
	public Step processInterestCalculation() {
		return stepBuilderFactory.get("partitionCashL2Values")
				// The Grid size match to the task executor core.
				// it means that we will have a initial thread number equals to the pool core threads with different data
				.partitioner("getCreditDebitValues", partitionerMsg()).gridSize(configProperties.getCorePoolSize())
				.step(interestCalculationMainStep())
				.taskExecutor(taskExecutor)
				.build();
	}

	/**
	 * Step which contains reader, processor, writer 
	 * @return
	 */
	@Bean
	public Step interestCalculationMainStep() {
		return stepBuilderFactory.get("getCreditDebitValues")
				.<ParticipantBean, T5CashL2>chunk(configProperties.getBatchSize())
				// Every thread will execute the writing with the batch value in Hibernate. This value must come from the property file and
				// it will similar to the batch for the writing.
				.reader(itemReaderMsg(null, null, null, null))		//injected from executer context
				.processor(itemProcessor(null))
				.writer(itemWriterMsg())
				.exceptionHandler(taskletExHandler)
				.build();
	}

	/**
	 * It will collect participant's count from the database. Used to partition the whole data.
	 * @return
	 */
	@Bean
	public InterestCalculationPartitioner partitionerMsg() {
		InterestCalculationPartitioner partitioner = new InterestCalculationPartitioner();
		// Setting the information needed for the partitioner to divide the jobs 
		partitioner.setTotalCount(interestCalculationProcessDAO.getParticipantsCount());
		return partitioner;
	}

	/**
	 * The reader will read the participants based on the range given
	 * @param message
	 * @return
	 * @throws UnexpectedInputException
	 */
	@Bean
	@StepScope
	public ListItemReader<ParticipantBean> itemReaderMsg(
			@Value("#{stepExecutionContext[page]}") Integer page,
			@Value("#{stepExecutionContext[range]}") Integer range,
			@Value("#{stepExecutionContext[fromId]}") Integer fromId,
			@Value("#{stepExecutionContext[toId]}") Integer toId)  {

		List<ParticipantBean> t5CashL2s = new ArrayList<ParticipantBean>();
		log.info("Reading the page {} range {}", page-1, range);

		List<ParticipantBean> participantList = interestCalculationProcessDAO.getParticipants(fromId, page, range);
		participantList.forEach(t5CashL2s::add);
		
		return new ListItemReader<ParticipantBean>(t5CashL2s);
	}
	
	/**
	 * Processor process the data
	 * @param accountBalanceList 
	 * @return
	 */
	@Bean
	@StepScope
	public ItemProcessor<ParticipantBean, T5CashL2> itemProcessor(@Value("#{jobExecutionContext['BALANCE_DETAILS']}")  List<AccountBalanceBean> accountBalanceList)  {
		return new InterestCalculationProcessor(accountBalanceList);
	}

	/**
	 * The writer will read the entities and writes it based on the chunk size
	 * @return
	 */
	@Bean
	@StepScope
	public InterestCalculationWriter itemWriterMsg() {
		return new InterestCalculationWriter();
	}
}
