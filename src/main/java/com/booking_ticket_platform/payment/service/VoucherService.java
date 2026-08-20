package com.booking_ticket_platform.payment.service;

import com.booking_ticket_platform.payment.entity.Voucher;
import com.booking_ticket_platform.payment.repository.IVoucherRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VoucherService {

    private final IVoucherRepository voucherRepository;

    public VoucherService(IVoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }
}
