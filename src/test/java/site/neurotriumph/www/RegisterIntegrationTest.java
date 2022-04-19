package site.neurotriumph.www;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import site.neurotriumph.www.constant.Const;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.pojo.ErrorResponseBody;
import site.neurotriumph.www.pojo.RegisterRequestBody;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class RegisterIntegrationTest {
  private final String baseUrl = "/register";

  @Value("${spring.mail.username}")
  private String senderEmail;

  @Value("${spring.mail.password}")
  private String senderPassword;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @RegisterExtension
  private GreenMail greenMail;

  private void startGreenMail() {
    if (greenMail != null)
      greenMail.stop();

    greenMail = new GreenMail(ServerSetupTest.SMTP)
      .withConfiguration(GreenMailConfiguration.aConfig()
        .withUser(senderEmail, senderPassword));

    greenMail.start();
  }

  @Test
  @Sql(value = {"/sql/insert_user.sql"})
  public void shouldReturnUserAlreadyExistsError() throws Exception {
    RegisterRequestBody registerRequestBody = new RegisterRequestBody(senderEmail, "Qwerty123");

    this.mockMvc.perform(post(baseUrl)
        .content(objectMapper.writeValueAsString(registerRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.USER_ALREADY_EXISTS))));
  }

  @Test
  public void shouldReturnPasswordCannotBeBlankError() throws Exception {
    RegisterRequestBody registerRequestBody = new RegisterRequestBody(senderEmail, null);

    this.mockMvc.perform(post(baseUrl)
        .content(objectMapper.writeValueAsString(registerRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.PASSWORD_CANNOT_BE_BLANK))));
  }

  @Test
  public void shouldReturnPasswordTooShortError() throws Exception {
    RegisterRequestBody registerRequestBody = new RegisterRequestBody(senderEmail,
      "a".repeat(Const.MIN_PASSWORD_LENGTH - 1));

    this.mockMvc.perform(post(baseUrl)
        .content(objectMapper.writeValueAsString(registerRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.PASSWORD_IS_TOO_SHORT))));
  }

  @Test
  public void shouldReturnEmailCannotBeBlankError() throws Exception {
    List<String> invalidEmails = new ArrayList<>();
    invalidEmails.add("");
    invalidEmails.add(null);

    RegisterRequestBody registerRequestBody = new RegisterRequestBody(null, "Qwerty123");

    for (String invalidEmail : invalidEmails) {
      registerRequestBody.setEmail(invalidEmail);

      this.mockMvc.perform(post(baseUrl)
          .content(objectMapper.writeValueAsString(registerRequestBody))
          .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.EMAIL_CANNOT_BE_BLANK))));
    }
  }

  @Test
  public void shouldReturnInvalidEmailError() throws Exception {
    List<String> invalidEmails = new ArrayList<>();
    invalidEmails.add("myemail.com");
    invalidEmails.add("@email.com");
    invalidEmails.add("my@");

    RegisterRequestBody registerRequestBody = new RegisterRequestBody(null, "Qwerty123");

    for (String invalidEmail : invalidEmails) {
      registerRequestBody.setEmail(invalidEmail);

      this.mockMvc.perform(post(baseUrl)
          .content(objectMapper.writeValueAsString(registerRequestBody))
          .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.INVALID_EMAIL))));
    }
  }

  @Test
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnOkStatus() throws Exception {
    startGreenMail();

    RegisterRequestBody registerRequestBody = new RegisterRequestBody(senderEmail, "Qwerty123");

    this.mockMvc.perform(post(baseUrl)
        .content(objectMapper.writeValueAsString(registerRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isOk());

    await().atMost(2, SECONDS).untilAsserted(() -> {
      MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
      assertEquals(1, receivedMessages.length);

      MimeMessage receivedMessage = receivedMessages[0];
      assertEquals("Neuro Triumph", receivedMessage.getSubject());
      assertEquals(1, receivedMessage.getAllRecipients().length);
      assertEquals(registerRequestBody.getEmail(),
        receivedMessage.getAllRecipients()[0].toString());

      assertTrue(receivedMessage.getContent().toString().length() > 0);
    });
  }
}
