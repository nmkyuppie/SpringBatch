package com.xanite.dto;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "t5client")
@Data
public class T5Client implements Serializable {

    private static final long serialVersionUID = -8543737719108042116L;

    @Id
    @Column(name = "cliidentity")
    Long cliIdentity;

    @Column(name = "CLICODE")
    String cliCode;

    @Column(name = "CLIMANAGERRESP")
    String cliManagerResp;

    @Column(name = "CLIPRODUCTTYPE")
    String cliProductType;

    @Column(name = "CLIPARENTMANAGER")
    String cliParentManager;

    @OneToMany(mappedBy = "t5Client")
    private Set<T5CashL2> t5CashL2s;
}
