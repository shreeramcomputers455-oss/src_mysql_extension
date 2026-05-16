package com.ultradb;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.*;
import java.util.regex.Pattern;

/**
 * MailManager - Email Management with Validation
 * Handles email sending with CC/BCC, validation, and header sanitization
 */
public class MailManager {

  private String fromEmail;
  private String fromName;
  private String replyTo;

  public MailManager() {
    this.fromEmail = "noreply@ultradb.com";
    this.fromName = "UltraSecureDB";
    this.replyTo = "support@ultradb.com";
  }

  /**
   * Validate email address (RFC 5322 simplified)
   */
  public boolean validateEmail(String email) {
    if (email == null || email.isEmpty()) {
      return false;
    }

    String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    return Pattern.matches(emailPattern, email.trim());
  }

  /**
   * Validate email list (comma-separated)
   */
  public boolean validateEmailList(String emailList) {
    if (emailList == null || emailList.isEmpty()) {
      return true; // Empty list is valid
    }

    String[] emails = emailList.split(",");
    for (String email : emails) {
      if (!validateEmail(email.trim())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Sanitize header value (prevent header injection)
   */
  private String sanitizeHeader(String header) {
    if (header == null) {
      return "";
    }

    // Remove newlines and carriage returns
    return header.replaceAll("[\\r\\n]", "").trim();
  }

  /**
   * Build safe email headers
   */
  public JSONObject buildHeaders(String to, String subject, String cc, String bcc, 
                                  String replyTo, boolean isHtml) {
    JSONObject headers = new JSONObject();

    try {
      // Validate recipients
      if (!validateEmail(to)) {
        throw new IllegalArgumentException("Invalid To address");
      }

      // Sanitize all headers
      headers.put("To", sanitizeHeader(to));
      headers.put("Subject", sanitizeHeader(subject));
      headers.put("From", sanitizeHeader(fromName + " <" + fromEmail + ">"));

      // Optional CC
      if (cc != null && !cc.isEmpty()) {
        if (!validateEmailList(cc)) {
          throw new IllegalArgumentException("Invalid CC address");
        }
        headers.put("Cc", sanitizeHeader(cc));
      }

      // Optional BCC
      if (bcc != null && !bcc.isEmpty()) {
        if (!validateEmailList(bcc)) {
          throw new IllegalArgumentException("Invalid BCC address");
        }
        headers.put("Bcc", sanitizeHeader(bcc));
      }

      // Reply-To
      if (replyTo != null && !replyTo.isEmpty()) {
        if (!validateEmail(replyTo)) {
          throw new IllegalArgumentException("Invalid Reply-To address");
        }
        headers.put("Reply-To", sanitizeHeader(replyTo));
      }

      // Content type
      headers.put("Content-Type", isHtml ? "text/html; charset=UTF-8" : "text/plain; charset=UTF-8");

      // Security headers
      headers.put("X-Mailer", "UltraSecureDB");
      headers.put("X-Priority", "3");

    } catch (Exception e) {
      LogManager.error("MailManager", "Header building failed: " + e.getMessage());
    }

    return headers;
  }

  /**
   * Format email message
   */
  public String formatMessage(String message, boolean isHtml) {
    if (message == null) {
      return "";
    }

    if (isHtml) {
      // Ensure proper HTML structure
      if (!message.toLowerCase().contains("<html")) {
        return "<!DOCTYPE html><html><body>" + message + "</body></html>";
      }
      return message;
    } else {
      // Plain text - ensure newlines are preserved
      return message;
    }
  }

  /**
   * Create mail data JSON
   */
  public JSONObject createMailData(String to, String subject, String message, 
                                    String cc, String bcc, String replyTo, boolean isHtml) {
    JSONObject mailData = new JSONObject();

    try {
      // Validate recipients
      if (!validateEmail(to)) {
        throw new IllegalArgumentException("Invalid recipient email");
      }

      if (cc != null && !cc.isEmpty() && !validateEmailList(cc)) {
        throw new IllegalArgumentException("Invalid CC email");
      }

      if (bcc != null && !bcc.isEmpty() && !validateEmailList(bcc)) {
        throw new IllegalArgumentException("Invalid BCC email");
      }

      if (replyTo != null && !replyTo.isEmpty() && !validateEmail(replyTo)) {
        throw new IllegalArgumentException("Invalid Reply-To email");
      }

      // Build headers
      JSONObject headers = buildHeaders(to, subject, cc, bcc, replyTo, isHtml);

      // Add mail data
      mailData.put("to", to);
      mailData.put("subject", sanitizeHeader(subject));
      mailData.put("message", formatMessage(message, isHtml));
      mailData.put("from_email", fromEmail);
      mailData.put("from_name", fromName);
      mailData.put("headers", headers);
      mailData.put("is_html", isHtml);

      if (cc != null && !cc.isEmpty()) {
        mailData.put("cc", cc);
      }

      if (bcc != null && !bcc.isEmpty()) {
        mailData.put("bcc", bcc);
      }

      mailData.put("timestamp", System.currentTimeMillis());

    } catch (Exception e) {
      LogManager.error("MailManager", "Mail data creation failed: " + e.getMessage());
    }

    return mailData;
  }

  /**
   * Set from email
   */
  public void setFromEmail(String email) {
    if (validateEmail(email)) {
      this.fromEmail = email;
    }
  }

  /**
   * Set from name
   */
  public void setFromName(String name) {
    if (name != null && !name.isEmpty()) {
      this.fromName = sanitizeHeader(name);
    }
  }

  /**
   * Set reply-to email
   */
  public void setReplyTo(String email) {
    if (validateEmail(email)) {
      this.replyTo = email;
    }
  }
}
