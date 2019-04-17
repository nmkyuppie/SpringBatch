package com.xanite.application;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.xanite.config.BatchConfig;
import com.xanite.utils.JobEnum;
import com.xanite.utils.UtilityHelper;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(scanBasePackageClasses = {BatchConfig.class})
@Slf4j
public class Application {

    public static void main(String[] args) {

        try {
            SpringApplication application = new SpringApplication(Application.class);
            application.setWebApplicationType(WebApplicationType.NONE);
            ConfigurableApplicationContext ctx = application.run(args);

            BatchConfigurer batchConfigurer = ctx.getBean(BatchConfigurer.class);

            JobLauncher jobLauncher = batchConfigurer.getJobLauncher();

            if (args.length > 0) {
            	
            	/**
                 * The job name must be equal to the value in the enum and the bean name.
                 * Per example this is starting batch entry in the .bat file.
                 * start /b java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=9000,suspend=n -jar 
                 * -Dspring.config.location=service-impl-props\ (where the properties will be) 
                 * -Dlogging.config=service-impl-props\logback-spring.xml (where the logs config will be) 
                 * .\target\batch-boot-%version%.jar messagesJobInstance
                 * The Spring Boot is a big jar with a parameter that will equal to an entry in the JobEnum class created.
                 * This is easy way to control what is run.
                 */
                JobEnum job = JobEnum.get(args[0].trim());
                if (job != null) {
                    // Getting the job bean. A Job is a main Spring Batch class.
                    Job jobToRun = ctx.getBean(job.getValue(), Job.class);
                    // Getting JobParameters
                    JobParameters jobParameters = UtilityHelper.getJobParametersForJob(job);

                    // Launching the job
                    jobLaunch(jobLauncher, jobToRun, jobParameters);

                } else {
                    // not valid job value
                    log.error("No job found");
                    System.exit(javax.batch.runtime.BatchStatus.FAILED.ordinal());
                }
            } else {
                // not valid job value
                log.error("No job found");
                System.exit(javax.batch.runtime.BatchStatus.FAILED.ordinal());
            }

        } catch (Exception e) {
            log.error("Unexpected error running job:", e);
            System.exit(javax.batch.runtime.BatchStatus.FAILED.ordinal());
        }

    }

    /**
     * This method will be used to launch the job.
     *
     * @param jobLauncher
     * @param jobToExecute
     * @throws JobExecutionAlreadyRunningException
     * @throws JobRestartException
     * @throws JobInstanceAlreadyCompleteException
     * @throws JobParametersInvalidException
     * @throws InterruptedException
     */
    private static void jobLaunch(JobLauncher jobLauncher, Job jobToExecute, JobParameters jobParameters)
            throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException,
            JobParametersInvalidException, InterruptedException {
    	
    	/**
    	 * The time element will make the job unique. That will make possible to run several
    	 * batches from the same type without problems.
    	 * Running the job in a different thread and Getting the job execution
    	 * that will provide info about the running job
    	 */
        JobExecution jobExecution = jobLauncher.run(jobToExecute, jobParameters);

        // Getting the job instance to get the name per example.
        JobInstance jobInstance = jobExecution.getJobInstance();
        log.info(String.format("********* Running job with name %s **********", jobInstance.getJobName()));

        // Getting the job status
        BatchStatus batchStatus = jobExecution.getStatus();

        // Meanwhile is running we are sleeping the Spring boot thread one
        // second.
        while (batchStatus.isRunning()) {
            log.info("*********** Still running.... **************");
            Thread.sleep(1000);
        }

        // The Spring batch has finished, we need to obtain the exit status.
        ExitStatus exitStatus = jobExecution.getExitStatus();
        String exitCode = exitStatus.getExitCode();

        int exitCodeNum = 1; // An error by default
        if (exitCode.startsWith(ExitStatus.COMPLETED.getExitCode())) {
            exitCodeNum = 0;
        }

        log.info(String.format("*********** Final Exit status: %s *************", exitCode));

        // Returning the exit code as output for the batch.
        System.exit(exitCodeNum);
    }

}
