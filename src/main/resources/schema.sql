-- =============================================================================
-- Board API Database Schema (PostgreSQL)
-- Schema: board_store
-- Generated based on JPA Entities in com.board.api.entities
-- =============================================================================

-- 0. 스키마 생성 및 search_path 설정
CREATE SCHEMA IF NOT EXISTS board_store;
SET search_path TO board_store;

--------------------------------------------------------------------------------
-- 1. 사용자 테이블 (app_user)
--------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS board_store.app_user (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    user_name VARCHAR(100),
    user_addr VARCHAR(255),
    user_gender VARCHAR(10),
    user_role VARCHAR(50) DEFAULT 'USER',
    email VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMPTZ
);

COMMENT ON TABLE board_store.app_user IS '사용자 계정 정보 테이블';
COMMENT ON COLUMN board_store.app_user.id IS '사용자 고유 일련번호(PK)';
COMMENT ON COLUMN board_store.app_user.user_id IS '사용자 로그인 아이디(Unique)';
COMMENT ON COLUMN board_store.app_user.password IS '암호화된 비밀번호';
COMMENT ON COLUMN board_store.app_user.user_name IS '사용자 이름';
COMMENT ON COLUMN board_store.app_user.user_addr IS '사용자 주소';
COMMENT ON COLUMN board_store.app_user.user_gender IS '사용자 성별 (M: 남성, W: 여성)';
COMMENT ON COLUMN board_store.app_user.user_role IS '사용자 권한 (예: USER, ADMIN)';
COMMENT ON COLUMN board_store.app_user.email IS '이메일 주소';
COMMENT ON COLUMN board_store.app_user.is_active IS '계정 활성화 상태 여부 (true: 활성, false: 비활성)';
COMMENT ON COLUMN board_store.app_user.created_at IS '계정 생성 일시';
COMMENT ON COLUMN board_store.app_user.updated_at IS '계정 정보 수정 일시';
COMMENT ON COLUMN board_store.app_user.last_login_at IS '마지막 로그인 일시';

--------------------------------------------------------------------------------
-- 2. 메뉴 테이블 (app_menu)
--------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS board_store.app_menu (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT,
    menu_type VARCHAR(50),
    board_code VARCHAR(50),
    icon_class VARCHAR(100),
    menu_name VARCHAR(100) NOT NULL,
    href VARCHAR(255),
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE board_store.app_menu IS '시스템 네비게이션 메뉴 정보 테이블';
COMMENT ON COLUMN board_store.app_menu.id IS '메뉴 고유 일련번호(PK)';
COMMENT ON COLUMN board_store.app_menu.parent_id IS '상위 메뉴 일련번호 (1Depth 메뉴는 NULL)';
COMMENT ON COLUMN board_store.app_menu.menu_type IS '메뉴 유형 (예: BOARD, NOTICE, LINK 등)';
COMMENT ON COLUMN board_store.app_menu.board_code IS '연결 게시판 코드 (메뉴 유형이 BOARD인 경우)';
COMMENT ON COLUMN board_store.app_menu.icon_class IS '메뉴 표시 아이콘 CSS 클래스명';
COMMENT ON COLUMN board_store.app_menu.menu_name IS '메뉴 표시 명칭';
COMMENT ON COLUMN board_store.app_menu.href IS '이동 링크 URL';
COMMENT ON COLUMN board_store.app_menu.sort_order IS '메뉴 정렬 순서';
COMMENT ON COLUMN board_store.app_menu.is_active IS '메뉴 사용/노출 여부';
COMMENT ON COLUMN board_store.app_menu.created_at IS '메뉴 등록 일시';
COMMENT ON COLUMN board_store.app_menu.updated_at IS '메뉴 수정 일시';

--------------------------------------------------------------------------------
-- 3. 게시판 게시글 테이블 (app_board)
--------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS board_store.app_board (
    id BIGSERIAL PRIMARY KEY,
    board_type_code VARCHAR(50),
    title VARCHAR(255) NOT NULL,
    content TEXT,
    writer_id VARCHAR(100) NOT NULL,
    view_count INTEGER NOT NULL DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    group_id BIGINT,
    group_seq INTEGER NOT NULL DEFAULT 0,
    group_depth INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE board_store.app_board IS '게시판 게시글 테이블';
COMMENT ON COLUMN board_store.app_board.id IS '게시글 고유 일련번호(PK)';
COMMENT ON COLUMN board_store.app_board.board_type_code IS '게시판 구분 코드 (예: FREE, QNA 등)';
COMMENT ON COLUMN board_store.app_board.title IS '게시글 제목';
COMMENT ON COLUMN board_store.app_board.content IS '게시글 본문 내용';
COMMENT ON COLUMN board_store.app_board.writer_id IS '게시글 작성자 아이디';
COMMENT ON COLUMN board_store.app_board.view_count IS '게시글 조회수';
COMMENT ON COLUMN board_store.app_board.is_deleted IS '논리 삭제 플래그 (true: 삭제됨, false: 정상)';
COMMENT ON COLUMN board_store.app_board.group_id IS '계층형 답글 그룹 번호 (원글 ID)';
COMMENT ON COLUMN board_store.app_board.group_seq IS '답글 그룹 내 정렬 순번';
COMMENT ON COLUMN board_store.app_board.group_depth IS '답글 들여쓰기 깊이 (0: 원글, 1+: 대댓글)';
COMMENT ON COLUMN board_store.app_board.created_at IS '게시글 작성 일시';
COMMENT ON COLUMN board_store.app_board.updated_at IS '게시글 수정 일시';

--------------------------------------------------------------------------------
-- 4. 게시글 댓글 테이블 (app_board_comment)
--------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS board_store.app_board_comment (
    id BIGSERIAL PRIMARY KEY,
    board_id BIGINT NOT NULL,
    parent_id BIGINT,
    writer_id VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_board_comment_board FOREIGN KEY (board_id) REFERENCES board_store.app_board(id) ON DELETE CASCADE
);

COMMENT ON TABLE board_store.app_board_comment IS '게시글 댓글 테이블';
COMMENT ON COLUMN board_store.app_board_comment.id IS '댓글 고유 일련번호(PK)';
COMMENT ON COLUMN board_store.app_board_comment.board_id IS '대상 게시글 일련번호 (app_board.id FK)';
COMMENT ON COLUMN board_store.app_board_comment.parent_id IS '상위 부모 댓글 일련번호 (대댓글 계층)';
COMMENT ON COLUMN board_store.app_board_comment.writer_id IS '댓글 작성자 아이디';
COMMENT ON COLUMN board_store.app_board_comment.content IS '댓글 내용';
COMMENT ON COLUMN board_store.app_board_comment.is_deleted IS '댓글 논리 삭제 여부';
COMMENT ON COLUMN board_store.app_board_comment.created_at IS '댓글 작성 일시';
COMMENT ON COLUMN board_store.app_board_comment.updated_at IS '댓글 수정 일시';

--------------------------------------------------------------------------------
-- 5. 게시글 첨부파일 테이블 (app_board_file)
--------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS board_store.app_board_file (
    id BIGSERIAL PRIMARY KEY,
    board_id BIGINT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(1000) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_board_file_board FOREIGN KEY (board_id) REFERENCES board_store.app_board(id) ON DELETE CASCADE
);

COMMENT ON TABLE board_store.app_board_file IS '게시글 첨부파일 메타데이터 테이블';
COMMENT ON COLUMN board_store.app_board_file.id IS '첨부파일 고유 일련번호(PK)';
COMMENT ON COLUMN board_store.app_board_file.board_id IS '대상 게시글 일련번호 (app_board.id FK)';
COMMENT ON COLUMN board_store.app_board_file.original_file_name IS '사용자 업로드 원본 파일명';
COMMENT ON COLUMN board_store.app_board_file.file_name IS '서버/저장소 저장 파일명(UUID 등)';
COMMENT ON COLUMN board_store.app_board_file.file_url IS '파일 접근 URL / S3 경로';
COMMENT ON COLUMN board_store.app_board_file.file_type IS '파일 MIME 타입';
COMMENT ON COLUMN board_store.app_board_file.file_size IS '파일 크기 (Byte 단위)';
COMMENT ON COLUMN board_store.app_board_file.created_at IS '파일 등록 일시';

--------------------------------------------------------------------------------
-- 6. 게시글 좋아요 테이블 (app_board_like)
--------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS board_store.app_board_like (
    id BIGSERIAL PRIMARY KEY,
    board_id BIGINT NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_board_like_board FOREIGN KEY (board_id) REFERENCES board_store.app_board(id) ON DELETE CASCADE,
    CONSTRAINT uq_board_like_user UNIQUE (board_id, user_id)
);

COMMENT ON TABLE board_store.app_board_like IS '게시글 사용자 추천/좋아요 이력 테이블';
COMMENT ON COLUMN board_store.app_board_like.id IS '좋아요 고유 일련번호(PK)';
COMMENT ON COLUMN board_store.app_board_like.board_id IS '대상 게시글 일련번호 (app_board.id FK)';
COMMENT ON COLUMN board_store.app_board_like.user_id IS '좋아요 누른 사용자 아이디';
COMMENT ON COLUMN board_store.app_board_like.created_at IS '좋아요 등록 일시';

--------------------------------------------------------------------------------
-- 7. 공지사항 테이블 (app_notice)
--------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS board_store.app_notice (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    writer_id VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE board_store.app_notice IS '시스템 공지사항 테이블';
COMMENT ON COLUMN board_store.app_notice.id IS '공지사항 고유 일련번호(PK)';
COMMENT ON COLUMN board_store.app_notice.title IS '공지사항 제목';
COMMENT ON COLUMN board_store.app_notice.content IS '공지사항 본문 내용';
COMMENT ON COLUMN board_store.app_notice.writer_id IS '공지사항 작성자 아이디';
COMMENT ON COLUMN board_store.app_notice.created_at IS '공지사항 등록 일시';

--------------------------------------------------------------------------------
-- 8. 성능 최적화 인덱스 생성
--------------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_board_type_created ON board_store.app_board(board_type_code, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_board_group ON board_store.app_board(group_id, group_seq);
CREATE INDEX IF NOT EXISTS idx_comment_board ON board_store.app_board_comment(board_id);
CREATE INDEX IF NOT EXISTS idx_file_board ON board_store.app_board_file(board_id);
CREATE INDEX IF NOT EXISTS idx_like_board ON board_store.app_board_like(board_id);
