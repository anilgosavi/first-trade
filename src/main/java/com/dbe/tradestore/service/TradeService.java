package com.dbe.tradestore.service;

import com.dbe.tradestore.entity.Trade;
import com.dbe.tradestore.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class TradeService {
    @Autowired
    private TradeRepository tradeRepository;

    public Trade saveOrUpdateTrade(Trade trade) {
        validateTrade(trade);
        trade.setCreatedDate(new Date());
        return tradeRepository.save(trade);
    }

    private void validateTrade(Trade trade) {
        // Check if the trade version is valid
        Optional<Trade> existingTradeOpt = tradeRepository.findById(trade.getTradeId());
        if (existingTradeOpt.isPresent()) {
            Trade existingTrade = existingTradeOpt.get();
            if (trade.getVersion() < existingTrade.getVersion()) {
                throw new IllegalArgumentException("Lower version received. Trade rejected.");
            }
        }

        // Check if the maturity date is valid
        if (trade.getMaturityDate().before(new Date())) {
            throw new IllegalArgumentException("Maturity date is less than today's date. Trade rejected.");
        }
    }

    public void updateExpiredFlag() {
        // Iterate through all trades and update the expired flag if maturity date has passed
        tradeRepository.findAll().forEach(trade -> {
            if (trade.getMaturityDate().before(new Date())) {
                trade.setExpired(true);
                tradeRepository.save(trade);
            }
        });
    }
}
