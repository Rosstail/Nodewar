package fr.rosstail.nodewar.lang;

public enum LangMessage {
    PLUGIN_PREFIX("prefix", true),

    COMMANDS_BY_PLAYER_ONLY("commands.by-player-only", false),
    COMMANDS_PERMISSION_DENIED("commands.permission-denied", false),
    COMMANDS_INSERT_PLAYER_NAME("commands.insert-player-name", false),
    COMMANDS_WRONG_VALUE("commands.wrong-value", false),
    COMMANDS_WRONG_COMMAND("commands.wrong-command", false),
    COMMANDS_PLAYER_DOES_NOT_EXIST("commands.player-does-not-exist", false),
    COMMANDS_PLAYER_NO_DATA("commands.player-no-data", false),

    COMMANDS_HELP_HEADER("commands.help.header", false),

    COMMANDS_HELP_LINE("commands.help.line", false),

    COMMANDS_CHECK_DESC("commands.check.desc", false),
    COMMANDS_CHECK_SELF_DESC("commands.check.self.desc", false),
    COMMANDS_CHECK_SELF_RESULT("commands.check.self.result", false),
    COMMANDS_CHECK_OTHER_DESC("commands.check.other.desc", false),
    COMMANDS_CHECK_OTHER_RESULT("commands.check.other.result", false),

    COMMANDS_EDIT_DESC("commands.edit.desc", false),

    COMMANDS_EDIT_PLAYER_DESC("commands.edit.player.desc", false),
    COMMANDS_EDIT_PLAYER_DISCONNECTED("commands.edit.player.disconnected-player", false),
    COMMANDS_EDIT_PLAYER_NO_DATA("commands.edit.player.player-no-data", false),
    COMMANDS_EDIT_PLAYER_OUT_OF_BOUNDS("commands.edit.player.out-of-bounds", false),

    COMMANDS_TEAM_DESC("commands.team.desc", false),

    COMMANDS_TEAM_CHECK_DESC("commands.team.check.desc", false),
    COMMANDS_TEAM_CHECK_RESULT("commands.team.check.result", false),
    COMMANDS_TEAM_CREATE_DESC("commands.team.create.desc", false),
    COMMANDS_TEAM_CREATE_RESULT("commands.team.create.result", false),

    TERRITORY_HELP("territory-help", false),
    TERRITORY_SET_EMPIRE("territory-set-empire", false),
    TERRITORY_NEUTRALIZE("territory-neutralize", false),
    TERRITORY_VULNERABLE("territory-vulnerable", false),
    TERRITORY_INVULNERABLE("territory-invulnerable", false),

    TITLE_TERRITORY_NEUTRALIZED("territory.titles.neutralized.title", true),
    SUBTITLE_TERRITORY_NEUTRALIZED("territory.titles.neutralized.subtitle", true),
    TITLE_TERRITORY_CONQUERED("territory.titles.conquered.title", true),
    SUBTITLE_TERRITORY_CONQUERED("territory.titles.conquered.subtitle", true),
    TITLE_TERRITORY_DEFENDED("territory.titles.defended.title", true),
    SUBTITLE_TERRITORY_DEFENDED("territory.titles.defended.subtitle", true),

    COMMANDS_RELOAD_DESC("commands.reload.desc", false),
    COMMANDS_RELOAD_RESULT("commands.reload.result", false),

    COMMANDS_SAVE_DESC("commands.save.desc", false),
    COMMANDS_SAVE_RESULT("commands.save.result", false),

    COMMANDS_EVAL_DESC("commands.eval.desc", false),
    COMMANDS_EVAL_RESULT("commands.eval.result", false),

    PLAYER_ONLINE("player.online", false),
    PLAYER_OFFLINE("player.offline", false),

    STORAGE_TYPE("storage.type", false),
    FORMAT_DATETIME("format.datetime", false),
    FORMAT_DATETIME_NEVER("format.datetime-never", false),
    FORMAT_COUNTDOWN("format.countdown", false),
    ;

    private final String text;
    private String displayText;
    private final boolean nullable;

    LangMessage(String text, boolean nullable) {
        this.text = text;
        this.nullable = nullable;
    }

    String getText() {
        return this.text;
    }

    public String getDisplayText() {
        return displayText;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }
}