package com.viraj.dmabackend.lead.repository;

import com.viraj.dmabackend.lead.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeadRepositoryCustom {

    Page<Lead> searchLeads(
            String keyword,
            Pageable pageable
    );
}