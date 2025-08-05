package todo;

import todo.controller.TodoAppController;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(TodoAppController::new);
    }
}
