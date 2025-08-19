package eu.BoardTarefas;


import eu.BoardTarefas.persistence.migration.MigrationStrategy;
import eu.BoardTarefas.ui.MainMenu;

import java.sql.SQLException;

import static eu.BoardTarefas.persistence.config.ConnectionConfig.getConnection;

public class BoardTarefasApplication {

	public static void main(String[] args) throws SQLException {
		try (var connection = getConnection()) {
			new MigrationStrategy(connection).executeMigration();
		}
		new MainMenu().execute();
	}

}
