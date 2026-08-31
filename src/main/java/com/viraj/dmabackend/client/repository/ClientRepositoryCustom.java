package com.viraj.dmabackend.client.repository;

import com.viraj.dmabackend.client.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientRepositoryCustom {
    Page<Client> searchClients(String keyword, Pageable pageable);
}
