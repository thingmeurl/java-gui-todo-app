package todo.model;

import java.time.LocalDate;
import java.util.Optional;

/**
 * タスクの情報を表すモデルクラス。
 * 各タスクはid、タイトル、完了状態、優先度、期日を持ちます。
 */
public class Task {
    private final int id;
    private String title;
    private boolean done;
    private Priority priority;
    private LocalDate dueDate;
    /**
     * Taskのコンストラクタ。
     * @param id タスクのid
     * @param title タスク名
     * @param done 完了状態
     * @param priority 優先度
     * @param dueDate 期日（nullの場合もあり）
     */

    public Task(int id, String title, boolean done, Priority priority, LocalDate dueDate) {
        this.id = id;
        this.title = title;
        this.done = done;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    /** @return id */
    public int getId() {return id;}

    /** @return タスク名 */
    public String getTitle(){
        return title;
    }

    /** @param title タスク名を設定 */
    public void setTitle(String title){
        this.title = title;
    }

    /** @return 完了済みかどうか */
    public boolean isDone(){
        return this.done;
    }

    /** @param done 完了状態を設定 */
    public void setDone(boolean done) {
        this.done = done;
    }

    /** @return 優先度 */
    public Priority getPriority(){
        return priority;
    }

    /** @param priority 優先度を設定 */
    public void setPriority(Priority priority){
        this.priority = priority;
    }

    /** @return 期日 */
    public LocalDate getDueDate(){
        return dueDate;
    }

    /** @param dueDate 期日を設定 */
    public void setDueDate(LocalDate dueDate){
        this.dueDate = dueDate;
    }

    /** 完了状態をトグル（未完了→完了 or 完了→未完了） */
    public void toggleDone(){
        this.done = !this.done;
    }

    /** @return チェックボックス表記（[✓] or [ ]） */
    private String getCheckbox() {
        return done ? "[✓]" : "[ ]";
    }

    /** @return 期日が今日かどうか */
    public boolean isDueToday(){
        return LocalDate.now().equals(this.dueDate);
    }

    /** @return タスクの文字列表現（チェックボックス＋タイトルなど） */
    @Override
    public String toString(){
        return String.format("%s %-20s 優先度: %-2s 締切： %s",
                getCheckbox(),
                title,
                priority,
                dueDate != null ? dueDate.toString() : "なし");
    }

    /**
     * タスク情報をCSV形式の文字列に変換します。
     *
     * @return CSV文字列
     */
    String toCSV() {
        return String.format("%d,%s,%s,%s,%s",
            id,
            title,
            done,
            (priority != null ? priority.toString() : ""),
            (dueDate != null ? dueDate.toString() : "")
        );
    }

    /**
     * CSV形式の1行からTaskオブジェクトを生成します。
     *
     * @param line CSV形式の文字列（id,title,done,priority,dueDate）
     * @return Taskオブジェクトを含むOptional（不正な形式の場合はempty）
     */
    static Optional<Task> fromCSV(String line) {
        try {
            String[] parts = line.split(",", -1);
            if (parts.length < 5) return Optional.empty();

            int id = Integer.parseInt(parts[0].trim());
            String title = parts[1];
            boolean isDone = Boolean.parseBoolean(parts[2]);
            Priority priority = parts[3].isEmpty() ? null : Priority.fromString(parts[3]);
            LocalDate dueDate = parts[4].isEmpty() ? null : LocalDate.parse(parts[4]);

            return Optional.of(new Task(id,title, isDone, priority, dueDate));

        } catch (Exception e) {
            System.out.println("CSV読み込みエラー：" + e.getMessage());
            return Optional.empty();
        }
    }
}
