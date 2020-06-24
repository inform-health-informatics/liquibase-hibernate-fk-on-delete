package com.example.ejb3.auction;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;

@Entity
public class Watcher {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "WATCHER_SEQ")
    @TableGenerator(name = "WATCHER_SEQ", table = "WatcherSeqTable")
    private Integer id;

    @SuppressWarnings("unused")
    private String name;

    @ManyToOne
    private AuctionItem auctionItem;
}