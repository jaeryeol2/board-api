package com.board.api.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class HtmlSanitizerTest {

  @Test
  void removesScriptAndEventHandlers() {
    final String result =
        HtmlSanitizer.sanitize("<p>hi</p><img src=x onerror=alert(1)><script>alert(1)</script>");

    assertThat(result).doesNotContain("onerror", "<script", "alert");
    assertThat(result).contains("<p>hi</p>");
  }

  @Test
  void blocksDisallowedSchemes() {
    final String result = HtmlSanitizer.sanitize(
        "<a href=\"java&#9;script:alert(1)\">a</a><a href=\"data:text/html,x\">b</a>");

    assertThat(result).doesNotContain("javascript", "data:");
  }

  @Test
  void keepsEditorOutput() {
    final String html = "<p class=\"x\"><strong>b</strong> <a href=\"https://example.com\" "
        + "target=\"_blank\" rel=\"noopener\">link</a></p>"
        + "<img src=\"https://cdn.example.com/20261114/uuid\" alt=\"a\">";

    assertThat(HtmlSanitizer.sanitize(html)).isEqualTo(html);
  }
}
