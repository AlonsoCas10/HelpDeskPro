package com.helpdeskpro.helpdeskpro_backend.service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

public class GoogleGmailMessageParser {
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset\\s*=\\s*[\\\"]?([^;\\\"]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Z0-9._%+-]+@([A-Z0-9.-]+\\.[A-Z]{2,})", Pattern.CASE_INSENSITIVE);
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[^\\s<>\\\"']+", Pattern.CASE_INSENSITIVE);

    public ParsedGmailMessage parse(Message message) {
        String contentType = header(message, "Content-Type");
        Charset charset = charsetFrom(contentType);
        StringBuilder textBody = new StringBuilder();
        StringBuilder htmlBody = new StringBuilder();
        Set<String> links = new LinkedHashSet<>();
        List<AttachmentMetadata> attachments = new ArrayList<>();
        collectParts(message.getPayload(), charset, textBody, htmlBody, links, attachments);

        String from = header(message, "From");

        return new ParsedGmailMessage(
                message.getId(),
                message.getThreadId(),
                from,
                senderName(from),
                senderAddress(from),
                senderDomain(from),
                header(message, "To"),
                header(message, "Subject"),
                header(message, "Date"),
                message.getLabelIds() == null ? java.util.List.of() : message.getLabelIds(),
                textBody.toString(),
                htmlBody.toString(),
                List.copyOf(links),
                List.copyOf(attachments));
    }

    private void collectParts(
            MessagePart part,
            Charset inheritedCharset,
            StringBuilder textBody,
            StringBuilder htmlBody,
            Set<String> links,
            List<AttachmentMetadata> attachments) {
        if (part == null) {
            return;
        }

        Charset charset = charsetFrom(header(part, "Content-Type"), inheritedCharset);
        String mimeType = part.getMimeType() == null ? "" : part.getMimeType().toLowerCase(Locale.ROOT);
        String filename = part.getFilename();
        if ((!isBlank(filename) || !isBlank(part.getBody() == null ? null : part.getBody().getAttachmentId()))
                && !"text/plain".equals(mimeType)
                && !"text/html".equals(mimeType)) {
            attachments.add(new AttachmentMetadata(
                    blankToNull(filename),
                    blankToNull(part.getMimeType()),
                    extensionFrom(filename)));
        }

        if (("text/plain".equals(mimeType) || "text/html".equals(mimeType))
                && part.getBody() != null
                && part.getBody().getData() != null) {
            String body = decode(part.getBody().getData(), charset);
            if ("text/plain".equals(mimeType)) {
                append(textBody, body);
            } else {
                append(htmlBody, body);
            }
            collectLinks(body, links);
        }

        if (part.getParts() != null) {
            for (MessagePart child : part.getParts()) {
                collectParts(child, charset, textBody, htmlBody, links, attachments);
            }
        }
    }

    private void collectLinks(String content, Set<String> links) {
        Matcher matcher = URL_PATTERN.matcher(content);
        while (matcher.find()) {
            String link = trimUrl(matcher.group());
            if (!link.isEmpty()) {
                links.add(link);
            }
        }
    }

    private String trimUrl(String value) {
        return value.replaceFirst("[.,;:!?)]*$", "");
    }

    private String senderDomain(String from) {
        if (isBlank(from)) {
            return null;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(from);
        return matcher.find() ? matcher.group(1).toLowerCase(Locale.ROOT) : null;
    }

    private String senderAddress(String from) {
        if (isBlank(from)) {
            return null;
        }
        Matcher matcher = EMAIL_ADDRESS_PATTERN.matcher(from);
        return matcher.find() ? matcher.group().toLowerCase(Locale.ROOT) : null;
    }

    private String senderName(String from) {
        if (isBlank(from)) {
            return null;
        }
        int separator = from.indexOf('<');
        if (separator <= 0) {
            return null;
        }
        return blankToNull(from.substring(0, separator).trim().replaceAll("^\"|\"$", ""));
    }

    private String extensionFrom(String filename) {
        if (isBlank(filename)) {
            return null;
        }
        int separator = filename.lastIndexOf('.');
        if (separator <= 0 || separator == filename.length() - 1) {
            return null;
        }
        return filename.substring(separator + 1).toLowerCase(Locale.ROOT);
    }

    private String header(Message message, String name) {
        return message.getPayload() == null ? "" : header(message.getPayload(), name);
    }

    private String header(MessagePart part, String name) {
        if (part.getHeaders() == null) {
            return "";
        }
        return part.getHeaders().stream()
                .filter(header -> name.equalsIgnoreCase(header.getName()))
                .map(MessagePartHeader::getValue)
                .findFirst()
                .orElse("");
    }

    private Charset charsetFrom(String contentType) {
        return charsetFrom(contentType, StandardCharsets.UTF_8);
    }

    private Charset charsetFrom(String contentType, Charset fallback) {
        Matcher matcher = CHARSET_PATTERN.matcher(contentType == null ? "" : contentType);
        if (!matcher.find()) {
            return fallback;
        }
        try {
            return Charset.forName(matcher.group(1).trim());
        } catch (Exception exception) {
            return fallback;
        }
    }

    private String decode(String data, Charset charset) {
        return new String(Base64.getUrlDecoder().decode(data), charset);
    }

    private void append(StringBuilder target, String value) {
        if (target.length() > 0) {
            target.append("\n");
        }
        target.append(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value;
    }

    public record ParsedGmailMessage(
            String messageId,
            String threadId,
            String from,
            String senderName,
            String senderAddress,
            String senderDomain,
            String to,
            String subject,
            String date,
            java.util.List<String> labelIds,
            String textBody,
            String htmlBody,
            List<String> links,
            List<AttachmentMetadata> attachments) {
        public ParsedGmailMessage(
                String messageId,
                String threadId,
                String from,
                String to,
                String subject,
                String date,
                List<String> labelIds,
                String textBody,
                String htmlBody) {
            this(messageId, threadId, from, null, null, null, to, subject, date, labelIds, textBody, htmlBody, List.of(), List.of());
        }
    }

    public record AttachmentMetadata(
            String name,
            String mimeType,
            String extension) {
    }
}