package com.insurance.claims.config;

import com.insurance.claims.model.Claim;
import com.insurance.claims.model.ClaimDocument;
import com.insurance.claims.model.ClaimState;
import com.insurance.claims.model.ClaimStatus;
import com.insurance.claims.model.Policy;
import com.insurance.claims.repository.ClaimDocumentRepository;
import com.insurance.claims.repository.ClaimRepository;
import com.insurance.claims.repository.PolicyRepository;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Seeds the database on first run: a small set of curated, cross-referenced
 * records (the named demo data) plus a larger batch of Datafaker-generated rows
 * for realistic volume. Runs only when the policies table is empty, so it does
 * not duplicate data on restart against the persistent H2 file.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String[] COVERAGE_TYPES = {
            "Auto Insurance", "Home Insurance", "Health Insurance", "Life Insurance", "Travel Insurance"
    };
    private static final BigDecimal[] COVERAGE_PCTS = {
            new BigDecimal("0.80"), new BigDecimal("0.85"), new BigDecimal("0.90"), new BigDecimal("0.95")
    };

    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final ClaimDocumentRepository documentRepository;

    private final Faker faker = new Faker();
    private final Random random = new Random(42); // fixed seed -> reproducible bulk data
    private final Set<String> usedPolicyNumbers = new HashSet<>();

    public DataSeeder(PolicyRepository policyRepository, ClaimRepository claimRepository,
                      ClaimDocumentRepository documentRepository) {
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    public void run(String... args) {
        if (policyRepository.count() > 0) {
            log.info("Database already seeded ({} policies); skipping seed.", policyRepository.count());
            return;
        }
        log.info("Seeding database with curated + generated mock data...");
        seedCurated();
        seedBulk(50, 150);
        log.info("Seed complete: {} policies, {} claims, {} document analyses.",
                policyRepository.count(), claimRepository.count(), documentRepository.count());
    }

    // ---- Curated, narratively coherent demo data -------------------------------

    private void seedCurated() {
        savePolicy("POL123456", "John Smith", "Auto Insurance", "50000", "1200", "0.95",
                LocalDate.of(2023, 1, 15), LocalDate.of(2025, 1, 15), "Active");
        savePolicy("POL789012", "Sarah Johnson", "Home Insurance", "250000", "2400", "0.90",
                LocalDate.of(2022, 6, 1), LocalDate.of(2025, 6, 1), "Active");
        savePolicy("POL345678", "Michael Brown", "Health Insurance", "100000", "800", "0.85",
                LocalDate.of(2023, 3, 10), LocalDate.of(2025, 3, 10), "Active");
        savePolicy("POL567890", "Emily Davis", "Auto Insurance", "60000", "1400", "0.95",
                LocalDate.of(2023, 7, 1), LocalDate.of(2025, 7, 1), "Active");
        savePolicy("POL246810", "Robert Wilson", "Home Insurance", "300000", "2800", "0.90",
                LocalDate.of(2023, 2, 20), LocalDate.of(2025, 2, 20), "Active");

        saveClaim("CLM001", "POL123456", "John Smith", "Car accident repair", "15000",
                ClaimStatus.PAID, LocalDate.of(2023, 5, 12));
        saveClaim("CLM002", "POL123456", "John Smith", "Tire replacement", "300",
                ClaimStatus.APPROVED, LocalDate.of(2023, 10, 22));
        saveClaim("CLM003", "POL789012", "Sarah Johnson", "Water damage repair", "45000",
                ClaimStatus.PAID, LocalDate.of(2023, 2, 15));
        saveClaim("CLM004", "POL345678", "Michael Brown", "Medical consultation", "150",
                ClaimStatus.APPROVED, LocalDate.of(2023, 8, 30));

        saveDocument("CLM001", "Accident report and repair estimates analyzed",
                "Vehicle damage estimated at $15,000, with valid accident report",
                "Accident Report, Repair Estimates", "92%", "High",
                "Low risk - Valid claim with supporting documents");
        saveDocument("CLM002", "Tire replacement claim with receipt",
                "Tire replacement receipt shows $300 expenditure",
                "Receipt, Tire inspection report", "85%", "Medium",
                "Low risk - Valid claim with supporting documentation");
        saveDocument("CLM003", "Water damage repair estimate",
                "Water damage assessment shows $45,000 in damages",
                "Damage assessment report, Photos", "95%", "High",
                "Low risk - Valid claim with supporting documentation");
    }

    // ---- Datafaker-generated bulk ---------------------------------------------

    private void seedBulk(int policyCount, int claimCount) {
        List<Policy> generated = new ArrayList<>();
        for (int i = 0; i < policyCount; i++) {
            String number = uniquePolicyNumber();
            LocalDate effective = LocalDate.now().minusDays(random.nextInt(900) + 30);
            generated.add(savePolicy(
                    number,
                    faker.name().fullName(),
                    COVERAGE_TYPES[random.nextInt(COVERAGE_TYPES.length)],
                    String.valueOf((random.nextInt(45) + 5) * 10000),       // 50k .. 500k
                    String.valueOf((random.nextInt(30) + 5) * 100),         // 500 .. 3400
                    COVERAGE_PCTS[random.nextInt(COVERAGE_PCTS.length)].toPlainString(),
                    effective,
                    effective.plusYears(1),
                    random.nextInt(10) < 8 ? "Active" : "Expired"));
        }

        ClaimStatus[] statuses = ClaimStatus.values();
        for (int i = 0; i < claimCount; i++) {
            Policy p = generated.get(random.nextInt(generated.size()));
            String ref = String.format("CLM%04d", 1000 + i);
            LocalDate filed = LocalDate.now().minusDays(random.nextInt(700) + 1);
            saveClaim(ref, p.getPolicyNumber(), p.getPolicyHolder(),
                    faker.lorem().sentence(4).replace(".", ""),
                    String.valueOf((random.nextInt(490) + 1) * 100),       // 100 .. 49000
                    statuses[random.nextInt(statuses.length)],
                    filed);
        }
    }

    private String uniquePolicyNumber() {
        String number;
        do {
            number = "POL" + (100000 + random.nextInt(900000));
        } while (!usedPolicyNumbers.add(number) || policyRepository.existsByPolicyNumber(number));
        return number;
    }

    // ---- Persistence helpers ---------------------------------------------------

    private Policy savePolicy(String number, String holder, String type, String coverage, String premium,
                              String pct, LocalDate effective, LocalDate expiry, String status) {
        usedPolicyNumbers.add(number);
        Policy p = new Policy(number, holder, type, new BigDecimal(coverage), new BigDecimal(premium),
                new BigDecimal(pct), effective, expiry, status);
        return policyRepository.save(p);
    }

    private void saveClaim(String reference, String policyNumber, String claimant, String description,
                           String amount, ClaimStatus status, LocalDate filedOn) {
        Claim c = new Claim();
        c.setReference(reference);
        c.setPolicyNumber(policyNumber);
        c.setClaimantName(claimant);
        c.setClaimDescription(description);
        c.setClaimAmount(new BigDecimal(amount));
        c.setStatus(status);
        c.setState(stateFor(status));
        c.setCreatedAt(filedOn.atTime(9, 0));
        claimRepository.save(c);
    }

    private void saveDocument(String claimRef, String summary, String findings, String evidence,
                              String relevance, String confidence, String risk) {
        documentRepository.save(new ClaimDocument(claimRef, summary, findings, evidence,
                relevance, confidence, risk));
    }

    private ClaimState stateFor(ClaimStatus status) {
        return switch (status) {
            case PAID -> ClaimState.PAID;
            case APPROVED -> ClaimState.APPROVED;
            case REJECTED -> ClaimState.REJECTED;
            case INVESTIGATING -> ClaimState.INVESTIGATION;
            case PENDING -> ClaimState.SUBMITTED;
        };
    }
}
