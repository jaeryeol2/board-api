package com.board.api.repositories.read;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.board.api.common.utils.QueryDslUtil;
import com.board.api.dtos.request.NoticeFilter;
import com.board.api.dtos.response.Notice;
import com.board.api.entities.QAppNotice;
import com.board.api.entities.QAppUser;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReadNoticeRepository {
    private final JPAQueryFactory factory;
    private static final QAppNotice notice = QAppNotice.appNotice;
    private static final QAppUser user = QAppUser.appUser;
    private static final String WRITER_NAME = "writerName";


    public long findNoticeCount() {
        return factory.select(notice.id.count()).from(notice).fetchOne();
    }

    public List<Notice> findNotices(NoticeFilter filter) {
        final QBean<Notice> columns =
                QueryDslUtil.generateFields(Notice.class, notice, user.userName.as(WRITER_NAME));

        return factory.select(columns)//
                .from(notice)//
                .leftJoin(user).on(notice.writerId.eq(user.userId))//
                .orderBy(notice.createdAt.desc())//
                .limit(filter.getRowsPerPage())//
                .offset((long) (filter.getPage() - 1) * filter.getRowsPerPage())//
                .fetch();
    }
}
