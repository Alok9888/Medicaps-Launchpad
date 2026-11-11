package com.project.backend.question.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class SanitizationService {

    // Patterns for potentially dangerous content
    private static final Pattern SCRIPT_TAG = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern IFRAME_TAG = Pattern.compile("<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern ON_EVENT = Pattern.compile("\\bon\\w+\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVASCRIPT_PROTOCOL = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_PROTOCOL = Pattern.compile("data:", Pattern.CASE_INSENSITIVE);
    private static final Pattern VBSCRIPT_PROTOCOL = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXPRESSION = Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMPORT = Pattern.compile("@import", Pattern.CASE_INSENSITIVE);
    private static final Pattern BEHAVIOR = Pattern.compile("behavior\\s*:", Pattern.CASE_INSENSITIVE);
    private static final Pattern MOZ_BINDING = Pattern.compile("-moz-binding", Pattern.CASE_INSENSITIVE);

    // Allowed HTML tags for rich text fields (description, constraints, etc.)
    private static final String[] ALLOWED_TAGS = {
            "p", "br", "strong", "b", "em", "i", "u", "code", "pre",
            "ul", "ol", "li", "blockquote", "h1", "h2", "h3", "h4",
            "a", "img", "table", "thead", "tbody", "tr", "th", "td",
            "span", "div", "hr"
    };

    // Allowed attributes
    private static final String[] ALLOWED_ATTRIBUTES = {
            "href", "src", "alt", "title", "class", "id", "style",
            "width", "height", "colspan", "rowspan", "align", "valign"
    };

    public String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }

        String sanitized = input;

        // Remove dangerous tags and protocols
        sanitized = SCRIPT_TAG.matcher(sanitized).replaceAll("");
        sanitized = IFRAME_TAG.matcher(sanitized).replaceAll("");
        sanitized = ON_EVENT.matcher(sanitized).replaceAll("data-on-");
        sanitized = JAVASCRIPT_PROTOCOL.matcher(sanitized).replaceAll("javascript_removed:");
        sanitized = DATA_PROTOCOL.matcher(sanitized).replaceAll("data_removed:");
        sanitized = VBSCRIPT_PROTOCOL.matcher(sanitized).replaceAll("vbscript_removed:");
        sanitized = EXPRESSION.matcher(sanitized).replaceAll("expression_removed(");
        sanitized = IMPORT.matcher(sanitized).replaceAll("@import_removed");
        sanitized = BEHAVIOR.matcher(sanitized).replaceAll("behavior_removed:");
        sanitized = MOZ_BINDING.matcher(sanitized).replaceAll("-moz-binding_removed");

        // Strip disallowed HTML tags (keep only allowed ones)
        sanitized = stripDisallowedTags(sanitized);

        // Sanitize attributes in allowed tags
        sanitized = sanitizeAttributes(sanitized);

        return sanitized;
    }

    public String sanitizePlainText(String input) {
        if (input == null) {
            return null;
        }

        // Escape HTML entities
        return input
                .replace("&", "\u0026amp;")
                .replace("<", "\u0026lt;")
                .replace(">", "\u0026gt;")
                .replace("\"", "\u0026quot;")
                .replace("'", "\u0026#x27;")
                .replace("/", "\u0026#x2F;");
    }

    public String sanitizeCode(String input) {
        if (input == null) {
            return null;
        }

        // For code fields, only escape HTML entities - preserve all code content
        return input
                .replace("&", "\u0026amp;")
                .replace("<", "\u0026lt;")
                .replace(">", "\u0026gt;");
    }

    public String sanitizeSlug(String input) {
        if (input == null) {
            return null;
        }

        return input.toLowerCase()
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    public String sanitizeFileName(String input) {
        if (input == null) {
            return null;
        }

        return input
                .replaceAll("[^a-zA-Z0-9._-]", "")
                .replaceAll("\\.+", ".")
                .replaceAll("^\\.+|\\.+$", "");
    }

    private String stripDisallowedTags(String html) {
        // Simple approach: remove tags not in allowed list
        // This is a basic implementation - for production consider using a library like OWASP Java HTML Sanitizer
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < html.length()) {
            if (html.charAt(i) == '<') {
                int endTag = html.indexOf('>', i);
                if (endTag == -1) {
                    // No closing bracket, skip
                    break;
                }
                String tag = html.substring(i, endTag + 1);
                if (isAllowedTag(tag)) {
                    result.append(tag);
                }
                // else skip the tag
                i = endTag + 1;
            } else {
                result.append(html.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    private boolean isAllowedTag(String tag) {
        // Extract tag name
        String tagName = tag.replaceAll("</?([a-zA-Z0-9]+).*", "$1").toLowerCase();

        for (String allowed : ALLOWED_TAGS) {
            if (allowed.equals(tagName)) {
                return true;
            }
        }
        return false;
    }

    private String sanitizeAttributes(String html) {
        // Basic attribute sanitization - remove dangerous attributes
        // For production, use a proper HTML sanitizer library
        String result = html;

        // Remove event handlers
        result = ON_EVENT.matcher(result).replaceAll("data-on-");

        // Remove javascript: in href/src
        result = result.replaceAll("(?i)(href|src)\\s*=\\s*[\"']javascript:", "$1=\"javascript_removed:");

        return result;
    }

    public boolean containsSuspiciousContent(String input) {
        if (input == null) {
            return false;
        }

        return SCRIPT_TAG.matcher(input).find()
                || IFRAME_TAG.matcher(input).find()
                || ON_EVENT.matcher(input).find()
                || JAVASCRIPT_PROTOCOL.matcher(input).find()
                || VBSCRIPT_PROTOCOL.matcher(input).find()
                || EXPRESSION.matcher(input).find()
                || IMPORT.matcher(input).find();
    }
}