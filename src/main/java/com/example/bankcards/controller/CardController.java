package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@Tag(name = "Банковские карты", description = "API для выпуска, просмотра, блокировки карт и перевода средств")
public class CardController {

    private final CardService cardService;
    private final CardMapper cardMapper;

    public CardController(CardService cardService, CardMapper cardMapper) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Выпуск новой карты",
            description = "Создает новую банковскую карту для указанного пользователя. Баланс по умолчанию 0.00, статус ACTIVE."
    )
    public CardResponseDto createdСard(@RequestParam Long userId) {
        Card newCard = cardService.createCardForUser(userId);
        return cardMapper.toDto(newCard);
    }

    @GetMapping
    @Operation(
            summary = "Получение списка карт пользователя",
            description = "Возвращает список всех банковских карт (с замаскированными номерами), принадлежащих пользователю с переданным ID."
    )
    public List<CardResponseDto> getUserCards(@RequestParam Long userId) {
        List<Card> cards = cardService.getCardsByUserId(userId);
        return cards.stream().map(cardMapper::toDto).toList();
    }

    @PatchMapping("/{cardId}/block")
    @Operation(
            summary = "Блокировка карты",
            description = "Изменяет статус указанной карты на BLOCKED. После блокировки с карты нельзя переводить деньги."
    )
    public CardResponseDto blockCard(@PathVariable Long cardId) {
        Card blockedCard = cardService.blockCard(cardId);
        return cardMapper.toDto(blockedCard);
    }

    @PostMapping("/transfer")
    @Operation(
            summary = "Перевод денег",
            description = "Осуществляет защищенный транзакционный перевод средств с одной карты на другую. Проверяет баланс отправителя и статусы обеих карт."
    )
    public void transferMoney(@Valid @RequestBody TransferRequestDto requestDto) {
        cardService.transferMoney(
                requestDto.getFromCardId(),
                requestDto.getToCardNumber(),
                requestDto.getAmount()
        );
    }
}