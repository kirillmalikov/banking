package com.tuum.banking.api;

import com.tuum.banking.domain.Balance;
import com.tuum.banking.query.repository.AccountQueryRepository;
import com.tuum.banking.query.repository.dto.AccountDto;
import com.tuum.banking.query.repository.dto.BalanceDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.UUID;

import static com.tuum.banking.domain.Currency.EUR;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AccountControllerGetAccountIntegrationTests {

    private static final String URI = "/v1/account/";
    private final UUID accountId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountQueryRepository queryRepository;

    @Test
    void withInvalidAccountIdShouldReturnNotFound() throws Exception {
        mockMvc.perform(composeRequest(accountId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.entity", is("account")))
                .andExpect(jsonPath("$.message", is(String.format("account with id = %s was not found", accountId))));
    }

    @Test
    void withValidAccountIdShouldReturnAccountWithBalance() throws Exception {
        prepareAccountWithBalances(new Balance(EUR, BigDecimal.TEN));

        mockMvc.perform(composeRequest(accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", aMapWithSize(3)))
                .andExpect(jsonPath("$.accountId", is(accountId.toString())))
                .andExpect(jsonPath("$.customerId", is(customerId.toString())))
                .andExpect(jsonPath("$.balances", hasSize(1)))
                .andExpect(jsonPath("$.balances[0].currency", is(EUR.name())))
                .andExpect(jsonPath("$.balances[0].amount", is(10.00)));
    }

    private MockHttpServletRequestBuilder composeRequest(UUID accountId) {
        return get(URI + accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    private void prepareAccountWithBalances(Balance... balances) {
        var account = newTestAccount();
        queryRepository.insertAccount(account);

        for (Balance balance : balances) {
            queryRepository.insertBalance(new BalanceDto(account.getId(), balance.currency(), balance.amount()));
        }
    }

    private AccountDto newTestAccount() {
        return new AccountDto(accountId, customerId, "EE");
    }
}