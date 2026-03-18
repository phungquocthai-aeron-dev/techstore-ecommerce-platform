package com.techstore.notification.service;

import java.io.UnsupportedEncodingException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.techstore.notification.dto.request.SendEmailRequest;
import com.techstore.notification.dto.response.EmailResponse;
import com.techstore.notification.exception.AppException;
import com.techstore.notification.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {

    JavaMailSender mailSender;

    public EmailResponse sendEmail(SendEmailRequest request) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(
                    "phungquocthai0027@gmail.com", "Tech Store - Hệ thống bán hàng thiết bị công nghệ trực tuyến");
            helper.setTo(request.getTo().getEmail());
            helper.setSubject(request.getSubject());
            helper.setText(request.getHtmlContent(), true);

            mailSender.send(message);

            return EmailResponse.builder()
                    .messageId("SENT-" + System.currentTimeMillis())
                    .build();

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new AppException(ErrorCode.CANNOT_SEND_EMAIL);
        }
    }

    public void sendOtpEmail(String toEmail, String otp) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(
                    "phungquocthai0027@gmail.com", "Tech Store - Hệ thống bán hàng thiết bị công nghệ trực tuyến");
            helper.setTo(toEmail);
            helper.setSubject("Mã xác nhận đặt lại mật khẩu");
            helper.setText(buildOtpEmailTemplate(otp), true);

            mailSender.send(message);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new AppException(ErrorCode.CANNOT_SEND_EMAIL);
        }
    }

    // Template HTML cho email OTP
    private String buildOtpEmailTemplate(String otp) {
        return """
			<div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto;">
				<h2 style="color: #4CAF50;">Đặt lại mật khẩu</h2>
				<p>Mã OTP của bạn là:</p>
				<div style="font-size: 36px; font-weight: bold; color: #333;
							letter-spacing: 8px; text-align: center;
							padding: 20px; background: #f5f5f5; border-radius: 8px;">
					%s
				</div>
				<p style="color: #888;">Mã có hiệu lực trong <strong>5 phút</strong>.</p>
				<p style="color: #888;">Nếu bạn không yêu cầu, hãy bỏ qua email này.</p>
			</div>
		"""
                .formatted(otp);
    }
}
