package todo.view;

import todo.model.TaskTableModel;
import todo.controller.TodoAppController;
import todo.model.Task;
import todo.model.TaskManager;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * TodoAppGUI は、Swingを用いて構築されたToDoアプリケーションのメインウィンドウです。
 * タスクの追加・編集・削除・完了切替・フィルタリング機能など、タスク管理の基本操作を提供します。
 *
 * <p>このクラスはMVCパターンのViewに相当し、ユーザーインタフェースの表示とイベントの取り扱いを行います。
 * {@code TaskManager} や {@code TaskTableModel} と連携して動作します。</p>
 *
 * 使用ライブラリ：
 * <ul>
 *   <li>{@code javax.swing.*} : UIコンポーネント</li>
 *   <li>{@code java.awt.*} : レイアウトやイベント制御</li>
 *   <li>{@code java.time.LocalDate} : 日付管理</li>
 * </ul>
 *
 * @author thingmeurl
 * @version 1.0
 */
public class TodoAppGUI extends JFrame {

    private final TaskManager taskManager;
    private final JTable taskTable;
    private final TaskTableModel tableModel;
    private final JCheckBox filterCheckBox;
    private final TableRowSorter<TaskTableModel> sorter;

    /**
     * GUIの初期化処理を実行し、ウィンドウを表示します。
     * このメソッドは、mainメソッドから呼び出されることを前提としています。
     */
    public TodoAppGUI(TodoAppController controller){
        super("ToDoアプリ");
        this.taskManager = controller.getTaskManager();
        List<Task> loadedTasks = controller.getTaskManager().getTasks();

        // モデルとテーブルの準備
        tableModel = new TaskTableModel(loadedTasks);
        System.out.println(tableModel.getRowCount());
        taskTable = new JTable(tableModel);
        this.sorter = new TableRowSorter<>(tableModel);
        taskTable.setRowSorter(sorter);
        taskTable.setFont(new Font("Yu Gothic UI",Font.PLAIN,16));
        taskTable.setRowHeight(30);

        // 完了列の幅を固定
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        taskTable.getColumnModel().getColumn(0).setMaxWidth(50);
        taskTable.getColumnModel().getColumn(0).setMinWidth(50);

        // タスク名、期日、優先度の列幅
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(80);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("追加");
        JButton editButton = new JButton("編集");
        JButton deleteButton = new JButton("削除");
        filterCheckBox = new JCheckBox("完了済みを非表示");

        topPanel.add(addButton);
        topPanel.add(editButton);
        topPanel.add(deleteButton);
        topPanel.add(filterCheckBox);
        add(topPanel, BorderLayout.NORTH);

        // 追加・編集・削除ボタン
        addButton.addActionListener(e -> controller.onAddButtonClicked());
        editButton.addActionListener(e -> controller.onEditButtonClicked());
        deleteButton.addActionListener(e -> {
            int modelRow = getSelectedTaskModelRow();
            if (modelRow != -1) {
                controller.onDeleteButtonClicked(modelRow);
            }
        });

        JScrollPane scrollPane = new JScrollPane(taskTable);
        add(scrollPane, BorderLayout.CENTER);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * タスク一覧の JTable を最新の状態に更新します。
     * タスクの追加・削除・編集後に呼び出して表示を更新します。
     */
    public void refreshTaskTable() {
        List<Task> updatedTasks = taskManager.getTasks();
        tableModel.setTasks(updatedTasks);
        tableModel.fireTableDataChanged();
        System.out.println("refreshTaskTable: taskManager hash = " + System.identityHashCode(taskManager));
    }

    /**
     * タスクの完了フィルターに使用するチェックボックスを取得します。
     *
     * @return filterCheckBox フィルタ用の JCheckBox
     */
    public JCheckBox getFilterCheckBox() {
        return filterCheckBox;
    }

    /**
     * タスクテーブルの並び替えに使用される TableRowSorter を取得します。
     *
     * @return sorter タスクテーブルのソーター
     */
    public TableRowSorter<TaskTableModel> getSorter() {
        return sorter;
    }

    /**
     * タスクテーブルにバインドされたデータモデルを取得します。
     *
     * @return tableModel タスクテーブルのデータモデル
     */
    public TaskTableModel getTableModel() {
        return tableModel;
    }

    /**
     * 現在選択されているタスクを取得します。
     *
     * @return 選択されたタスク、何も選択されていない場合は null
     */
    public Task getSelectedTask() {
        int selectedRow = taskTable.getSelectedRow();
        return selectedRow != -1 ? tableModel.getTaskAt(selectedRow) : null;
    }

    /**
     * タスクテーブルで現在選択されている行のモデルインデックスを取得します。
     *
     * @return モデルインデックス。何も選択されていない場合は -1
     */
    public int getSelectedTaskModelRow() {
        int viewIndex = taskTable.getSelectedRow();
        if (viewIndex == -1) return -1;
        return taskTable.convertRowIndexToModel(viewIndex);
    }

    /**
     * 指定されたインデックスの行にあるタスクを取得します。
     *
     * @param rowIndex 行インデックス
     * @return 指定されたインデックスにあるタスク
     */
    public Task getTaskAt(int rowIndex) {
        return tableModel.getTaskAt(rowIndex);
    }

    /**
     * この GUI の親フレーム（自身）を取得します。
     *
     * @return この TodoAppGUI インスタンス
     */
    public JFrame getParentFrame() {
        return this;
    }
}
