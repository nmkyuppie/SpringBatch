package com.xanite.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
This class will keep all the custom configuration
 */
@Component
@Data
public class ConfigProperties {


    /**
     * Batch size
     */
    private int batchSize;

    /**
     * CorePoolSize
     */
    private int corePoolSize;


    /**
     * maxPoolSize
     */
    private int maxPoolSize;

    public ConfigProperties(
            @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}") int batchSize,
            @Value("${taskExecutor.corePoolSize}") int corePoolSize,  
            @Value("${taskExecutor.maxPoolSize}") int  maxPoolSize ) {
       this.batchSize =batchSize;
       this.corePoolSize =corePoolSize;
       this.maxPoolSize = maxPoolSize;
    }
}