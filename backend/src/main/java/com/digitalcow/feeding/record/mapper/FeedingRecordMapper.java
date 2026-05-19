package com.digitalcow.feeding.record.mapper;

import com.digitalcow.feeding.record.FeedingRecord;
import com.digitalcow.feeding.record.dto.FeedingRecordCreateDto;
import com.digitalcow.feeding.record.dto.FeedingRecordResponseDto;
import com.digitalcow.feeding.record.dto.FeedingRecordUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de FeedingRecord. */
@Mapper(componentModel = "spring")
public interface FeedingRecordMapper {

    FeedingRecordResponseDto toDto(FeedingRecord entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    FeedingRecord fromCreate(FeedingRecordCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "lotId", ignore = true)
    @Mapping(target = "feedItemId", ignore = true)
    @Mapping(target = "recordedByUserId", ignore = true)
    void applyUpdate(FeedingRecordUpdateDto dto, @MappingTarget FeedingRecord entity);
}
