package com.dbe.tradestore.controller;

import com.dbe.tradestore.entity.Trade;
import com.dbe.tradestore.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trades")
public class TradeController {
    @Autowired
    private TradeService tradeService;

    @PostMapping
    public ResponseEntity<Trade> saveOrUpdateTrade(@RequestBody Trade trade) {
        Trade savedTrade = tradeService.saveOrUpdateTrade(trade);
        return ResponseEntity.ok(savedTrade);
    }

    @PutMapping("/updateExpired")
    public ResponseEntity<Void> updateExpiredFlag() {
        tradeService.updateExpiredFlag();
        return ResponseEntity.noContent().build();
    }
}
