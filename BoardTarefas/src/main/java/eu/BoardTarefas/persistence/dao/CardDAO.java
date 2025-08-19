package eu.BoardTarefas.persistence.dao;

import com.mysql.cj.jdbc.StatementImpl;
import eu.BoardTarefas.dto.CardDetailsDTO;
import eu.BoardTarefas.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.apache.commons.lang3.stream.Streams.nonNull;
import static org.apache.commons.lang3.time.CalendarUtils.toOffsetDateTime;

@AllArgsConstructor
public class CardDAO {

    private Connection connection;

    public CardEntity insert(CardEntity entity) throws SQLException {
        var sql = "INSERT INTO CARDS (title, description, board_column_id) values (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            var i =1;
            statement.setString(i++, entity.getTitle());
            statement.setString(i++, entity.getDescription());
            statement.setLong(i, entity.getBoardColumn().getId());
            statement.executeUpdate();
            if (statement instanceof StatementImpl impl){
                entity.setId(impl.getLastInsertID());
            }
            return entity;
        }
    }

    public void moveToColumn(final Long columnId, final Long cardId) throws SQLException {
        var sql = "UPDATE CARDS SET board_column_id = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            var i =1;
            statement.setLong(i++, columnId);
            statement.setLong(i, cardId);
            statement.executeUpdate();
        }
    }

    public Optional<CardDetailsDTO> findById(final Long id) throws SQLException {
        var sql = """
                SELECT 
                    c.id,
                    c.title,
                    c.description,
                    b.blocked_at AS blocked_at,
                    b.block_reason AS block_reason,
                    c.board_column_id,
                    bc.name AS column_name,
                    (SELECT COUNT(sub_b.id) FROM BLOCKS sub_b WHERE sub_b.card_id = c.id) AS blocks_amount
                FROM CARDS c
                LEFT JOIN BLOCKS b ON c.id = b.card_id AND b.unblocked_at IS NULL
                INNER JOIN BOARD_COLUMNS bc ON bc.id = c.board_column_id
                WHERE c.id = ?
                """;
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                var dto = new CardDetailsDTO(
                        resultSet.getLong("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getString("block_reason") != null,
                        toOffsetDateTime(resultSet.getTimestamp("blocked_at")),
                        resultSet.getString("block_reason"),
                        resultSet.getInt("blocks_amount"),
                        resultSet.getLong("board_column_id"),
                        resultSet.getString("column_name")
                );
                return Optional.of(dto);
            }
        }
        return Optional.empty();

        
}

    private OffsetDateTime toOffsetDateTime(Timestamp ts) {
        if (ts == null) return null;
        return ts.toInstant().atOffset(ZoneOffset.UTC);
    }
}
