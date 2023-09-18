package com.tuum.banking.api;

import com.tuum.banking.api.model.CreateTransactionRequest;
import com.tuum.banking.api.model.CreateTransactionResponse;
import com.tuum.banking.api.model.GetTransactionResponse;
import com.tuum.banking.command.TransactionCommand;
import com.tuum.banking.command.handler.CommandHandler;
import com.tuum.banking.domain.AccountAggregate;
import com.tuum.banking.mapper.TransactionMapper;
import com.tuum.banking.query.GetTransactionQuery;
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

import java.util.List;
import java.util.UUID;

import static com.tuum.banking.mapper.TransactionMapper.mapAggregateToResponse;

@RestController
@RequestMapping("/v1/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final CommandHandler<AccountAggregate> commandHandler;
    private final QueryHandler<AccountDto, TransactionDto> queryHandler;

    @PostMapping
    public ResponseEntity<CreateTransactionResponse> createTransaction(
            @Validated @RequestBody CreateTransactionRequest request
    ) {
        var aggregate = commandHandler.handle(
                new TransactionCommand(
                        request.accountId(),
                        request.currency(),
                        request.amount(),
                        request.direction(),
                        request.description()
                )
        );

        return new ResponseEntity<>(mapAggregateToResponse(aggregate), HttpStatus.OK);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<List<GetTransactionResponse>> getTransactions(@PathVariable String accountId) {
        var transactions = queryHandler.handle(new GetTransactionQuery(UUID.fromString(accountId)));

        return new ResponseEntity<>(
                transactions.stream().map(TransactionMapper::mapDtoToResponse).toList(),
                HttpStatus.OK
        );
    }
}
