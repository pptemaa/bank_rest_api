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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing card management and transfer endpoints.
 */
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

    /** Creates card for selected user (admin only). */
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

    /** Returns paged list of all cards (admin only). */
    @GetMapping("/all")
    @Operation(
            summary = "Получение списка всех карт",
            description = "Возвращает список всех банковских карт в системе с пагинацией. Доступно только администратору."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardResponseDto> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Pageable pageable = buildPageable(page, size, direction);
        return cardService.getAllCards(pageable).map(cardMapper::toDto);
    }

    /** Returns paged list of user cards (admin only). */
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Получение списка карт пользователя администратором",
            description = "Возвращает список карт пользователя с пагинацией. Доступно только администратору."
    )
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardResponseDto> getUserCardsByAdmin(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Pageable pageable = buildPageable(page, size, direction);
        return cardService.getCardsByUserId(userId, pageable).map(cardMapper::toDto);
    }

    /** Returns paged list of current user's cards. */
    @GetMapping("/me")
    @Operation(
            summary = "Получение списка своих карт",
            description = "Возвращает список карт текущего авторизованного пользователя с пагинацией."
    )
    @PreAuthorize("hasRole('USER')")
    public Page<CardResponseDto> getCurrentUserCards(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Pageable pageable = buildPageable(page, size, direction);
        User currentUser = (User) authentication.getPrincipal();
        return cardService.getCardsForUser(currentUser.getId(), pageable).map(cardMapper::toDto);
    }

    private Pageable buildPageable(int page, int size, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(sortDirection, "id"));
    }

    /** Sends user request to block own card. */
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

    /** Blocks card by admin action. */
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

    /** Activates card by admin action. */
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
    /** Deletes card by admin action. */
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

    /** Transfers money between user's own cards. */
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