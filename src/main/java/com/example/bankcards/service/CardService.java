package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.TransferException;
import com.example.bankcards.repository.CardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserService userService;

    public CardService(CardRepository cardRepository, UserService userService) {
        this.cardRepository = cardRepository;
        this.userService = userService;
    }

    public Card createCardForUser(Long userId) {
        User user = userService.getUserById(userId);
        Card card = new Card();
        card.setUser(user);
        card.setBalance(BigDecimal.ZERO);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpirationDate(LocalDate.now().plusYears(3));
        card.setCardNumber(generateUniqueCardNumber());
        return cardRepository.save(card);
    }

    private String generateUniqueCardNumber() {
        Random random = new Random();
        String cardNumber;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                sb.append(random.nextInt(10));
            }
            cardNumber = sb.toString();
        } while (cardRepository.existsByCardNumber(cardNumber));
        return cardNumber;
    }

    public Page<Card> getCardsByUserId(Long userId, Pageable pageable){
        userService.getUserById(userId);
        return cardRepository.findAllByUserId(userId, pageable);
    }

    public Page<Card> getCardsForUser(Long userId, Pageable pageable) {
        return cardRepository.findAllByUserId(userId, pageable);
    }

    public Page<Card> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    public Card blockCardByAdmin(Long cardId){
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с ID " + cardId + " не найдена"));
        card.setStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    public Card requestBlockCard(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с ID " + cardId + " не найдена"));
        if (!card.getUser().getId().equals(userId)) {
            throw new TransferException("Нельзя запрашивать блокировку чужой карты");
        }
        card.setStatus(CardStatus.BLOCKED);
        return cardRepository.save(card);
    }

    public Card activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с ID " + cardId + " не найдена"));
        card.setStatus(CardStatus.ACTIVE);
        return cardRepository.save(card);
    }

    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Карта с ID " + cardId + " не найдена"));
        cardRepository.delete(card);
    }

    @Transactional
    public void transferMoney(Long userId, Long fromCardId, String toCardNumber, BigDecimal amount) {
        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Карта отправителя не найдена"));

        Card toCard = cardRepository.findByCardNumber(toCardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Карта получателя не найдена"));

        if (!fromCard.getUser().getId().equals(userId) || !toCard.getUser().getId().equals(userId)) {
            throw new TransferException("Переводы разрешены только между своими картами");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new TransferException("Недостаточно средств на карте");
        }

        if (!fromCard.getStatus().name().equals("ACTIVE") || !toCard.getStatus().name().equals("ACTIVE")) {
            throw new TransferException("Одна из карт заблокирована, перевод невозможен");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

    }




}
