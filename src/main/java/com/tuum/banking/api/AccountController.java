package com.tuum.banking.api;

import com.tuum.banking.api.model.CreateAccountRequest;
import com.tuum.banking.api.model.CreateAccountResponse;
import com.tuum.banking.api.model.GetAccountResponse;
import com.tuum.banking.command.CreateAccountCommand;
import com.tuum.banking.command.handler.CommandHandler;
import com.tuum.banking.domain.AccountAggregate;
import com.tuum.banking.query.GetAccountQuery;
import com.tuum.banking.query.handler.QueryHandler;
import com.tuum.banking.query.repository.dto.AccountDto;
import com.tuum.banking.query.repository.dto.TransactionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.tuum.banking.mapper.AccountMapper.mapAggregateToResponse;
import static com.tuum.banking.mapper.AccountMapper.mapDtoToResponse;

@RestController
@RequestMapping("/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final CommandHandler<AccountAggregate> commandHandler;
    private final QueryHandler<AccountDto, TransactionDto> queryHandler;

    @PostMapping
    public ResponseEntity<CreateAccountResponse> createAccount(@Validated @RequestBody CreateAccountRequest request) {
        var aggregate = commandHandler.handle(new CreateAccountCommand(
                request.customerId(),
                request.country().toUpperCase(),
                request.currencies()
        ));

        return new ResponseEntity<>(mapAggregateToResponse(aggregate), HttpStatus.CREATED);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<GetAccountResponse> getAccount(@PathVariable String accountId) {
        var account = queryHandler.handle(new GetAccountQuery(UUID.fromString(accountId)));

        return new ResponseEntity<>(mapDtoToResponse(account), HttpStatus.OK);
    }
}
