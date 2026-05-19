package com.digitalcow.feeding.feeditem.mapper;

import com.digitalcow.feeding.feeditem.FeedItem;
import com.digitalcow.feeding.feeditem.dto.FeedItemCreateDto;
import com.digitalcow.feeding.feeditem.dto.FeedItemResponseDto;
import com.digitalcow.feeding.feeditem.dto.FeedItemUpdateDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Mapper MapStruct de FeedItem. */
@Mapper(componentModel = "spring")
public interface FeedItemMapper {

    @Mapping(target = "isGlobal", expression = "java(entity.getAccountId() == null)")
    FeedItemResponseDto toDto(FeedItem entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    FeedItem fromCreate(FeedItemCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "code", ignore = true)
    void applyUpdate(FeedItemUpdateDto dto, @MappingTarget FeedItem entity);
}
