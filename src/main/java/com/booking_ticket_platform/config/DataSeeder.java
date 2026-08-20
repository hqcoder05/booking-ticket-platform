package com.booking_ticket_platform.config;

import com.booking_ticket_platform.auth.entity.User;
import com.booking_ticket_platform.auth.repository.UserRepository;
import com.booking_ticket_platform.concert.entity.Concert;
import com.booking_ticket_platform.concert.entity.Seat;
import com.booking_ticket_platform.concert.entity.TicketCategory;
import com.booking_ticket_platform.concert.repository.IConcertRepository;
import com.booking_ticket_platform.concert.repository.ISeatRepository;
import com.booking_ticket_platform.concert.repository.ITicketCategoryRepository;
import com.booking_ticket_platform.payment.entity.Voucher;
import com.booking_ticket_platform.payment.repository.IVoucherRepository;
import com.booking_ticket_platform.venue.entity.Venue;
import com.booking_ticket_platform.venue.repository.IVenueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * DataSeeder chạy mỗi lần Spring Boot khởi động.
 * Tự động kiểm tra và seed dữ liệu mẫu nếu database trống.
 * Bao gồm: Users (ADMIN, OPERATOR, CUSTOMER), Venues, Concerts,
 * Ticket Categories, Seats, và Vouchers.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final IVenueRepository venueRepository;
    private final IConcertRepository concertRepository;
    private final ITicketCategoryRepository ticketCategoryRepository;
    private final ISeatRepository seatRepository;
    private final IVoucherRepository voucherRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      IVenueRepository venueRepository,
                      IConcertRepository concertRepository,
                      ITicketCategoryRepository ticketCategoryRepository,
                      ISeatRepository seatRepository,
                      IVoucherRepository voucherRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
        this.concertRepository = concertRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.seatRepository = seatRepository;
        this.voucherRepository = voucherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedVenuesAndConcerts();
        seedVouchers();
    }

    private void seedUsers() {
        String encodedPassword = passwordEncoder.encode("admin123");

        createUserIfNotExists("admin@geekup.vn", encodedPassword, "ADMIN");
        createUserIfNotExists("operator@geekup.vn", encodedPassword, "OPERATOR");
        createUserIfNotExists("customer@geekup.vn", encodedPassword, "CUSTOMER");
    }

    private void createUserIfNotExists(String email, String encodedPassword, String role) {
        if (!userRepository.existsByEmail(email)) {
            User user = User.builder()
                    .email(email)
                    .passwordHash(encodedPassword)
                    .role(role)
                    .build();
            userRepository.save(user);
            log.info("[DataSeeder] Created {} account: {}", role, email);
        }
    }

    private void seedVenuesAndConcerts() {
        if (concertRepository.count() > 0) {
            log.info("[DataSeeder] Concerts already exist, skipping venue/concert seed.");
            return;
        }

        // --- Venues ---
        Venue myDinh = venueRepository.save(Venue.builder()
                .name("Sân vận động Quốc gia Mỹ Đình")
                .address("Đường Lê Đức Thọ, Nam Từ Liêm")
                .city("Hà Nội")
                .capacity(40000)
                .build());

        Venue hoaBinh = venueRepository.save(Venue.builder()
                .name("Nhà hát Hòa Bình")
                .address("240 Đường 3/2, Quận 10")
                .city("TP. Hồ Chí Minh")
                .capacity(2500)
                .build());

        Venue phuTho = venueRepository.save(Venue.builder()
                .name("Phú Thọ Indoor Stadium")
                .address("1 Lữ Gia, Quận 11")
                .city("TP. Hồ Chí Minh")
                .capacity(5000)
                .build());

        log.info("[DataSeeder] Created 3 venues.");

        // --- Concert 1: FC Bayern Munich x BTS (PUBLISHED) ---
        Concert bts = concertRepository.save(Concert.builder()
                .venue(myDinh)
                .name("FC Bayern Munich x BTS - World Tour 2027")
                .eventDate(OffsetDateTime.of(2027, 3, 15, 19, 0, 0, 0, ZoneOffset.ofHours(7)))
                .status("PUBLISHED")
                .stageLayout("CENTER")
                .build());

        TicketCategory btsVVIP = createCategory(bts, "VVIP - Sân khấu gần nhất", "SEATED", 5000000, 100);
        TicketCategory btsVIP = createCategory(bts, "VIP - Khán đài A", "SEATED", 3000000, 200);
        TicketCategory btsStd = createCategory(bts, "Standard - Khán đài B", "SEATED", 1500000, 500);
        createCategory(bts, "Standing - Sân cỏ", "STANDING", 800000, 5000);

        createSeats(btsVVIP, "VVIP-A", 100);
        createSeats(btsVIP, "VIP-B", 200);
        createSeats(btsStd, "STD-C", 500);

        // --- Concert 2: BLACKPINK (PUBLISHED) ---
        Concert blackpink = concertRepository.save(Concert.builder()
                .venue(phuTho)
                .name("BLACKPINK - Born Pink World Tour Finale")
                .eventDate(OffsetDateTime.of(2027, 4, 20, 20, 0, 0, 0, ZoneOffset.ofHours(7)))
                .status("PUBLISHED")
                .stageLayout("FRONT")
                .build());

        TicketCategory bpVIP = createCategory(blackpink, "VIP - Hàng đầu", "SEATED", 4000000, 100);
        TicketCategory bpStd = createCategory(blackpink, "Standard - Khu A", "SEATED", 2000000, 300);
        TicketCategory bpEco = createCategory(blackpink, "Economy - Khu B", "SEATED", 1000000, 600);

        createSeats(bpVIP, "VIP-", 100);
        createSeats(bpStd, "STD-", 300);
        createSeats(bpEco, "ECO-", 600);

        // --- Concert 3: Sơn Tùng M-TP (DRAFT) ---
        Concert sonTung = concertRepository.save(Concert.builder()
                .venue(hoaBinh)
                .name("Sơn Tùng M-TP - Sky Tour 2027")
                .eventDate(OffsetDateTime.of(2027, 6, 10, 19, 30, 0, 0, ZoneOffset.ofHours(7)))
                .status("DRAFT")
                .stageLayout("FRONT")
                .build());

        TicketCategory stVIP = createCategory(sonTung, "VIP", "SEATED", 2500000, 200);
        TicketCategory stStd = createCategory(sonTung, "Standard", "SEATED", 1200000, 800);

        createSeats(stVIP, "VIP-", 200);
        createSeats(stStd, "STD-", 800);

        log.info("[DataSeeder] Created 3 concerts with {} total seats.", seatRepository.count());
    }

    private TicketCategory createCategory(Concert concert, String name, String type, long price, int qty) {
        return ticketCategoryRepository.save(TicketCategory.builder()
                .concert(concert)
                .name(name)
                .type(type)
                .price(BigDecimal.valueOf(price))
                .totalQuantity(qty)
                .availableQuantity(qty)
                .build());
    }

    private void createSeats(TicketCategory category, String prefix, int count) {
        for (int i = 1; i <= count; i++) {
            seatRepository.save(Seat.builder()
                    .ticketCategory(category)
                    .seatNumber(prefix + i)
                    .status("AVAILABLE")
                    .build());
        }
    }

    private void seedVouchers() {
        if (voucherRepository.count() > 0) {
            log.info("[DataSeeder] Vouchers already exist, skipping voucher seed.");
            return;
        }

        voucherRepository.save(Voucher.builder().code("EARLYBIRD").discountType("PERCENTAGE").discountValue(BigDecimal.valueOf(10)).maxUsage(100).currentUsage(0).build());
        voucherRepository.save(Voucher.builder().code("FLASHSALE50K").discountType("FIXED_AMOUNT").discountValue(BigDecimal.valueOf(50000)).maxUsage(200).currentUsage(0).build());
        voucherRepository.save(Voucher.builder().code("VIP20").discountType("PERCENTAGE").discountValue(BigDecimal.valueOf(20)).maxUsage(50).currentUsage(0).build());
        voucherRepository.save(Voucher.builder().code("GEEKUP100K").discountType("FIXED_AMOUNT").discountValue(BigDecimal.valueOf(100000)).maxUsage(30).currentUsage(0).build());

        log.info("[DataSeeder] Created 4 vouchers.");
    }
}
