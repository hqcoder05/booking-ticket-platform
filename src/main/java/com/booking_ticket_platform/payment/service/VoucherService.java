package com.booking_ticket_platform.payment.service;

import com.booking_ticket_platform.payment.entity.Voucher;
import com.booking_ticket_platform.payment.repository.IVoucherRepository;
import com.booking_ticket_platform.shared.exception.DuplicateResourceException;
import com.booking_ticket_platform.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VoucherService {

    private final IVoucherRepository voucherRepository;

    public VoucherService(IVoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    public Voucher createVoucher(com.booking_ticket_platform.payment.dto.VoucherCreateRequest request) {
        if (voucherRepository.findAll().stream()
                .anyMatch(v -> v.getCode().equalsIgnoreCase(request.getCode()))) {
            throw new DuplicateResourceException("Voucher code '" + request.getCode() + "' already exists");
        }
        Voucher voucher = Voucher.builder()
                .code(request.getCode())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxUsage(request.getMaxUsage())
                .currentUsage(0)
                .build();
        return voucherRepository.save(voucher);
    }

    public Voucher getVoucherById(UUID id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
    }

    public void deleteVoucher(UUID id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
        if (voucher.getCurrentUsage() > 0) {
            throw new IllegalStateException("Cannot delete voucher that has been used");
        }
        voucherRepository.delete(voucher);
    }
}
