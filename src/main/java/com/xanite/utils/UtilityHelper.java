package com.xanite.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
@NoArgsConstructor(access= AccessLevel.PRIVATE)
public class UtilityHelper {

    public static String getFileExtension(String fileName) {

        String fileExtension = null;
        int fileNameExtensionIndex = fileName.lastIndexOf('.');
        if (fileNameExtensionIndex != -1) {
            fileExtension = fileName.substring(fileNameExtensionIndex + 1);
        }
        return fileExtension;
    }

    public static JobParameters getJobParametersForJob(JobEnum jobEnum) {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString("JOB_NAME", jobEnum.getValue());
        jobParametersBuilder.addString("Time", String.valueOf(System.currentTimeMillis()));
        return jobParametersBuilder.toJobParameters();
    }

}
