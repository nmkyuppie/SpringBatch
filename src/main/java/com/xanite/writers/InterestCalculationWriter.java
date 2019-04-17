package com.xanite.writers;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.xanite.dto.T5CashL2;
import com.xanite.repository.T5CashL2Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InterestCalculationWriter implements ItemWriter<T5CashL2>{
	
	@Autowired
	T5CashL2Repository t5CashL2Repository;

	@Override
	public void write(List<? extends T5CashL2> t5CashL2s) throws Exception {
		/**
		 * Entities will be passed as bulk to the writer. 
		 */
		for (T5CashL2 t5CashL2 : t5CashL2s) {
//			t5CashL2Repository.saveAndFlush(t5CashL2);
			log.info("Wrote line " + t5CashL2);
        }
//		log.info("Size of t5cashl2s updates {} " , t5CashL2s.size());
	}
}
