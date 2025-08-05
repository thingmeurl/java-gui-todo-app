package todo.view;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import todo.model.Priority;
import todo.model.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.Locale;

/**
 * {@code TaskEditorDialog} は、タスクの作成および編集を行うためのダイアログウィンドウです。
 * モーダルとして表示され、タスクのタイトル、期限、優先度、完了状態を入力・編集できます。
 * 新規タスク作成と既存タスク編集の両方に対応しています。
 */
public class TaskEditorDialog extends JDialog {
    private final JFrame parentFrame;
    private DatePicker datePicker;
    private JPanel mainPanel;
    private JPanel titlePanel;
    private JPanel deadLinePanel;
    private JPanel priorityPanel;
    private JPanel completedPanel;
    private JButton okButton;
    private JButton cancelButton;
    private JComboBox<Priority> priorityBox;
    private JCheckBox completedCheckBox;
    private JTextField titleField;
    private JTextField dueDateField;
    private LocalDate dueDate;
    private boolean confirmed = false;
    private boolean completed = false;
    private boolean isNewTask;
    private final Task task;
    private DatePicker dueDatePicker;
    private static final Font COMMON_FONT = new Font("Yu Gothic UI",Font.PLAIN,16);
    private ActionListener onSaveHandler;

    /**
     * タスク編集ダイアログを作成します。
     *
     * @param parent 呼び出し元の親フレーム
     * @param task 編集対象のタスク。新規作成の場合は {@code null} を指定
     */
    public TaskEditorDialog(JFrame parent, Task task) {
        super(parent, "タスク編集", true);
        this.parentFrame = parent;
        this.task = task;
        this.isNewTask = (task == null);
        initializeGUI();
        setResizable(false);
        setPreferredSize(new Dimension(450,250));
        pack();
        setLocationRelativeTo(null);
        addEventHandlers();
        if (task != null) {
            setFieldFromTask(task);
        }
    }

    /**
     * UIコンポーネントの初期化処理を行います。
     */
    private void initializeGUI() {
        // メインパネルを垂直方向に並べる BoxLayout で構成
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // ボタン生成と配置
        okButton = new JButton("OK");
        okButton.setFont(COMMON_FONT);
        cancelButton = new JButton("キャンセル");
        cancelButton.setFont(COMMON_FONT);

        // タスク名行
        titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel("タスク名：");
        titleLabel.setFont(COMMON_FONT);
        titleLabel.setPreferredSize(new Dimension(80,25));

        titleField = new JTextField();
        titleField.setFont(COMMON_FONT);
        Dimension titleFieldSize = new Dimension(300, 20);
        titleField.setPreferredSize(titleFieldSize);
        titleField.setMaximumSize(titleFieldSize);
        titleField.setMinimumSize(titleFieldSize);
        titleField.setBackground(Color.WHITE);
        titleField.setForeground(Color.BLACK);
        titleField.setDisabledTextColor(Color.BLACK);
        titleField.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(26,0)));
        titlePanel.add(titleField);

        mainPanel.add(titlePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0,10))); // 縦スぺ―ス

        // 期日行
        deadLinePanel = new JPanel();
        deadLinePanel.setLayout(new BoxLayout(deadLinePanel, BoxLayout.X_AXIS));
        deadLinePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ラベル（他と同じ幅に調整）
        JLabel deadLineLabel = new JLabel("期日：");
        deadLineLabel.setFont(COMMON_FONT);
        deadLineLabel.setPreferredSize(new Dimension(80,25));
        deadLinePanel.add(deadLineLabel);
        deadLinePanel.add(Box.createRigidArea(new Dimension(46,0)));

        // 日付表示用のフィールド
        Dimension dateFieldSize = new Dimension(100,25);
        dueDateField = new JTextField(10);
        dueDateField.setFont(COMMON_FONT);
        dueDateField.setPreferredSize(dateFieldSize);
        dueDateField.setMaximumSize(dateFieldSize);
        dueDateField.setMinimumSize(dateFieldSize);
        dueDateField.setEnabled(false);
        dueDateField.setBackground(Color.WHITE);
        dueDateField.setForeground(Color.BLACK);
        dueDateField.setDisabledTextColor(Color.BLACK);
        dueDateField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        deadLinePanel.add(dueDateField);

        // DatePicker の設定と追加
        DatePickerSettings settings = new DatePickerSettings(Locale.JAPAN);
        settings.setVisibleDateTextField(false);
        dueDatePicker = new DatePicker(settings);
        Dimension pickerSize = dueDatePicker.getPreferredSize();
        dueDatePicker.setMaximumSize(pickerSize);
        dueDatePicker.setMinimumSize(pickerSize);
        dueDatePicker.addDateChangeListener(e ->{
            if (e.getNewDate() != null) {
                dueDateField.setText(e.getNewDate().toString());
            } else {
                dueDateField.setText("");
            }
        });
        deadLinePanel.add(Box.createRigidArea(new Dimension(10,0))); // Picker との余白
        deadLinePanel.add(dueDatePicker);

        mainPanel.add(deadLinePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0,10)));

        // 優先度行
        JPanel priorityPanel = new JPanel();
        priorityPanel.setLayout(new BoxLayout(priorityPanel, BoxLayout.X_AXIS));
        priorityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priorityLabel = new JLabel("優先度：");
        priorityLabel.setFont(COMMON_FONT);
        priorityLabel.setPreferredSize(new Dimension(80,25));

        // サイズ固定
        priorityBox = new JComboBox<>(Priority.values());
        priorityBox.setSelectedItem(Priority.MEDIUM);
        priorityBox.setFont(COMMON_FONT);
        priorityBox.setPreferredSize(new Dimension(50,25));
        priorityBox.setMaximumSize(new Dimension(50,25));
        priorityBox.setMinimumSize(new Dimension(50,25));

        priorityPanel.add(priorityLabel);
        priorityPanel.add(Box.createRigidArea(new Dimension(31,0))); // ラベルとボックスの間の余白
        priorityPanel.add(priorityBox);

        mainPanel.add(priorityPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0,10)));

        // 完了行
        completedPanel = new JPanel();
        completedCheckBox = new JCheckBox();

        completedPanel.setLayout(new BoxLayout(completedPanel,BoxLayout.X_AXIS));
        completedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel completeLabel = new JLabel("完了：");
        completeLabel.setFont(COMMON_FONT);
        completeLabel.setPreferredSize(new Dimension(60,25));
        completeLabel.setMaximumSize(new Dimension(60,25));

        completedCheckBox.setPreferredSize(new Dimension(25,25));
        completedCheckBox.setMaximumSize(new Dimension(25,25));
        completedCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        completedPanel.add(completeLabel);
        completedPanel.add(Box.createRigidArea(new Dimension(30,0)));
        completedPanel.add(completedCheckBox);

        mainPanel.add(completedPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0,10)));

        // ボタン行
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        okButton = new JButton("OK");
        okButton.setFont(COMMON_FONT);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(okButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
        cancelButton = new JButton(("キャンセル"));
        cancelButton.setFont(COMMON_FONT);
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalGlue());

        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buttonPanel);

        // ダイアログにセット
        System.out.println("mainPanel = " + mainPanel);
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * ボタンやチェックボックスなどのイベントリスナーを設定します。
     */
    private void addEventHandlers() {
        // OKボタンが押されたら、Controllerから渡された処理（保存処理）を呼び出す
        okButton.addActionListener(e -> {
            if (onSaveHandler != null) {
                onSaveHandler.actionPerformed(e);
            }
        });

        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
    }

    /**
     * 「保存」ボタンに割り当てるアクションリスナーを設定します。
     *
     * @param handler 保存処理を実行するアクションリスナー
     */
    public void setOnSaveHandler(ActionListener handler) {
        this.onSaveHandler = handler;
    }

    /**
     * 現在の {@code Task} オブジェクトで、UIにフィールド値を設定します。
     *
     * @param task 表示対象のタスク
     */
    private void setFieldFromTask(Task task) {
        titleField.setText(task.getTitle());
        priorityBox.setSelectedItem(task.getPriority());
        completedCheckBox.setSelected(task.isDone());

        // 期日nullチェック
        LocalDate dueDate = task.getDueDate();
        dueDatePicker.setDate(dueDate);

    }

    /**
     * 入力されたタスクのタイトルを取得します。
     *
     * @return タスクのタイトル
     */
    public String getTaskTitle() {
        return titleField.getText();
    }

    /**
     * 入力された期日（Due Date）を取得します。
     *
     * @return 期日（{@link LocalDate}）、未設定の場合は null
     */
    public LocalDate getDueDate() {
        return dueDatePicker.getDate();
    }

    /**
     * 選択された優先度（Priority）を取得します。
     *
     * @return {@link Priority} オブジェクト
     */
    public Priority getTaskPriority() {
        return (Priority) priorityBox.getSelectedItem();
    }

    /**
     * タスクが完了としてマークされているかどうかを返します。
     *
     * @return 完了していれば true、そうでなければ false
     */
    public boolean isTaskCompleted() {
        return completedCheckBox.isSelected();
    }

    /**
     * ユーザーが［OK］を押して確定した場合、編集済みの {@link Task} を返します。
     *
     * @return 確定されたタスク、確定されていなければ null
     */
    public Task getTask() {
        return confirmed ? task : null;
    }

    /**
     * 編集が確定されたかどうかを返します。
     *
     * @return 確定されていれば true、キャンセルされていれば false
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * 編集の確定状態を設定します。
     *
     * @param confirmed true に設定すると編集を確定したことになります
     */
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

}
