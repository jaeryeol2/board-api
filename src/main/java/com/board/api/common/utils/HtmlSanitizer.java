package com.board.api.common.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * 게시글 본문 XSS 방어용 HTML 정제 유틸.
 * 에디터(tiptap)가 만드는 태그·속성만 허용하고, URL 속성은 http/https/mailto 스킴만 허용합니다.
 * 프론트 sanitize-html.ts의 허용 목록과 동일하게 유지해야 합니다.
 */
public final class HtmlSanitizer {
  private static final String[] URL_PROTOCOLS = {"http", "https", "mailto"};
  private static final String[] IMAGE_PROTOCOLS = {"http", "https"};

  private static final Safelist SAFELIST = new Safelist()
      .addTags("p", "h1", "h2", "h3", "h4", "h5", "h6", "strong", "b", "em", "i", "u", "s", "del",
          "strike", "a", "ul", "ol", "li", "blockquote", "code", "pre", "hr", "br", "img", "span",
          "div")
      .addAttributes(":all", "class", "id")
      .addAttributes("img", "src", "alt", "title", "width", "height", "loading")
      .addAttributes("a", "href", "target", "rel", "title")
      .addProtocols("a", "href", URL_PROTOCOLS)
      .addProtocols("img", "src", IMAGE_PROTOCOLS);

  // 정제 시 줄바꿈·들여쓰기를 추가하지 않아 원본 서식을 그대로 유지합니다.
  private static final Document.OutputSettings OUTPUT_SETTINGS =
      new Document.OutputSettings().prettyPrint(false);

  private HtmlSanitizer() {}

  public static String sanitize(final String html) {
    if (html == null || html.isBlank()) {
      return html;
    }

    return Jsoup.clean(html, "", SAFELIST, OUTPUT_SETTINGS);
  }
}
