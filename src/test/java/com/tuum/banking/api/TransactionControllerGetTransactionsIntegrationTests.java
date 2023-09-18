package com.tuum.banking.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.banking.domain.Balance;
import com.tuum.banking.es.events.AccountCreatedEvent;
import com.tuum.banking.es.events.TransactionEvent;
import com.tuum.banking.es.repository.EventStoreRepository;
import com.tuum.banking.es.repository.model.EventDto;
import com.tuum.banking.query.repository.AccountQueryRepository;
import com.tuum.banking.query.repository.dto.AccountDto;
import com.tuum.banking.query.repository.dto.BalanceDto;
import com.tuum.banking.query.repository.dto.TransactionDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.tuum.banking.domain.Currency.EUR;
import static com.tuum.banking.es.events.TransactionEvent.TransactionType.IN;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TransactionControllerGetTransactionsIntegrationTests {

    private static final String URI = "/v1/transaction/";
    private final UUID accountId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID transactionId = UUID.randomUUID();
    private final String country = "EE";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EventStoreRepository eventStoreRepository;
    @Autowired
    private AccountQueryRepository queryRepository;

    @Test
    void withInvalidAccountShouldReturnNotFound() throws Exception {
        var invalidAccountId = UUID.randomUUID();
        mockMvc.perform(composeRequest(invalidAccountId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.entity", is("account")))
                .andExpect(jsonPath(
                        "$.message",
                        is(String.format("account with id = %s was not found", invalidAccountId))
                ));
    }

    @Test
    void withValidAccountShouldReturnTransactions() throws Exception {
        prepareNewAccount(Set.of(new Balance(EUR, BigDecimal.TEN)));

        mockMvc.perform(composeRequest(accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]", aMapWithSize(6)))
                .andExpect(jsonPath("$[0].accountId", is(accountId.toString())))
                .andExpect(jsonPath("$[0].transactionId", is(transactionId.toString())))
                .andExpect(jsonPath("$[0].currency", is(EUR.name())))
                .andExpect(jsonPath("$[0].amount", is(10.00)))
                .andExpect(jsonPath("$[0].direction", is(IN.name())))
                .andExpect(jsonPath("$[0].description", is("initial amount")));
    }

    private void prepareNewAccount(Set<Balance> balances) throws Exception {
        AtomicLong version = new AtomicLong(0L);
        var accountCreatedEvent = new AccountCreatedEvent(
                accountId,
                customerId,
                country,
                balances.stream().map(Balance::currency).collect(Collectors.toSet())
        );
        queryRepository.insertAccount(new AccountDto(accountId, customerId, country));
        eventStoreRepository.storeEvent(
                EventDto.builder()
                        .aggregateId(accountId)
                        .data(objectMapper.writeValueAsBytes(accountCreatedEvent))
                        .version(version.incrementAndGet())
                        .build()
        );
        for (Balance balance : balances) {
            queryRepository.insertBalance(new BalanceDto(accountId, balance.currency(), balance.amount()));
            if (balance.amount().compareTo(BigDecimal.ZERO) > 0) {
                var transactionEvent = new TransactionEvent(
                        accountId,
                        transactionId,
                        balance.currency(),
                        balance.amount(),
                        IN,
                        "initial amount"
                );
                transactionEvent.setVersion(version.incrementAndGet());
                eventStoreRepository.storeEvent(
                        EventDto.builder()
                                .aggregateId(accountId)
                                .data(objectMapper.writeValueAsBytes(transactionEvent))
                                .version(version.get())
                                .build());
                queryRepository.insertTransaction(
                        new TransactionDto(
                                transactionEvent.getTransactionId(),
                                transactionEvent.getAggregateId(),
                                transactionEvent.getType().name(),
                                transactionEvent.getCurrency().name(),
                                transactionEvent.getAmount(),
                                transactionEvent.getDescription()
                        )
                );
            }
        }
    }

    private MockHttpServletRequestBuilder composeRequest(UUID accountId) {
        return get(URI + accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }
}
