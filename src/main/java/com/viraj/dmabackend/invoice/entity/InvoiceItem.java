package com.viraj.dmabackend.invoice.entity;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {

    private String description;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal lineTotal;
}