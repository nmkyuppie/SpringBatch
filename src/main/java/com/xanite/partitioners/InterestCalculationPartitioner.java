package com.xanite.partitioners;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class InterestCalculationPartitioner implements Partitioner {

    /**
     * Key constant for the context in the partitioned  map
     */
    private static final String DEFAULT_KEY_NAME = "thread";

    /**
     * Prefix for every partition
     */
    private static final String PARTITION_KEY = "partition";

    @Getter
    @Setter
    private Long totalCount;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> map = new HashMap<>(gridSize);
        log.debug("Total:" + totalCount);
        log.debug("Grid size:" + gridSize);
        long range = totalCount / gridSize;
        long fromId = 1;
        long toId = range;
        long mod = totalCount % gridSize;
        for (int i = 1; i <= gridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            context.putLong("fromId", fromId);
            context.putLong("page", i);

            if (i == gridSize) {
                context.putLong("range", range + mod);
                context.putLong("toId", toId + mod);
            } else {
                context.putLong("range", range);
                context.putLong("toId", toId);
            }

            // Give each thread a name, thread 1,2,3
            context.putString("name", DEFAULT_KEY_NAME + i);
            map.put(PARTITION_KEY + i, context);

            fromId = toId + 1;
            toId += range;
        }

        return map;
    }

}
