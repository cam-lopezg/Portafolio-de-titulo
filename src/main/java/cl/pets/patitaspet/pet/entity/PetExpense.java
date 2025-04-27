package cl.pets.patitaspet.pet.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class PetExpense {
    private Long id;
    private Long petId;
    private String title;
    private String description;
    private BigDecimal amount;
    private String amountStr; // Para almacenamiento en Firestore
    private String currencyCode; // Por ejemplo: CLP, USD
    private ExpenseCategory category;
    private String dateStr; // Fecha del gasto en formato ISO
    private String receiptImageUrl; // URL de la imagen del recibo/boleta
    private String vendorName; // Nombre del proveedor o tienda
    private LocalDateTime createdAt;
    private String createdAtStr; // Para almacenamiento en Firestore
}