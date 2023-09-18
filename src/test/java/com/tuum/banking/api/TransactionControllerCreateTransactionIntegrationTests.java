package com.tuum.banking.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.banking.api.model.CreateTransactionRequest;
import com.tuum.banking.domain.Balance;
import com.tuum.banking.es.events.AccountCreatedEvent;
import com.tuum.banking.es.events.TransactionEvent;
import com.tuum.banking.es.events.TransactionEvent.TransactionType;
import com.tuum.banking.es.repository.EventStoreRepository;
import com.tuum.banking.es.repository.model.EventDto;
import com.tuum.banking.query.repository.AccountQueryRepository;
import com.tuum.banking.query.repository.dto.AccountDto;
import com.tuum.banking.query.repository.dto.BalanceDto;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import static com.tuum.banking.es.events.TransactionEvent.TransactionType.OUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TransactionControllerCreateTransactionIntegrationTests {

    private static final String URI = "/v1/transaction";
    private final UUID accountId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
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
    void withAbsentRequestValuesShouldReturnBadRequest() throws Exception {
        mockMvc.perform(composeRequest(new JSONObject().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.rows", hasSize(5)))
                .andExpect(jsonPath("$.type", is("ARGUMENT_NOT_VALID")))
                .andExpect(jsonPath("$.rows[0].field", is("accountId")))
                .andExpect(jsonPath("$.rows[0].reason", is("NotNull")))
                .andExpect(jsonPath("$.rows[0].message", is("must not be null")))

                .andExpect(jsonPath("$.rows[1].field", is("amount")))
                .andExpect(jsonPath("$.rows[1].reason", is("NotNull")))
                .andExpect(jsonPath("$.rows[1].message", is("must not be null")))

                .andExpect(jsonPath("$.rows[2].field", is("currency")))
                .andExpect(jsonPath("$.rows[2].reason", is("NotNull")))
                .andExpect(jsonPath("$.rows[2].message", is("must not be null")))

                .andExpect(jsonPath("$.rows[3].field", is("description")))
                .andExpect(jsonPath("$.rows[3].reason", is("NotBlank")))
                .andExpect(jsonPath("$.rows[3].message", is("must not be blank")))

                .andExpect(jsonPath("$.rows[4].field", is("direction")))
                .andExpect(jsonPath("$.rows[4].reason", is("NotNull")))
                .andExpect(jsonPath("$.rows[4].message", is("must not be null")));
    }

    @Test
    void withNotPositiveAmountShouldReturnBadRequest() throws Exception {
        var request = new CreateTransactionRequest(
                accountId,
                EUR,
                BigDecimal.valueOf(-1),
                IN,
                "Test description"
        );

        mockMvc.perform(composeRequest(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.rows", hasSize(1)))
                .andExpect(jsonPath("$.type", is("ARGUMENT_NOT_VALID")))
                .andExpect(jsonPath("$.rows[0].field", is("amount")))
                .andExpect(jsonPath("$.rows[0].reason", is("Positive")))
                .andExpect(jsonPath("$.rows[0].message", is("must be greater than 0")));
    }

    @Test
    void withInvalidDirectionShouldReturnBadRequest() throws Exception {
        var request = new JSONObject()
                .put("accountId", accountId)
                .put("currency", EUR)
                .put("amount", 1)
                .put("direction", "WITHIN")
                .put("description", "Test description");

        mockMvc.perform(composeRequest(request.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.rows", hasSize(1)))
                .andExpect(jsonPath("$.type", is("ARGUMENT_NOT_VALID")))
                .andExpect(jsonPath("$.rows[0].field", is("TransactionType")))
                .andExpect(jsonPath("$.rows[0].reason", is("WITHIN")))
                .andExpect(jsonPath("$.rows[0].message", is("invalid type")));
    }

    @ParameterizedTest
    @ValueSource(doubles = { 0.001, 9999999999999999.99 })
    void withAmountOutOfBoundsShouldReturnBadRequest(Double amount) throws Exception {
        var request = new CreateTransactionRequest(
                accountId,
                EUR,
                BigDecimal.valueOf(amount),
                IN,
                "Test description"
        );

        mockMvc.perform(composeRequest(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.rows", hasSize(1)))
                .andExpect(jsonPath("$.type", is("ARGUMENT_NOT_VALID")))
                .andExpect(jsonPath("$.rows[0].field", is("amount")))
                .andExpect(jsonPath("$.rows[0].reason", is("Digits")))
                .andExpect(jsonPath("$.rows[0].message", is("numeric value out of bounds (<15 digits>.<2 digits> expected)")));
    }

    @Test
    void withInvalidAccountShouldReturnNotFound() throws Exception {
        var request = new CreateTransactionRequest(
                accountId,
                EUR,
                BigDecimal.TEN,
                IN,
                "Test description"
        );

        mockMvc.perform(composeRequest(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.entity", is("account")))
                .andExpect(jsonPath("$.message", is(String.format("account with id = %s was not found", accountId))));
    }

    @Test
    void withValidDepositTransactionRequestShouldMakeTransactionAndReturnValidResponse() throws Exception {
        testValidTransaction(IN, BigDecimal.valueOf(20));
    }

    @Test
    void withValidWithdrawTransactionRequestShouldMakeTransactionAndReturnValidResponse() throws Exception {
        testValidTransaction(OUT, BigDecimal.ZERO);
    }

    private void testValidTransaction(TransactionType type, BigDecimal balanceAfterTransaction) throws Exception {
        var currency = EUR;
        var amount = BigDecimal.TEN;
        var description = "Test description";
        var balances = Set.of(new Balance(currency, BigDecimal.TEN));

        prepareNewAccount(balances);

        var request = new CreateTransactionRequest(
                accountId,
                currency,
                amount,
                type,
                description
        );

        mockMvc.perform(composeRequest(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", aMapWithSize(7)))
                .andExpect(jsonPath("$.accountId", is(accountId.toString())))
                .andExpect(jsonPath("$.transactionId", is(notNullValue())))
                .andExpect(jsonPath("$.currency", is(currency.name())))
                .andExpect(jsonPath("$.amount", is(10)))
                .andExpect(jsonPath("$.type", is(type.name())))
                .andExpect(jsonPath("$.description", is(description)))
                .andExpect(jsonPath("$.balance", is(balanceAfterTransaction.intValue())));

        var events = eventStoreRepository.getEvents(accountId, 2L);
        assertThat(events.size()).isEqualTo(1);

        var eventDto = events.get(0);
        assertThat(eventDto.getAggregateId()).isEqualTo(accountId);
        assertThat(eventDto.getVersion()).isEqualTo(3L);

        var transactionEvent = objectMapper.readValue(eventDto.getData(), TransactionEvent.class);
        assertThat(transactionEvent.getAggregateId()).isEqualTo(accountId);
        assertThat(transactionEvent.getCurrency()).isEqualTo(currency);
        assertThat(transactionEvent.getAmount()).isEqualTo(amount);
        assertThat(transactionEvent.getType()).isEqualTo(type);
        assertThat(transactionEvent.getDescription()).isEqualTo(description);
        assertThat(transactionEvent.getVersion()).isEqualTo(3L);Thread.sleep(1000);

        var transactions = queryRepository.selectTransactions(accountId);
        assertThat(transactions.size()).isEqualTo(1);

        var transaction = transactions.get(0);
        assertThat(transaction.id()).isEqualTo(transactionEvent.getTransactionId());
        assertThat(transaction.accountId()).isEqualTo(transactionEvent.getAggregateId());
        assertThat(transaction.currency()).isEqualTo(transactionEvent.getCurrency().name());
        assertThat(transaction.amount().setScale(2)).isEqualTo(transactionEvent.getAmount().setScale(2));
        assertThat(transaction.type()).isEqualTo(transactionEvent.getType().name());
        assertThat(transaction.description()).isEqualTo(transactionEvent.getDescription());

        var balance = queryRepository.selectBalance(accountId, currency);
        assertThat(balance.isPresent()).isTrue();
        assertThat(balance.get().amount().setScale(2)).isEqualTo(balanceAfterTransaction.setScale(2));
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
                        UUID.randomUUID(),
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
            }
        }
    }

    private MockHttpServletRequestBuilder composeRequest(String content) {
        return post(URI).content(content).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
    }
}
