package com.xanite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.xanite.dto.T5CashL2;

@Repository
public interface T5CashL2Repository extends JpaRepository<T5CashL2, Integer> {

	@Query("SELECT count(cl2) FROM T5CashL2 cl2 "
			+ "INNER JOIN T5Client cli on cl2.cl2ClientNumber = cli.cliCode "
			+ "INNER JOIN T5Participant p on cl2.cl2AccountCode = p.participantID AND p.participantInterest = 'Y' "
			+ "WHERE  p.participantID <> 'INCOME' ")
	Long getParticipantsCount();
}