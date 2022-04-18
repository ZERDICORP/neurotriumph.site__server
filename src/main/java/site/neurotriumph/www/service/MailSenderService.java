package site.neurotriumph.www.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailSenderService {
  @Value("${spring.mail.username}")
  private String senderEmail;

  @Autowired
  private JavaMailSender javaMailSender;

  public void send(String to, String subject, String body) {
    SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    simpleMailMessage.setFrom(senderEmail);
    simpleMailMessage.setTo(to);
    simpleMailMessage.setSubject(subject);
    simpleMailMessage.setText(body);

    javaMailSender.send(simpleMailMessage);
  }
}