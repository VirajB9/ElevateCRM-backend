package com.viraj.dmabackend.invoice.mapper;

import com.viraj.dmabackend.invoice.dto.InvoiceResponse;
import com.viraj.dmabackend.invoice.entity.Invoice;
import com.viraj.dmabackend.invoice.entity.InvoiceItem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class InvoiceMapperTest {

    private final InvoiceMapper invoiceMapper = new InvoiceMapper();

    @Test
    void toResponse_whenItemsListIsNull_mapsToEmptyListWithoutNPE() {
        Invoice invoice = Invoice.builder()
                .id("inv-1")
                .invoiceNumber("INV-2026-00001")
                .items(null)
                .build();

        InvoiceResponse response = invoiceMapper.toResponse(invoice);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).isEmpty();
    }

    @Test
    void toResponse_whenItemsContainNullElement_ignoresNullWithoutNPE() {
        List<InvoiceItem> items = new ArrayList<>();
        items.add(null);
        items.add(InvoiceItem.builder().description("Dev").build());

        Invoice invoice = Invoice.builder()
                .id("inv-2")
                .invoiceNumber("INV-2026-00002")
                .items(items)
                .build();

        InvoiceResponse response = invoiceMapper.toResponse(invoice);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getDescription()).isEqualTo("Dev");
    }
}
