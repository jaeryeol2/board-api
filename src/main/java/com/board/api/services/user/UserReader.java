package com.board.api.services.user;

import static com.board.api.constants.GlobalConstant.ACCESS_TOKEN;
import static com.board.api.constants.GlobalConstant.REFRESH_TOKEN;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.board.api.common.base.ResponseApi;
import com.board.api.common.base.ResponseApi.ResponseList;
import com.board.api.common.condition.CaseGet;
import com.board.api.common.utils.TokenUtil;
import com.board.api.dtos.request.BoardFilter;
import com.board.api.dtos.response.Board;
import com.board.api.dtos.response.Board.BoardComment;
import com.board.api.dtos.response.PersonalUser;
import com.board.api.dtos.response.User;
import com.board.api.repositories.read.ReadBoardRepository;
import com.board.api.repositories.read.ReadUserRepository;
import com.board.api.services.common.CommonReader;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserReader {
  private final TokenUtil tokenUtil;
  private final ReadUserRepository readUserRepository;
  private final ReadBoardRepository readBoardRepository;
  private final CommonReader commonReader;

  // 마이페이지 첫 화면에 담는 최근 작성 글·댓글 수 (프론트 MyActivities의 페이지당 개수와 동일)
  private static final int PERSONAL_RECENT_ROWS = 10;

  @Cacheable(cacheNames = "getUserInfo", key = "#userId", unless = "#result == null")
  public User getUserInfo(String userId) {
    return readUserRepository.findUserById(userId);
  }

  /**
   * 로그인·비밀번호 확인용 사용자 정보 (작성 글·댓글은 조회하지 않음)
   */
  public PersonalUser getLoginUserInfo(String userId) {
    return readUserRepository.findLoginInfoById(userId);
  }

  /**
   * 마이페이지용 사용자 정보. 작성 글·댓글은 최근 PERSONAL_RECENT_ROWS건과 전체 개수만 담습니다.
   * (나머지 페이지는 /user/posts, /user/comments로 조회)
   */
  public PersonalUser getPersonalUserInfo(String userId) {
    final PersonalUser personalUser = readUserRepository.findLoginInfoById(userId);
    if (personalUser != null) {
      final BoardFilter firstPage =
          BoardFilter.builder().page(1).rowsPerPage(PERSONAL_RECENT_ROWS).build();
      final ResponseList<Board> posts = getMyPosts(userId, firstPage);
      final ResponseList<BoardComment> comments = getMyComments(userId, firstPage);

      personalUser.setWritedContentList(posts.getList());
      personalUser.setWritedContentCount(posts.getTotal());
      personalUser.setWritedCommentList(comments.getList());
      personalUser.setWritedCommentCount(comments.getTotal());
    }
    return personalUser;
  }

  public ResponseList<Board> getMyPosts(final String userId, final BoardFilter filter) {
    final long count = readBoardRepository.findBoardCountByWriterId(userId);
    return ResponseList.of(Math.toIntExact(count), CaseGet.when(() -> count > 0)//
        .then(() -> {
          final List<Long> ids = readBoardRepository.findBoardIdsByWriterId(userId, filter);
          return readBoardRepository.findBoardListByWriterId(userId, ids);
        })//
        .other(Collections::emptyList)//
        .get());
  }

  public ResponseList<BoardComment> getMyComments(final String userId, final BoardFilter filter) {
    final long count = readBoardRepository.findBoardCommentCountByWriterId(userId);
    return ResponseList.of(Math.toIntExact(count), CaseGet.when(() -> count > 0)//
        .then(() -> readBoardRepository.findBoardCommentListByWriterId(userId, filter))//
        .other(Collections::emptyList)//
        .get());
  }

  public String getUserId(HttpServletRequest request) {
    String token = getAccessToken(request);

    if (token == null) {
      token = getRefreshToken(request);
    }

    return token != null ? tokenUtil.getTokenInfo(token).getSubject() : null;
  }

  public boolean checkToken(HttpServletRequest request) {
    String token = getAccessToken(request);

    if (token == null) {
      token = getRefreshToken(request);
    }

    return !tokenUtil.isTokenExpired(token);
  }

  public String getAccessToken(HttpServletRequest request) {
    String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (accessToken != null) {
      final String token = accessToken.replaceAll("^(?i)Bearer\\s+", "");
      return !tokenUtil.isTokenExpired(token) ? token : null;
    }

    return null;
  }

  public String getRefreshToken(HttpServletRequest request) {
    final Cookie[] cookies =
        Objects.requireNonNullElseGet(request.getCookies(), () -> new Cookie[0]);
    final String token = Arrays.asList(cookies).stream()//
        .filter(Objects::nonNull)//
        .filter(cookie -> REFRESH_TOKEN.getValue().equalsIgnoreCase(cookie.getName()))//
        .findFirst()//
        .orElseGet(() -> {
          final Cookie cookie = new Cookie(REFRESH_TOKEN.getValue(), null);
          cookie.setHttpOnly(true);
          cookie.setSecure(true);

          return cookie;
        })//
        .getValue();

    return !tokenUtil.isTokenExpired(token) ? token : null;
  }

  /** 값과 관계없이 refresh 토큰 쿠키가 요청에 포함되어 있는지 여부 */
  public boolean hasRefreshTokenCookie(HttpServletRequest request) {
    final Cookie[] cookies =
        Objects.requireNonNullElseGet(request.getCookies(), () -> new Cookie[0]);
    return Arrays.stream(cookies).filter(Objects::nonNull)
        .anyMatch(cookie -> REFRESH_TOKEN.getValue().equalsIgnoreCase(cookie.getName()));
  }

  public ResponseApi<Boolean> setGuest(final String newGuestId, HttpServletResponse response) {
    final ResponseCookie newGuestCookie = ResponseCookie.from("guest_tracking_id", newGuestId)//
        .path("/")//
        .httpOnly(true)//
        .secure(true)//
        .sameSite("Lax")//
        .maxAge(Duration.ofMinutes(5L))//
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, newGuestCookie.toString());
    commonReader.recordUserActivity(newGuestId);

    return ResponseApi.ok(true);
  }

  @CacheEvict(cacheNames = "getUserInfo", key = "#userId",
      condition = "#userId != null && !#userId.equals('')")
  public void expireToken(String userId, HttpServletResponse response) {
    final ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN.getValue(), "").path("/")
        .httpOnly(true).secure(true).sameSite("Lax").maxAge(Duration.ofSeconds(0L)).build();
    final ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN.getValue(), "").path("/")
        .httpOnly(true).secure(true).sameSite("Lax").maxAge(Duration.ofSeconds(0L)).build();

    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    commonReader.removeUserActivity(userId);
  }

  public void recordUserActivity(String id) {
    commonReader.recordUserActivity(id);
  }

  public void removeUserActivity(String id) {
    commonReader.removeUserActivity(id);
  }

  public void makeToken(String userId, HttpServletResponse response) {
    makeAccessToken(userId, response);
    makeRefreshToken(userId, response);
  }

  public void makeAccessToken(String userId, HttpServletResponse response) {
    final String accessToken = tokenUtil.createToken(userId, 1L);
    final ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN.getValue(), accessToken)
        .path("/").httpOnly(true).secure(true).sameSite("Lax").maxAge(Duration.ofHours(1L)).build();

    response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
  }

  public void makeRefreshToken(String userId, HttpServletResponse response) {
    final String refreshToken = tokenUtil.createToken(userId, 8760L);
    final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
    final ResponseCookie refreshCookie =
        ResponseCookie.from(REFRESH_TOKEN.getValue(), refreshToken).path("/").httpOnly(true)
            .secure(true).sameSite("Lax").maxAge(Duration.between(now, now.plusYears(1L))).build();

    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
  }

  public String makeNewAccessToken(HttpServletRequest request) {
    final String refreshToken = getRefreshToken(request);

    if (refreshToken != null) {
      String userId = tokenUtil.getTokenInfo(refreshToken).getSubject();
      User user = readUserRepository.findLoginInfoById(userId);

      return user != null ? tokenUtil.createToken(userId, 1L) : null;
    }

    return null;
  }
}
