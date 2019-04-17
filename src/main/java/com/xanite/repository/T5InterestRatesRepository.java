package com.xanite.repository;

import com.xanite.dto.T5InterestRates;
import com.xanite.dto.T5InterestRatesKey;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true,isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRED) // If the service using the repository is not transactional
public interface T5InterestRatesRepository extends JpaRepository<T5InterestRates, T5InterestRatesKey> {

    @Cacheable("interestRates")
    List<T5InterestRates> findAll();
}
