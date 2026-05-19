package com.digitalcow.feeding.plan.mapper;

import com.digitalcow.feeding.plan.FeedingPlan;
import com.digitalcow.feeding.plan.FeedingPlanItem;
import com.digitalcow.feeding.plan.LotFeedingPlan;
import com.digitalcow.feeding.plan.dto.FeedingPlanCreateDto;
import com.digitalcow.feeding.plan.dto.FeedingPlanItemCreateDto;
import com.digitalcow.feeding.plan.dto.FeedingPlanItemDto;
import com.digitalcow.feeding.plan.dto.FeedingPlanItemUpdateDto;
import com.digitalcow.feeding.plan.dto.FeedingPlanUpdateDto;
import com.digitalcow.feeding.plan.dto.LotFeedingPlanResponseDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de FeedingPlan y entidades anidadas. */
@Mapper(componentModel = "spring")
public interface FeedingPlanMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    FeedingPlan fromCreate(FeedingPlanCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    void applyUpdate(FeedingPlanUpdateDto dto, @MappingTarget FeedingPlan entity);

    FeedingPlanItemDto toItemDto(FeedingPlanItem entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "feedingPlanId", ignore = true)
    FeedingPlanItem fromItemCreate(FeedingPlanItemCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "feedingPlanId", ignore = true)
    @Mapping(target = "feedItemId", ignore = true)
    void applyItemUpdate(FeedingPlanItemUpdateDto dto, @MappingTarget FeedingPlanItem entity);

    LotFeedingPlanResponseDto toLotAssignDto(LotFeedingPlan entity);
}
