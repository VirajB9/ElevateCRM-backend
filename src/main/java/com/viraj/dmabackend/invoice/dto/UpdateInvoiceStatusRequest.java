package com.viraj.dmabackend.invoice.dto;

import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInvoiceStatusRequest {

    @NotNull(message = "Invoice status is required")
    private InvoiceStatus status;
}
