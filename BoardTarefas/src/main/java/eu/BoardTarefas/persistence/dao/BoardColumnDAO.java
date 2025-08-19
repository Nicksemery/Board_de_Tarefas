package eu.BoardTarefas.persistence.dao;

import com.mysql.cj.jdbc.StatementImpl;
import eu.BoardTarefas.dto.BoardColumnDTO;
import eu.BoardTarefas.persistence.entity.BoardColumnEntity;
import eu.BoardTarefas.persistence.entity.CardEntity;
import lombok.RequiredArgsConstructor;

import javax.swing.text.html.Option;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static eu.BoardTarefas.persistence.entity.BoardColumnKindEnum.findByName;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class BoardColumnDAO {
    private final Connection connection;

    public BoardColumnEntity insert(final BoardColumnEntity entity) throws SQLException {
        var sql = "INSERT INTO BOARDS_COLUMNS (name, `order`, kind, board_id) VALUES (?, ?, ?, ?);";
        try(var statemente = connection.prepareStatement(sql)) {
            var i =1;
            statemente.setString(i++, entity.getName());
            statemente.setInt(i++, entity.getOrder());
            statemente.setString(i++, entity.getKind().name());
            statemente.setLong(i, entity.getBoard().getId());
            statemente.executeUpdate();
            if (statemente instanceof StatementImpl impl){
                entity.setId(impl.getLastInsertID());
            }return entity;
        }
    }

    public List<BoardColumnEntity> findByBoardId(final Long boardId) throws SQLException {
        List<BoardColumnEntity> entities = new ArrayList<>();
        var sql = "SELECT id, name, `order`, kind FROM BOARDS_COLUMNS WHERE board_id = ? ORDER BY `order`";
        try(var statemente = connection.prepareStatement(sql)) {
            statemente.setLong(1, boardId);
            statemente.executeQuery();
            var resultSet = statemente.getResultSet();
            while (resultSet.next()) {
                var entity = new BoardColumnEntity();
                entity.setId(resultSet.getLong("id"));
                entity.setName(resultSet.getString("name"));
                entity.setOrder(resultSet.getInt("order"));
                entity.setKind(findByName(resultSet.getString("kind")));
                entities.add(entity);
            }
        }return entities;
    }

    public List<BoardColumnDTO> findByBoardIdWithDetails(final Long boardId) throws SQLException {
        List<BoardColumnDTO> dtos = new ArrayList<>();
        var sql = """
                SELECT bc.id, bc.name, bc.kind, (SELECT COUNT(c.id) FROM CARDS c WHERE c.board_column_id = bc.id) cards_amount
                FROM BOARDS_COLUMNS bc
                WHERE board_id = ?
                ORDER BY `order`;
                """;
        try(var statemente = connection.prepareStatement(sql)) {
            statemente.setLong(1, boardId);
            statemente.executeQuery();
            var resultSet = statemente.getResultSet();
            BoardColumnDTO dto = null;
            while (resultSet.next()) {
                dto = new BoardColumnDTO(
                        resultSet.getLong("bc.id"),
                        resultSet.getString("bc.name"),
                        findByName(resultSet.getString("bc.kind")),
                        resultSet.getInt("cards_amount"));
            }
            dtos.add(dto);
        }return dtos;
    }

    public Optional<BoardColumnEntity> findById(final Long id) throws SQLException {
        var sql = """
                SELECT bc.name, bc.kind, c.id, c.title, c.description
                FROM BOARDS_COLUMNS bc
                LEFT JOIN CARDS c ON bc.board_column_id = c.id
                WHERE bc.id = ?;
                """;
        try(var statemente = connection.prepareStatement(sql)) {
            statemente.setLong(1, id);
            statemente.executeQuery();
            var resultSet = statemente.getResultSet();
            if (resultSet.next()) {
                var entity = new BoardColumnEntity();
                entity.setName(resultSet.getString("bc.name"));
                entity.setKind(findByName(resultSet.getString("bc.kind")));
                do {
                    var card = new CardEntity();
                    if (isNull(resultSet.getString("c.title"))) {
                        break;
                    }
                    card.setId(resultSet.getLong("c.id"));
                    card.setTitle(resultSet.getString("c.title"));
                    card.setDescription(resultSet.getString("c.description"));
                    entity.getCards().add(card);
                }while (resultSet.next());
                return Optional.of(entity);
            }
        }return  Optional.empty();
    }

}
