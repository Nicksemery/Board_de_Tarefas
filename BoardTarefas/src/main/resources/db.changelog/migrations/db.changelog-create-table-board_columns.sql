create table BOARDS_COLUMNS(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) not null,
    `order` int not null,
    kind varchar(7) not null,
    board_id bigint not null,
    constraint boards__boards_columns_fk foreign key (board_id) references BOARDS(id) on delete cascade,
    constraint id_order_uk unique key unique_board_id_order (board_id, `order`)
)ENGINE=InnoDB