# board-api

회원, 게시글, 댓글, 공지, 이미지 업로드를 제공하는 게시판 서비스의 백엔드 API 서버입니다.

## 전체 구성

```
board-front (3000) → board-gateway (8888) → board-api (8000) → PostgreSQL / Redis / LocalStack(S3)
```

| 저장소 | 역할 |
|---|---|
| [board-front](https://github.com/jaeryeol2/board-front) | 화면 (Next.js) |
| [board-gateway](https://github.com/jaeryeol2/board-gateway) | 단일 진입점, 인증 확인, CORS, 라우팅 |
| board-api (이 저장소) | 비즈니스 로직, DB/캐시/파일 저장 |

## 기능

- **회원**: 가입(승인 전에는 로그인 차단), 로그인/로그아웃, access/refresh 토큰 발급과 재발급(httpOnly 쿠키), 비로그인 사용자용 게스트 ID, 프로필/비밀번호 변경, 내가 쓴 글/댓글 조회
- **게시글/댓글**: 게시판 유형별 목록·상세·작성·수정, 인기글 목록, 익명 게시판(작성자 이름 숨김), 댓글과 답글, 조회수(같은 사용자 1시간 내 중복 제외)
- **공지**: 목록, 작성
- **이미지 업로드**: S3 업로드/삭제(로컬은 LocalStack), 본문 HTML은 jsoup으로 정리
- **Redis**: 메뉴·홈·게시글 상세·회원 정보 캐시(`@Cacheable`), 로그인 실패·가입 요청 횟수 제한, 접속자 수 집계

## 설계 특징

- **Reader/Writer 서비스 분리**: 도메인마다 조회용 `*Reader`와 변경용 `*Writer`를 나누고, `*Service`가 이를 조합합니다.
- **읽기/쓰기 DB 커넥션 분리**: 서비스 메서드 이름(`get*`, `find*` 등)으로 트랜잭션의 readOnly를 정하고, readOnly면 read 풀, 아니면 write 풀로 라우팅합니다. (`src/main/java/com/board/api/zconfig/database/DataSourceConfig.java`)

읽기 전용 복제 DB를 두어도 쓰기 DB 부하가 줄지 않는 경우가 왜 생기는지 궁금해서, 라우팅 원리를 직접 구현해 본 학습용 프로젝트입니다.

## 기술 스택

Java 21, Spring Boot 3.5.4, Spring Data JPA, QueryDSL 5.1.0, Spring Security, Spring Data Redis, PostgreSQL, jjwt 0.12.6, Jasypt 3.0.5, Spring Cloud AWS 3.4.2 (S3), p6spy-spring-boot-starter 1.9.1, springdoc-openapi 2.8.6

## 로컬 실행

1. Docker로 PostgreSQL(5432), Redis(6379), LocalStack 3.0(4566)을 띄웁니다. DB 접속 정보는 `src/main/resources/config/application-local.yaml`을 따릅니다. 테이블은 기동 시 `schema.sql`로 생성되며, 초기 데이터(메뉴 등)는 포함되어 있지 않습니다.
2. 이미지 업로드를 쓰려면 LocalStack에 `board-image` 버킷을 만듭니다(코드에서 생성하지 않음).
   ```bash
   docker exec <localstack 컨테이너> awslocal s3 mb s3://board-image
   ```
3. 환경변수를 지정해 실행합니다. `JWT_SECRET_KEY`는 gateway, front와 같은 값이어야 합니다.
   ```bash
   export JASYPT_PASSWORD=...   # Jasypt 암호화 키
   export JWT_SECRET_KEY=...    # JWT 서명 키
   ./gradlew bootRun            # http://localhost:8000/api
   ```

헬스 체크: `GET http://localhost:8000/api/actuator/health`
