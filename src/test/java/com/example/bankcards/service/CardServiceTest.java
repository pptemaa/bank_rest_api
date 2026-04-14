package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.TransferException;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {
    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private CardService cardService;

    @Test
    void testCreateCard() {
        User user = new User();
        user.setUsername("user1");
        when(userService.getUserById(1L)).thenReturn(user);
        when(cardRepository.existsByCardNumber(any())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Card result = cardService.createCardForUser(1L);

        assertNotNull(result.getCardNumber());
        assertEquals(16, result.getCardNumber().length());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals(user, result.getUser());
        assertNotNull(result.getExpirationDate());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void testGetCardsPage() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<Card> expectedPage = new PageImpl<>(List.of(new Card(), new Card()), pageable, 2);

        when(cardRepository.findAllByUserId(10L, pageable)).thenReturn(expectedPage);

        Page<Card> result = cardService.getCardsByUserId(10L, pageable);

        assertEquals(2, result.getTotalElements());
        verify(userService).getUserById(10L);
        verify(cardRepository).findAllByUserId(10L, pageable);
    }

    @Test
    void testBlockOwnCard() {
        User owner = new User();

        Card card = new Card();
        card.setUser(owner);
        card.setStatus(CardStatus.ACTIVE);

        setUserId(owner, 7L);

        when(cardRepository.findById(20L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Card result = cardService.requestBlockCard(20L, 7L);

        assertEquals(CardStatus.BLOCKED, result.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void testBlockForeignCard() {
        User owner = new User();
        setUserId(owner, 9L);

        Card card = new Card();
        card.setUser(owner);

        when(cardRepository.findById(30L)).thenReturn(Optional.of(card));

        Executable action = () -> cardService.requestBlockCard(30L, 3L);

        assertThrows(TransferException.class, action);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void testTransferSuccess() {
        User owner = new User();
        setUserId(owner, 15L);

        Card from = new Card();
        from.setUser(owner);
        from.setStatus(CardStatus.ACTIVE);
        from.setBalance(new BigDecimal("150.00"));

        Card to = new Card();
        to.setUser(owner);
        to.setStatus(CardStatus.ACTIVE);
        to.setBalance(new BigDecimal("30.00"));
        to.setCardNumber("1111222233334444");

        when(cardRepository.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepository.findByCardNumber("1111222233334444")).thenReturn(Optional.of(to));
        cardService.transferMoney(15L, 1L, "1111222233334444", new BigDecimal("50.00"));
        assertEquals(new BigDecimal("100.00"), from.getBalance());
        assertEquals(new BigDecimal("80.00"), to.getBalance());
    }

    @Test
    void testTransferForeignCards() {
        User owner = new User();
        setUserId(owner, 1L);
        User another = new User();
        setUserId(another, 2L);

        Card from = new Card();
        from.setUser(owner);
        from.setStatus(CardStatus.ACTIVE);
        from.setBalance(new BigDecimal("100.00"));

        Card to = new Card();
        to.setUser(another);
        to.setStatus(CardStatus.ACTIVE);
        to.setCardNumber("9999000011112222");

        when(cardRepository.findById(100L)).thenReturn(Optional.of(from));
        when(cardRepository.findByCardNumber("9999000011112222")).thenReturn(Optional.of(to));
        assertThrows(TransferException.class,
                () -> cardService.transferMoney(1L, 100L, "9999000011112222", new BigDecimal("10.00")));
    }

    @Test
    void testTransferNoMoney() {
        User owner = new User();
        setUserId(owner, 1L);

        Card from = new Card();
        from.setUser(owner);
        from.setStatus(CardStatus.ACTIVE);
        from.setBalance(new BigDecimal("10.00"));

        Card to = new Card();
        to.setUser(owner);
        to.setStatus(CardStatus.ACTIVE);
        to.setCardNumber("1234123412341234");

        when(cardRepository.findById(5L)).thenReturn(Optional.of(from));
        when(cardRepository.findByCardNumber("1234123412341234")).thenReturn(Optional.of(to));

        assertThrows(TransferException.class,
                () -> cardService.transferMoney(1L, 5L, "1234123412341234", new BigDecimal("50.00")));
    }

    @Test
    void testActivateNotFound() {
        when(cardRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cardService.activateCard(99L));
    }

    private void setUserId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Не удалось подготовить тестовые данные: " + e.getMessage());
        }
    }
}
