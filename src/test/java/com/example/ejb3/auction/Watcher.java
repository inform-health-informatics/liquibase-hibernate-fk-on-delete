package com.example.ejb3.auction;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
public class Watcher {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "WATCHER_SEQ")
    @TableGenerator(name = "WATCHER_SEQ", table = "WatcherSeqTable")
    private Integer id;

    @SuppressWarnings("unused")
    private String name;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AuctionItem auctionItem;
}