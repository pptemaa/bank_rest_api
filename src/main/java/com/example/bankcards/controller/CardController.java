package com.example.bankcards.controller;

import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Выпуск новой карты",
            description = "Создает новую банковскую карту для указанного пользователя. Баланс по умолчанию 0.00, статус ACTIVE."
    )
    public CardResponseDto createdСard(@RequestParam Long userId) {
        Card newCard = cardService.createCardForUser(userId);
        return cardMapper.toDto(newCard);
    }

    @GetMapping("/all")
    @Operation(
            summary = "Получение списка всех карт",
            description = "Возвращает список всех банковских карт в системе. Доступно только администратору."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public List<CardResponseDto> getAllCards() {
        List<Card> cards = cardService.getAllCards();
        return cards.stream().map(cardMapper::toDto).toList();
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Получение списка карт пользователя администратором",
            description = "Возвращает список карт пользователя с переданным ID. Доступно только администратору."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public List<CardResponseDto> getUserCardsByAdmin(@PathVariable Long userId) {
        List<Card> cards = cardService.getCardsByUserId(userId);
        return cards.stream().map(cardMapper::toDto).toList();
    }

    @GetMapping("/me")
    @Operation(
            summary = "Получение списка своих карт",
            description = "Возвращает список карт текущего авторизованного пользователя."
    )
    @PreAuthorize("hasRole('USER')")
    public List<CardResponseDto> getCurrentUserCards(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<Card> cards = cardService.getCardsForUser(currentUser.getId());
        return cards.stream().map(cardMapper::toDto).toList();
    }

    @PatchMapping("/{cardId}/block-request")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Запрос блокировки своей карты",
            description = "Пользователь может запросить блокировку только своей карты."
    )
    public CardResponseDto requestBlockCard(@PathVariable Long cardId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Card blockedCard = cardService.requestBlockCard(cardId, currentUser.getId());
        return cardMapper.toDto(blockedCard);
    }

    @PatchMapping("/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Блокировка карты",
            description = "Администратор блокирует карту пользователя."
    )
    public CardResponseDto blockCard(@PathVariable Long cardId) {
        Card blockedCard = cardService.blockCardByAdmin(cardId);
        return cardMapper.toDto(blockedCard);
    }

    @PatchMapping("/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Активация карты",
            description = "Администратор переводит карту в статус ACTIVE."
    )
    public CardResponseDto activateCard(@PathVariable Long cardId) {
        Card blockedCard = cardService.activateCard(cardId);
        return cardMapper.toDto(blockedCard);
    }
    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Удаление карты",
            description = "Администратор удаляет карту пользователя."
    )
    public void deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Перевод денег",
            description = "Осуществляет перевод только между картами текущего пользователя."
    )
    public void transferMoney(@Valid @RequestBody TransferRequestDto requestDto, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        cardService.transferMoney(
                currentUser.getId(),
                requestDto.getFromCardId(),
                requestDto.getToCardNumber(),
                requestDto.getAmount()
        );
    }
}