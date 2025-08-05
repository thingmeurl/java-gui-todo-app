package todo.controller;

import todo.model.Priority;
import todo.model.Task;
import todo.model.TaskManager;
import todo.model.TaskTableModel;
import todo.view.TaskEditorDialog;
import todo.view.TodoAppGUI;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableRowSorter;
import java.time.LocalDate;

/**
 * アプリケーションのコントローラークラス。
 * Model（TaskManager）と View（TodoAppGUI）を接続し、ユーザー操作に応じたロジックを提供する。
 * 主な責務：
 * - タスクの追加・編集・削除処理
 * - 完了タスクの表示切り替え
 * - モデルの永続化処理（保存・読み込み）
 */
public class TodoAppController {
    private final TaskManager taskManager;
    private final TodoAppGUI appGUI;
    private final JCheckBox filterCheckBox;
    private final TableRowSorter<TaskTableModel> sorter;
    private final TaskTableModel tableModel;
    private final JFrame parentFrame;

    /**
     * コントローラーを初期化し、GUIの表示とイベント設定を行う。
     */
    public TodoAppController() {
        this.taskManager = new TaskManager();
        this.appGUI = new TodoAppGUI(this);
        this.parentFrame = appGUI;

        appGUI.setTitle("Todo アプリ");
        appGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appGUI.setSize(600,400);
        appGUI.setVisible(true);

        this.filterCheckBox = appGUI.getFilterCheckBox();
        this.sorter = appGUI.getSorter();
        this.tableModel = appGUI.getTableModel();

        this.tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                this.taskManager.saveTasksToFile();
            }
        });

        setupFilterCheckBoxListener();

    }

    /**
     * 追加ボタン押下時の処理。
     * TaskEditorDialogを表示し、入力されたタスクをTaskManagerに追加する。
     */
    public void onAddButtonClicked() {
        TaskEditorDialog dialog = new TaskEditorDialog(parentFrame, null);

        dialog.setOnSaveHandler(e ->{
            String title = dialog.getTaskTitle();
            LocalDate dueDate = dialog.getDueDate();
            Priority priority = dialog.getTaskPriority();
            boolean completed = dialog.isTaskCompleted();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,"タスク名を入力してください","入力エラー",JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (dueDate == null) {
                int result = JOptionPane.showConfirmDialog(dialog,"期日が未入力です。空欄のままでよいですか","確認",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION) return;
            }

            Task newTask = taskManager.creatNewTask(title,completed,priority,dueDate);
            taskManager.addTask(newTask);
            dialog.setConfirmed(true);
            dialog.dispose();

            appGUI.refreshTaskTable();
            JOptionPane.showMessageDialog(parentFrame,"タスクを追加しました");
            taskManager.saveTasksToFile();
            System.out.println("handleAddTask: taskManager hash = " + System.identityHashCode(taskManager));

        });
        dialog.setVisible(true);
    }

    /**
     * 編集ボタン押下時の処理。
     * 選択されたタスクをTaskEditorDialogで編集し、更新内容を反映する。
     */
    public void onEditButtonClicked() {
        int selectedRow = appGUI.getSelectedTaskModelRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(appGUI,"編集するタスクを選択してください");
            return;
        }

        Task selectedTask = appGUI.getTaskAt(selectedRow);
        JFrame parentFrame = appGUI.getParentFrame();

        TaskEditorDialog dialog = new TaskEditorDialog(parentFrame, selectedTask);

        dialog.setOnSaveHandler(e ->{
            String title = dialog.getTaskTitle();
            LocalDate dueDate = dialog.getDueDate();
            Priority priority = dialog.getTaskPriority();
            boolean completed = dialog.isTaskCompleted();

            if(title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,"タスク名を入力してください","入力エラー",JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (dueDate == null) {
                int result = JOptionPane.showConfirmDialog(dialog,"期日が未入力です。空欄のままでよいですか","確認",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION) return;
            }

            selectedTask.setTitle(title);
            selectedTask.setPriority(priority);
            selectedTask.setDone(completed);
            selectedTask.setDueDate(dueDate);

            dialog.setConfirmed(true);
            dialog.dispose();

            appGUI.refreshTaskTable();
            JOptionPane.showMessageDialog(appGUI,"タスクを編集しました");
            taskManager.saveTasksToFile();
        });

        dialog.setVisible(true);
    }

    /**
     * 削除ボタン押下時の処理。
     * 選択されたタスクを確認の上、TaskManagerから削除する。
     *
     * @param modelRowIndex 削除対象のテーブル行インデックス（モデルインデックス）
     */
    public void onDeleteButtonClicked(int modelRowIndex) {
        if (modelRowIndex >= 0) {
            Task selectedTask = appGUI.getTableModel().getTaskAt(modelRowIndex);

            int confirm = JOptionPane.showConfirmDialog(
              appGUI,
              "選択したタスクを削除しますか？",
              "確認",
              JOptionPane.YES_NO_CANCEL_OPTION
            );

            if(confirm == JOptionPane.YES_OPTION) {
                taskManager.removeTask(selectedTask);
                appGUI.refreshTaskTable();
                taskManager.saveTasksToFile();
                JOptionPane.showMessageDialog(appGUI,"タスクを削除しました");
            } else {
                JOptionPane.showMessageDialog(appGUI,"削除がキャンセルされました");
            }
        } else {
            JOptionPane.showMessageDialog(appGUI,"削除するタスクを選択してください");
        }
    }

    /**
     * 完了済みタスクの表示切り替え用のチェックボックスリスナーを設定する。
     */
    public void setupFilterCheckBoxListener() {
        filterCheckBox.addActionListener(e -> {
            if (filterCheckBox.isSelected()) {
                sorter.setRowFilter(new RowFilter<>() {
                    @Override
                    public boolean include(Entry<? extends TaskTableModel, ? extends Integer> entry) {
                        Task task = tableModel.getTaskAt(entry.getIdentifier());
                        return !task.isDone();
                    }
                });
            } else {
                sorter.setRowFilter(null);
            }
        });
    }

    /**
     * モデルであるTaskManagerのインスタンスを返す。
     *
     * @return TaskManagerのインスタンス
     */
    public TaskManager getTaskManager() {
        return taskManager;
    }

    /**
     * アプリケーションのエントリポイント。
     *
     * @param args 実行時引数（未使用）
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(TodoAppController::new);
    }

}
