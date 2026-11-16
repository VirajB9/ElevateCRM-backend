package com.viraj.dmabackend.integration;

import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.entity.RefreshToken;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.enums.PermissionType;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import com.viraj.dmabackend.auth.repository.RefreshTokenRepository;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.common.counter.CounterRepository;
import com.viraj.dmabackend.invoice.entity.InvoiceHistory;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.repository.InvoiceHistoryRepository;
import com.viraj.dmabackend.menu.entity.Menu;
import com.viraj.dmabackend.menu.repository.MenuRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoriesIntegrationTest extends AbstractMongoIntegrationTest {

    @Autowired
    private CounterRepository counterRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private InvoiceHistoryRepository invoiceHistoryRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @AfterEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        menuRepository.deleteAll();
        invoiceHistoryRepository.deleteAll();
    }

    @Test
    void counterRepository_generatesAtomicSequentialValuesAndDecrements() {
        String seqName = "test_sequence_" + System.currentTimeMillis();
        long seq1 = counterRepository.getNextSequence(seqName);
        long seq2 = counterRepository.getNextSequence(seqName);

        assertThat(seq1).isEqualTo(1L);
        assertThat(seq2).isEqualTo(2L);

        boolean decremented = counterRepository.decrementSequenceIfCurrent(seqName, 2L);
        assertThat(decremented).isTrue();

        long seq3 = counterRepository.getNextSequence(seqName);
        assertThat(seq3).isEqualTo(2L);
    }

    @Test
    void refreshTokenRepository_persistsAndQueriesHashedTokens() {
        String hash = "hash_" + System.currentTimeMillis();
        RefreshToken token = RefreshToken.builder()
                .userId("u-123")
                .tokenHash(hash)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .revoked(false)
                .build();

        refreshTokenRepository.save(token);

        Optional<RefreshToken> found = refreshTokenRepository.findByTokenHash(hash);
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo("u-123");

        Optional<RefreshToken> active = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash);
        assertThat(active).isPresent();

        refreshTokenRepository.deleteByUserId("u-123");
        assertThat(refreshTokenRepository.findByTokenHash(hash)).isEmpty();
    }

    @Test
    void roleAndPermissionRepository_persistsAndFindsByNaturalKeys() {
        Optional<Permission> existingPermission = permissionRepository.findByPermissionType(PermissionType.USER_READ);
        assertThat(existingPermission).isPresent();
        assertThat(permissionRepository.existsByPermissionType(PermissionType.USER_READ)).isTrue();

        Role role = Role.builder()
                .name("INTEGRATION_TEST_ROLE")
                .description("Test role")
                .permissionIds(List.of(existingPermission.get().getId()))
                .build();

        roleRepository.save(role);
        Optional<Role> foundRole = roleRepository.findByName("INTEGRATION_TEST_ROLE");
        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getPermissionIds()).contains(existingPermission.get().getId());

        roleRepository.delete(role);
    }

    @Test
    void menuRepository_managesHierarchyAndActiveOrderIndexSorting() {
        Menu parent = Menu.builder()
                .title("Settings Root")
                .path("/settings-test")
                .icon("settings")
                .orderIndex(1)
                .active(true)
                .build();
        Menu savedParent = menuRepository.save(parent);

        Menu child = Menu.builder()
                .title("Sub Settings")
                .path("/settings-test/sub")
                .icon("sub")
                .parentId(savedParent.getId())
                .orderIndex(2)
                .active(true)
                .build();
        menuRepository.save(child);

        assertThat(menuRepository.existsByParentIdAndActiveTrue(savedParent.getId())).isTrue();
        List<Menu> activeMenus = menuRepository.findByActiveTrueOrderByOrderIndexAsc();
        assertThat(activeMenus).isNotEmpty();
        assertThat(menuRepository.findByPathAndActiveTrue("/settings-test")).isPresent();
    }

    @Test
    void invoiceHistoryRepository_persistsSnapshotsAndOrdersByVersionAsc() {
        InvoiceHistory h1 = InvoiceHistory.builder()
                .invoiceId("inv-hist-1")
                .invoiceVersion(1L)
                .status(InvoiceStatus.DRAFT)
                .totalAmount(new BigDecimal("100.00"))
                .changedAt(LocalDateTime.now())
                .changedBy("user-1")
                .build();

        InvoiceHistory h2 = InvoiceHistory.builder()
                .invoiceId("inv-hist-1")
                .invoiceVersion(2L)
                .status(InvoiceStatus.SENT)
                .totalAmount(new BigDecimal("100.00"))
                .changedAt(LocalDateTime.now().plusSeconds(10))
                .changedBy("user-1")
                .build();

        invoiceHistoryRepository.saveAll(List.of(h2, h1)); // save out of order

        List<InvoiceHistory> historyList = invoiceHistoryRepository.findByInvoiceIdOrderByInvoiceVersionAsc("inv-hist-1");
        assertThat(historyList).hasSize(2);
        assertThat(historyList.get(0).getInvoiceVersion()).isEqualTo(1L);
        assertThat(historyList.get(1).getInvoiceVersion()).isEqualTo(2L);

        Page<InvoiceHistory> page = invoiceHistoryRepository.findByInvoiceIdOrderByInvoiceVersionAsc("inv-hist-1", PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(2);
    }
}
