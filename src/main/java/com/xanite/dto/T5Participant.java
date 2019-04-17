package com.xanite.dto;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t5participant")
@Data
public class T5Participant {

    @Id
    @Column(name = "PARPDRPARTICIPANTID")
    String participantID;

    @Column(name = "parl3interest")
    String participantInterest;

}
