package todo.model;

/**
 * タスクの優先度を表す列挙型。
 * 高（HIGH）、中（MEDIUM）、低（LOW）の3段階で管理される。
 */
public enum Priority {
    HIGH,
    MEDIUM,
    LOW;

    /**
     * ユーザー向けに日本語で表示する文字列を返します。
     *
     * @return "高", "中", "低" のいずれか
     */
    @Override
    public String toString() {
        return switch (this) {
            case HIGH -> "高";
            case MEDIUM -> "中";
            case LOW -> "低";
        };
    }

    /**
     * 与えられた文字列をもとに、対応するPriority列挙値を返します。
     * 英語（"high"など）と日本語（"高"など）の両方に対応。
     *
     * @param input 優先度を表す文字列（例："高", "medium", "low"）
     * @return 対応するPriority列挙値
     * @throws IllegalArgumentException 無効な文字列の場合
     */
    public static Priority fromString(String input) {
        return switch (input.trim().toLowerCase()) {
            case "high", "高" -> HIGH;
            case "medium", "中" -> MEDIUM;
            case "low", "低" -> LOW;
            default -> throw new IllegalArgumentException("無効な優先度：" + input);
        };
    }
}
