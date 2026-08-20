package com.booking_ticket_platform.notification.service;

import com.booking_ticket_platform.booking.entity.Booking;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class RealEmailService implements INotificationService {

    private final JavaMailSender mailSender;

    public RealEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPaymentSuccessNotification(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(booking.getUser().getEmail());
            message.setSubject("Xác nhận thanh toán thành công - " + booking.getConcert().getName());
            
            String content = "Chào bạn,\n\n" +
                    "Đơn đặt vé của bạn đã được thanh toán thành công.\n" +
                    "Mã đơn: " + booking.getId() + "\n" +
                    "Sự kiện: " + booking.getConcert().getName() + "\n" +
                    "Tổng tiền: " + booking.getTotalAmount() + " VND\n\n" +
                    "Cảm ơn bạn đã sử dụng hệ thống của chúng tôi!";
            
            message.setText(content);
            mailSender.send(message);
            System.out.println("Email đã được gửi tới: " + booking.getUser().getEmail());
        } catch (Exception e) {
            System.err.println("Gửi email thất bại: " + e.getMessage());
            // In a real app, you might want to save this to a queue to retry later
        }
    }
}
