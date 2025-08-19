package eu.BoardTarefas.persistence.dao;

import com.mysql.cj.jdbc.StatementImpl;
import eu.BoardTarefas.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
public class BoardDAO {

    private Connection connection;

    public BoardEntity insert(final BoardEntity boardEntity) throws SQLException {
        var sql = "INSERT INTO BOARDS(name) VALUES (?);";
        try(var statemente = connection.prepareStatement(sql)) {
            statemente.setString(1, boardEntity.getName());
            statemente.executeUpdate();
            if(statemente instanceof StatementImpl impl){
                boardEntity.setId(impl.getLastInsertID());
            }
        }
        return boardEntity;
    }


    public void delete(final Long id) throws SQLException {
        var sql = "DELETE FROM BOARDS WHERE id = ?;";
        try(var statemente = connection.prepareStatement(sql)) {
            statemente.setLong(1, id);
            statemente.executeUpdate();
        }
    }

    public Optional<BoardEntity> findById(final Long id) throws SQLException {
        var sql = "SELECT id, name FROM BOARDS WHERE id = ?;";
        try(var statemente = connection.prepareStatement(sql)) {
            statemente.setLong(1, id);
            statemente.executeQuery();
            var resultSet = statemente.getResultSet();
            if(resultSet.next()){
                var entity = new BoardEntity();
                entity.setId(resultSet.getLong("id"));
                entity.setName(resultSet.getString("name"));
                return Optional.of(entity);
            }
        }return Optional.empty();
    }


    public boolean exists(final Long id) throws SQLException {
        var sql = "SELECT 1 FROM BOARDS WHERE id = ?;";
        try(var statemente = connection.prepareStatement(sql)) {
            statemente.setLong(1, id);
            statemente.executeQuery();
            return statemente.getResultSet().next();
        }
    }
}
