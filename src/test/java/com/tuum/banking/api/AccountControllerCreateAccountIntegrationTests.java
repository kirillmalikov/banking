package com.tuum.banking.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.tuum.banking.api.model.CreateAccountRequest;
import com.tuum.banking.domain.Currency;
import com.tuum.banking.es.events.AccountCreatedEvent;
import com.tuum.banking.es.repository.EventStoreRepository;
import com.tuum.banking.es.repository.model.EventDto;
import com.tuum.banking.query.repository.AccountQueryRepository;
import com.tuum.banking.query.repository.dto.BalanceDto;
import org.assertj.core.groups.Tuple;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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

import static com.tuum.banking.domain.Currency.EUR;
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
class AccountControllerCreateAccountIntegrationTests {

    private static final String URI = "/v1/account";
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
    void withNullCustomerIdShouldReturnBadRequest() throws Exception {
        var request = new CreateAccountRequest(null, country, Set.of(EUR));

        mockMvc.perform(composeRequest(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.rows", hasSize(1)))
                .andExpect(jsonPath("$.type", is("ARGUMENT_NOT_VALID")))
                .andExpect(jsonPath("$.rows[0].field", is("customerId")))
                .andExpect(jsonPath("$.rows[0].reason", is("NotNull")))
                .andExpect(jsonPath("$.rows[0].message", is("must not be null")));
    }

    @ParameterizedTest
    @MethodSource("nullOrBlankStrings")
    void withNullCountryShouldReturnBadRequest(String country) throws Exception {
        var request = new CreateAccountRequest(customerId, country, Set.of(EUR));

        mockMvc.perform(composeRequest(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.type", is("ARGUMENT_NOT_VALID")))
                .andExpect(jsonPath("$.rows[0].field", is("country")))
                .andExpect(jsonPath("$.rows[0].reason", is("NotBlank")))
                .andExpect(jsonPath("$.rows[0].message", is("must not be blank")));
    }

    private static String[] nullOrBlankStrings() {
        return new String[]{"", null};
    }

    @ParameterizedTest
    @ValueSource(strings = {"E", "EST"})
    void withInvalidSizeCountryCodeShouldReturnBadRequest(String country) throws Exception {
        var request = new CreateAccountRequest(customerId, country, Set.of(EUR));

        mockMvc.perform(composeRequest(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.rows", hasSize(1)))
                .andExpect(jsonPath("$.type", is("ARGUMENT_NOT_VALID")))
                .andExpect(jsonPath("$.rows[0].field", is("country")))
                .andExpect(jsonPath("$.rows[0].reason", is("Size")))
                .andExpect(jsonPath("$.rows[0].message", is("size must be between 2 and 2")));
    }

    @Test
    void withInvalidCurrencyCodeShouldReturnBadRequest() throws Exception {
        var request = new JSONObject();
        var currencies = new JSONArray();
        currencies.put("EEK");
        request.put("customerId", customerId);
        request.put("country", country);
        request.put("currencies", currencies);

        mockMvc.perform(composeRequest(request.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.rows", hasSize(1)))
                .andExpect(jsonPath("$.type", is("ARGUMENT_NOT_VALID")))
                .andExpect(jsonPath("$.rows[0].field", is(Currency.class.getSimpleName())))
                .andExpect(jsonPath("$.rows[0].reason", is("EEK")))
                .andExpect(jsonPath("$.rows[0].message", is("invalid type")));
    }

    @Test
    void withEmptyCurrenciesCodeShouldReturnBadRequest() throws Exception {
        var request = new CreateAccountRequest(customerId, country, Set.of());

        mockMvc.perform(composeRequest(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.rows", hasSize(1)))
                .andExpect(jsonPath("$.type", is("ARGUMENT_NOT_VALID")))
                .andExpect(jsonPath("$.rows[0].field", is("currencies")))
                .andExpect(jsonPath("$.rows[0].reason", is("NotEmpty")))
                .andExpect(jsonPath("$.rows[0].message", is("must not be empty")));
    }

    @Test
    void withValidRequestShouldCreateAccountAndStoreEvent() throws Exception {
        var request = new CreateAccountRequest(customerId, country, Set.of(EUR));

        var response = mockMvc
                .perform(composeRequest(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", aMapWithSize(3)))
                .andExpect(jsonPath("$.accountId", is(notNullValue())))
                .andExpect(jsonPath("$.customerId", is(customerId.toString())))
                .andExpect(jsonPath("$.balances", hasSize(1)))
                .andExpect(jsonPath("$.balances[0].currency", is(EUR.name())))
                .andExpect(jsonPath("$.balances[0].amount", is(0)))
                .andReturn();Thread.sleep(1000);

        UUID accountId = UUID.fromString(JsonPath.read(response.getResponse().getContentAsString(), "$.accountId"));
        var account = queryRepository.selectAccount(accountId);
        assertThat(account.isPresent()).isTrue();
        assertThat(account.get().getCustomerId()).isEqualTo(customerId);
        assertThat(account.get().getCountry()).isEqualTo(country);
        assertThat(account.get().getBalances().size()).isEqualTo(1);
        assertThat(account.get().getBalances())
                .extracting(BalanceDto::accountId, BalanceDto::currency, BalanceDto::amount)
                .containsOnly(Tuple.tuple(accountId, EUR, BigDecimal.ZERO.setScale(2)));

        var events = eventStoreRepository.getEvents(accountId);
        assertThat(events.size()).isEqualTo(1);
        assertThat(events.get(0)).extracting(EventDto::getAggregateId).isEqualTo(accountId);
        assertThat(events.get(0).getData()).isNotEmpty();

        var event = objectMapper.readValue(events.get(0).getData(), AccountCreatedEvent.class);
        assertThat(event).extracting(
                AccountCreatedEvent::getAggregateId,
                AccountCreatedEvent::getCustomerId,
                AccountCreatedEvent::getCountry,
                AccountCreatedEvent::getCurrencies,
                AccountCreatedEvent::getVersion
        ).containsExactly(accountId, customerId, country, Set.of(EUR), 1L);
    }

    private MockHttpServletRequestBuilder composeRequest(String content) {
        return post(URI).content(content).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
    }
}