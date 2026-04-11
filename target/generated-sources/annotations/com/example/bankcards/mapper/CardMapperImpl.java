package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.entity.Card;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-11T14:14:56+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.17 (Eclipse Adoptium)"
)
@Component
public class CardMapperImpl implements CardMapper {

    @Override
    public CardResponseDto toDto(Card card) {
        if ( card == null ) {
            return null;
        }

        CardResponseDto cardResponseDto = new CardResponseDto();

        cardResponseDto.setCardNumber( maskCardNumber( card.getCardNumber() ) );
        cardResponseDto.setId( card.getId() );
        cardResponseDto.setExpirationDate( card.getExpirationDate() );
        cardResponseDto.setStatus( card.getStatus() );
        cardResponseDto.setBalance( card.getBalance() );

        return cardResponseDto;
    }
}
