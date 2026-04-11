package com.example.bankcards.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class TransferRequestDto {
    @NotNull(message = "ID карты отправителя не может быть пустым")
    private Long fromCardId;
    @NotBlank(message = "Номер карты получателя обязателен")
    private String toCardNumber;
    @NotNull(message = "Сумма перевода обязательна")
    @Positive(message = "Сумма перевода должна быть больше нуля")
    private BigDecimal amount;
}
