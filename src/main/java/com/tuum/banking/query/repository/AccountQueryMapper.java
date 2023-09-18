package com.tuum.banking.query.repository;

import com.tuum.banking.query.repository.dto.AccountDto;
import com.tuum.banking.query.repository.dto.BalanceDto;
import com.tuum.banking.query.repository.dto.TransactionDto;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mapper
public interface AccountQueryMapper {

    @Insert("INSERT INTO account (id, customer_id, country) VALUES (#{id}, #{customerId}, #{country})")
    void insertAccount(AccountDto entity);

    @Insert("INSERT INTO balance (account_id, currency, amount) VALUES (#{accountId}, #{currency}, #{amount})")
    void insertBalance(BalanceDto balance);

    @Update("UPDATE balance SET amount=#{amount} WHERE account_id=#{accountId} AND currency=#{currency}")
    void updateBalance(BalanceDto balance);

    @Select("SELECT * FROM balance WHERE account_id=#{accountId} AND currency=#{currency}")
    Optional<BalanceDto> selectBalance(UUID accountId, String currency);

    @Results(id = "accountResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "customerId", column = "customer_id"),
            @Result(property = "country", column = "country"),
            @Result(property = "balances", column = "id",
                    javaType = Set.class,
                    many = @Many(select = "selectBalancesByAccountId"))
    })
    @Select("SELECT * FROM account WHERE id=#{accountId}")
    Optional<AccountDto> selectAccount(UUID accountId);

    @Select("SELECT * FROM balance WHERE account_id = #{accountId}")
    List<BalanceDto> selectBalancesByAccountId(UUID accountId);

    @Insert("INSERT INTO transaction (id, account_id, type, currency, amount, description) VALUES (#{id}, #{accountId}, #{type}, #{currency}, #{amount}, #{description})")
    void insertTransaction(TransactionDto transaction);

    @Select("SELECT * FROM transaction WHERE account_id=#{accountId}")
    List<TransactionDto> selectTransactions(UUID accountId);

    @Select("SELECT count(id) FROM account WHERE id = #{accountId}")
    int accountExists(UUID accountId);
}
