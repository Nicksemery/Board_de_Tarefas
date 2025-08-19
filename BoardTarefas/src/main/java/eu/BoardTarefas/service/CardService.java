package eu.BoardTarefas.service;

import eu.BoardTarefas.dto.BoardColumnInfoDTO;
import eu.BoardTarefas.exception.CardBlockedException;
import eu.BoardTarefas.exception.CardFinishedExeception;
import eu.BoardTarefas.exception.EntityNotFoundExceptiom;
import eu.BoardTarefas.persistence.dao.BlockDAO;
import eu.BoardTarefas.persistence.dao.CardDAO;
import eu.BoardTarefas.persistence.entity.BoardColumnKindEnum;
import eu.BoardTarefas.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@AllArgsConstructor
public class CardService {

    private final Connection connection;

    public CardEntity create(final CardEntity card) throws SQLException {
        try{
            var dao = new CardDAO(connection);
            dao.insert(card);
            connection.commit();
            return card;
        }catch(SQLException e){
            connection.rollback();
            throw e;
        }
    }

    public void moveToNextColumn(final Long cardId, final List<BoardColumnInfoDTO> boardcColumnInfo) throws SQLException {
        try{
            var dao = new CardDAO(connection);
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(
            ()-> new EntityNotFoundExceptiom("O card de Id %s não foi encontrado".formatted(cardId)));
            if (dto.blocked()){
                var message = "O card %s está bloqueado, é necessário desbloquea-lo para mover".formatted(cardId);
                throw new CardBlockedException(message);
            }
            var correntColumn = boardcColumnInfo.stream().filter(bc -> bc.id().equals(dto.id())).findFirst().orElseThrow(() -> new IllegalStateException("O card informado pertence a outro board"));
            if (correntColumn.kind().equals(BoardColumnKindEnum.FINAL)){
                throw new CardFinishedExeception("O card já foi finalizado com sucesso");
            }
            var nextColumn = boardcColumnInfo.stream().filter(bc -> bc.id().equals(dto.columnId())).findFirst().orElseThrow(() -> new IllegalStateException("O card está cancelado"));
            dao.moveToColumn(nextColumn.id(), cardId);
        }catch(SQLException e){
            connection.rollback();
            throw e;
        }
    }

    public void cancel (final Long cardId, final Long cancelColumnId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try{
            var dao = new CardDAO(connection);
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundExceptiom("O ccard de id %s não foi encontrado".formatted(cardId)));
            if (dto.blocked()){
                var message = "O card %s está bloqueado, é necessário desbloquea-lo para mover".formatted(cardId);
                throw new CardBlockedException(message);
            }
            var correntColumn = boardColumnsInfo.stream().filter(bc -> bc.id().equals(dto.columnId())).findFirst().orElseThrow(() -> new IllegalStateException("O card informado pertence a outro board"));
            if (correntColumn.kind().equals(BoardColumnKindEnum.FINAL)){
                throw new CardFinishedExeception("O card já foi finalizado com sucesso");
            }
            boardColumnsInfo.stream().filter(bc -> bc.order() == correntColumn.order()+1).findFirst().orElseThrow(()-> new IllegalStateException("O card está cancelado"));
            dao.moveToColumn(cancelColumnId, cardId);
        }catch (SQLException e){
            connection.rollback();
            throw e;
        }
    }

    public void block(final Long id, final String reason, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try{
            var dao = new CardDAO(connection);
            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundExceptiom("O card de id %s não foi encontrado".formatted(id))
            );
            if (dto.blocked()){
                var message = "O card %s já está bloqueado".formatted(id);
                throw new CardBlockedException(message);
            }
            var correntColumn = boardColumnsInfo.stream().filter(bc -> bc.id().equals(dto.columnId())).findFirst().orElseThrow();
            if (correntColumn.kind().equals(BoardColumnKindEnum.FINAL) || correntColumn.kind().equals(BoardColumnKindEnum.CANCEL)){
                var message = "O card está em uma coluna do tipo %s e não pode ser bloqueada".formatted(correntColumn.kind());
                throw new IllegalStateException(message);
            }
            var blockDAO = new BlockDAO(connection);
            blockDAO.block(reason,id);
            connection.commit();
        }catch (SQLException e){
            connection.rollback();
            throw e;
        }
    }

    public void unblock(final Long id, final String reason) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var optional = dao.findById(id);
            var dto = optional.orElseThrow(
                    () -> new EntityNotFoundExceptiom("O card de id %s não foi encontrado".formatted(id))
            );
            if (!dto.blocked()){
                var message = "O card %s não está bloqueado".formatted(id);
                throw new CardBlockedException(message);
            }
            var blockDAO = new BlockDAO(connection);
            blockDAO.unblock(reason,id);
            connection.commit();
        }catch (SQLException e){
            connection.rollback();
            throw e;
        }
    }
}
