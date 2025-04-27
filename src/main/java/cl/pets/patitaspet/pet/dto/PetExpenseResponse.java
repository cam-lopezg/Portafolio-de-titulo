package cl.pets.patitaspet.pet.dto;

import cl.pets.patitaspet.pet.entity.ExpenseCategory;

import java.math.BigDecimal;

public class PetExpenseResponse {
    private Long id;
    private Long petId;
    private String petName;
    private String title;
    private String description;
    private BigDecimal amount;
    private String currencyCode;
    private ExpenseCategory category;
    private String categoryDisplay; // Nombre formateado de la categoría
    private String date;
    private String receiptImageUrl;
    private String vendorName;
    private String createdAt;

    public PetExpenseResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
        this.categoryDisplay = formatCategoryName(category);
    }

    public String getCategoryDisplay() {
        return categoryDisplay;
    }

    public void setCategoryDisplay(String categoryDisplay) {
        this.categoryDisplay = categoryDisplay;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReceiptImageUrl() {
        return receiptImageUrl;
    }

    public void setReceiptImageUrl(String receiptImageUrl) {
        this.receiptImageUrl = receiptImageUrl;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Método utilitario para formatear el nombre de la categoría
    private String formatCategoryName(ExpenseCategory category) {
        if (category == null) {
            return "";
        }

        String name = category.name();
        return name.substring(0, 1) + name.substring(1).toLowerCase().replace('_', ' ');
    }
}