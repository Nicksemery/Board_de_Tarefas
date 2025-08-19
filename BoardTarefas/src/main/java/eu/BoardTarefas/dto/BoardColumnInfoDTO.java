package eu.BoardTarefas.dto;

import eu.BoardTarefas.persistence.entity.BoardColumnKindEnum;

public record BoardColumnInfoDTO(Long id,
                                 int order,
                                 BoardColumnKindEnum kind) {
}
