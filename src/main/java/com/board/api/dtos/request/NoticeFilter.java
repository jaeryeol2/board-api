package com.board.api.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeFilter {
    private int page = 1;
    private int rowsPerPage = 10;
}
