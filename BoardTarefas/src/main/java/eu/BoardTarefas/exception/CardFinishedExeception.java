package eu.BoardTarefas.exception;

public class CardFinishedExeception extends RuntimeException {
    public CardFinishedExeception(final String message) {
        super(message);
    }
}
