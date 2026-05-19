package com.digitalcow.health.plan.mapper;

import com.digitalcow.health.plan.AnimalHealthPlan;
import com.digitalcow.health.plan.HealthPlan;
import com.digitalcow.health.plan.HealthPlanStep;
import com.digitalcow.health.plan.dto.AnimalHealthPlanDto;
import com.digitalcow.health.plan.dto.HealthPlanCreateDto;
import com.digitalcow.health.plan.dto.HealthPlanStepCreateDto;
import com.digitalcow.health.plan.dto.HealthPlanStepDto;
import com.digitalcow.health.plan.dto.HealthPlanStepUpdateDto;
import com.digitalcow.health.plan.dto.HealthPlanUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct para HealthPlan, HealthPlanStep y AnimalHealthPlan. */
@Mapper(componentModel = "spring")
public interface HealthPlanMapper {

    HealthPlanStepDto toStepDto(HealthPlanStep step);

    AnimalHealthPlanDto toAssignDto(AnimalHealthPlan entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    HealthPlan fromCreate(HealthPlanCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void applyUpdate(HealthPlanUpdateDto dto, @MappingTarget HealthPlan entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "healthPlanId", ignore = true)
    HealthPlanStep fromStepCreate(HealthPlanStepCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void applyStepUpdate(HealthPlanStepUpdateDto dto, @MappingTarget HealthPlanStep entity);
}
