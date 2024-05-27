package com.dbe.tradestore.service;

import com.dbe.tradestore.entity.Trade;
import com.dbe.tradestore.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TradeServiceTest {

    @InjectMocks
    private TradeService tradeService;

    @Mock
    private TradeRepository tradeRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveOrUpdateTrade_NewTrade() {
        Trade trade = new Trade();
        trade.setTradeId("T1");
        trade.setVersion(1);
        trade.setCounterPartyId("CP-1");
        trade.setBookId("B1");
        trade.setMaturityDate(new Date(System.currentTimeMillis() + 100000)); // Future date

        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        Trade savedTrade = tradeService.saveOrUpdateTrade(trade);

        assertNotNull(savedTrade);
        verify(tradeRepository, times(1)).save(trade);
    }

    @Test
    void testSaveOrUpdateTrade_ExistingTrade_LowerVersion() {
        Trade existingTrade = new Trade();
        existingTrade.setTradeId("T1");
        existingTrade.setVersion(2);

        Trade newTrade = new Trade();
        newTrade.setTradeId("T1");
        newTrade.setVersion(1);

        when(tradeRepository.findById("T1")).thenReturn(Optional.of(existingTrade));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            tradeService.saveOrUpdateTrade(newTrade);
        });

        String expectedMessage = "Lower version received. Trade rejected.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testSaveOrUpdateTrade_MaturityDatePassed() {
        Trade trade = new Trade();
        trade.setTradeId("T1");
        trade.setVersion(1);
        trade.setMaturityDate(new Date(System.currentTimeMillis() - 100000)); // Past date

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            tradeService.saveOrUpdateTrade(trade);
        });

        String expectedMessage = "Maturity date is less than today's date. Trade rejected.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testUpdateExpiredFlag() {
        Trade trade1 = new Trade();
        trade1.setTradeId("T1");
        trade1.setMaturityDate(new Date(System.currentTimeMillis() - 100000)); // Past date
        trade1.setExpired(false);

        Trade trade2 = new Trade();
        trade2.setTradeId("T2");
        trade2.setMaturityDate(new Date(System.currentTimeMillis() + 100000)); // Future date
        trade2.setExpired(false);

        when(tradeRepository.findAll()).thenReturn(List.of(trade1, trade2));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        tradeService.updateExpiredFlag();

        assertTrue(trade1.isExpired());
        assertFalse(trade2.isExpired());
        verify(tradeRepository, times(1)).save(trade1);
        verify(tradeRepository, never()).save(trade2);
    }
}
