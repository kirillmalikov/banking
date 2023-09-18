package com.tuum.banking.es.repository;

import com.tuum.banking.es.repository.model.EventDto;
import com.tuum.banking.es.repository.model.SnapshotDto;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface AccountEventStoreMapper {

    @Insert("INSERT INTO event (aggregate_id, data, version) VALUES (#{aggregateId}, #{data}, #{version})")
    void insertEvent(EventDto event);

    @Select("SELECT * FROM event WHERE aggregate_id=#{aggregateId}")
    List<EventDto> selectEvents(UUID aggregateId);

    @Select("SELECT * FROM event WHERE aggregate_id=#{aggregateId} AND version>#{version}")
    List<EventDto> selectEventsAfterVersion(UUID aggregateId, Long version);

    @Insert("INSERT INTO snapshot (aggregate_id, data, version) VALUES (#{aggregateId}, #{data}, #{version}) ON CONFLICT (aggregate_id) DO UPDATE SET data=#{data}, version=#{version}, timestamp=now()")
    void insertSnapshot(SnapshotDto snapshot);

    @Select("SELECT * FROM snapshot WHERE aggregate_id=#{aggregateId}")
    Optional<SnapshotDto> selectSnapshot(UUID aggregateId);

    @Select("SELECT count(id) FROM event WHERE aggregate_id = #{aggregateId}")
    int exists(UUID aggregateId);
}
