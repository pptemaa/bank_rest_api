package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(target = "cardNumber", qualifiedByName = "maskCardNumber")
    CardResponseDto toDto(Card card);
    @Named("maskCardNumber")
    default String maskCardNumber(String rawNumber){
        if (rawNumber==null || rawNumber.length()<4){
            return rawNumber;
        }
        return "**** **** **** " + rawNumber.substring(rawNumber.length() - 4);
    }
}
