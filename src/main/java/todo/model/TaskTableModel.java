package todo.model;

import javax.swing.table.AbstractTableModel;
import javax.swing.JTable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link JTable} に表示するタスクデータのためのテーブルモデル。
 * タスク一覧の保持と、セル単位での表示・編集ロジックを提供する。
 * 本クラスは {@link TaskManager} には依存せず、
 * タスクのリスト {@code List<Task>} を直接操作対象とする。
 * データの更新通知（追加・削除・変更など）も内包しており、テーブル側に即時反映される。
 */
public class TaskTableModel extends AbstractTableModel {
    private final String[] columnNames = {"完了","タスク名","期日","優先度"};
    private List<Task> tasks;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * タスク一覧を初期データとして受け取るコンストラクタ。
     * 内部的に新たなリストを作成して保持するため、引数のリストを外部から変更しても影響を受けない。
     * @param tasks 初期表示に用いるタスクのリスト
     */
    public TaskTableModel(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * モデル内のタスク一覧を新しいリストで置き換えます。
     * 内部でリストをコピーするため、外部の変更による影響を受けません。
     * データ変更後はテーブルに更新通知を送信します。
     *
     * @param newTasks 新しく設定するタスクのリスト
     */
    public void setTasks(List<Task> newTasks) {
        this.tasks = new ArrayList<>(newTasks);
        fireTableDataChanged();
    }

    /**
     * テーブルの行数（タスク数）を返す。
     *
     * @return 登録されているタスクの数
     */
    @Override
    public int getRowCount() {
        return this.tasks.size();
    }

    /**
     * テーブルの列数を返す。
     *
     * @return 常に 4（タイトル、期限、優先度、完了）
     */
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * 指定された列の名前を返す。
     *
     * @param column 列インデックス
     * @return 列の表示名
     */
    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> "完了";
            case 1 -> "タスク名";
            case 2 -> "期日";
            case 3 -> "優先度";
            default -> "";
        };
    }

    /**
     * 各列のデータ型を返す。
     * 特に「完了」列ではチェックボックス表示のため {@link Boolean} 型を返す。
     *
     * @param columnIndex 列インデックス
     * @return 各列のクラス型
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Boolean.class;   // チェックボックス用
            case 1 -> String.class;    // タスク名
            case 2 -> LocalDate.class; // 期日
            case 3 -> Priority.class;  // 優先度
            default -> Object.class;
        };
    }

    /**
     * 指定されたセルの値を返す。
     *
     * @param rowIndex    行インデックス（0から始まる）
     * @param columnIndex 列インデックス（0から始まる）
     * @return セルに表示すべきオブジェクト（String, Booleanなど）
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Task task = this.tasks.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> task.isDone();
            case 1 -> task.getTitle();
            case 2 -> {
                LocalDate dueDate = task.getDueDate();
                yield (dueDate != null) ? formatter.format(dueDate) : "";
            }
            case 3 -> task.getPriority();
            default -> null;
        };
    }

    /**
     * 指定された行インデックスに対応する Task オブジェクトを取得します。
     *
     * @param rowIndex 表内の行インデックス（0 始まり）
     * @return 指定された行の Task オブジェクト
     */
    public Task getTaskAt(int rowIndex) {
        return this.tasks.get(rowIndex);
    }

    /**
     * 指定されたセルに新しい値を設定します。
     * モデル内のタスクオブジェクトに対して該当のプロパティを更新し、
     * テーブルの表示も更新されるよう通知を行います。
     *
     * @param aValue セルに設定する新しい値
     * @param rowIndex 行番号（タスクのインデックス）
     * @param columnIndex 列番号（タイトル、期限、優先度、完了状態のいずれか）
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Task task = this.tasks.get(rowIndex);

        switch (columnIndex) {
            case 0: // 完了チェック
                if (aValue instanceof Boolean) {
                    task.setDone((Boolean) aValue);
                }
                break;
            case 1: // タスク名
                if (aValue != null) {
                    task.setTitle(aValue.toString());
                }
                break;
            case 2: // 期日
                if (aValue == null || aValue.toString().isBlank()) {
                    task.setDueDate(null);
                } else {
                    try {
                        task.setDueDate(LocalDate.parse(aValue.toString()));
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date format: " + aValue);
                    }
                }
                break;
            case 3: // 優先度
                if (aValue instanceof Priority) {
                    task.setPriority((Priority) aValue);
                }
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    /**
     * 指定されたセルが編集可能かどうかを返します。
     * この実装では、すべてのセルを編集可能としています。
     *
     * @param rowIndex 行番号（無視されます）
     * @param columnIndex 列番号（無視されます）
     * @return 常に true（すべてのセルが編集可能）
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0; //完了列のみ編集可
    }


}
