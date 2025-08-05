package todo.model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskManagerはタスクの状態を管理するモデルクラスです。
 * タスクの追加・削除・一覧取得を提供し、永続化のためのファイル操作も担当します。
 * GUIアプリケーションにおけるModel層として使用されます。
 */
public class TaskManager {
    private int nextId = 1;
    private final List<Task> tasks;
    private static final String FILE_NAME = "task.txt";

    public TaskManager() {
        tasks = new ArrayList<>();
        loadTasksFromFile();
    }

    public TaskManager(boolean skipLoading){
        tasks = new ArrayList<>();
        if (!skipLoading) {
            loadTasksFromFile();
        }
    }

    /**
     * 新しい Task オブジェクトを作成します。
     * このメソッドは一意のIDを自動的に割り当てて Task インスタンスを生成します。
     *
     * @param title タスクの名称
     * @param done タスクが完了済みかどうか
     * @param priority タスクの優先度（null可）
     * @param dueDate タスクの期日（null可）
     * @return 生成された Task オブジェクト
     */
    public Task creatNewTask(String title, boolean done, Priority priority, LocalDate dueDate) {
        return new Task(nextId++, title, done, priority, dueDate);
    }

    /**
     * タスクをリストに追加します。
     *
     * @param task 追加するタスク
     */
    public void addTask(Task task) {
        tasks.add(task);
        System.out.println("addTask: tasks size = " + tasks.size());
    }

    /**
     * 指定されたタスクをリストから削除します。
     *
     * @param task 削除するタスク
     */
    public void removeTask(Task task) {
        tasks.remove(task);
    }

    /**
     * 現在のタスクリストを取得します。
     *
     * @return タスクの一覧（List）
     */
    public List<Task> getTasks() { return tasks; }

    public void toggleTaskStatus(int index) {
        if(index < 0 || index >= tasks.size()) {
            throw new IndexOutOfBoundsException("無効なインデックスです: " + index);
        }
        Task task = tasks.get(index);
        task.setDone(!task.isDone());
        saveTasksToFile();
    }

    /**
     * タスクリストをファイルに保存します。
     */
    public void saveTasksToFile() {
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(FILE_NAME), StandardCharsets.UTF_8))){
            for (Task task : tasks) {
                writer.println(task.toCSV());
            }
        } catch (IOException e) {
            System.out.println("ファイル保存中にエラーが発生しました" + e.getMessage());
        }
    }

    /**
     * ファイルからタスクリストを読み込みます。
     */
    public void loadTasksFromFile(){
        tasks.clear();
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(FILE_NAME), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Task.fromCSV(line).ifPresent(tasks::add);
            }

            // 最大IDを調べて nextId を更新
            int maxId = tasks.stream().mapToInt(Task::getId).max().orElse(0);
            nextId = maxId + 1;

        } catch (IOException e) {
            System.out.println("ファイル読み込み中にエラーが発生しました" + e.getMessage());
        }
    }
}
