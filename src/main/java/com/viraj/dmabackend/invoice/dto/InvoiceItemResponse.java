package com.viraj.dmabackend.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemResponse {

    private String description;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal lineTotal;
}