package fr.rosstail.nodewar.lang;

import java.util.List;

public enum LangMessage {
    PLUGIN_PREFIX("prefix", true, false),

    COMMANDS_BY_PLAYER_ONLY("commands.by-player-only", false, false),
    COMMANDS_PERMISSION_DENIED("commands.permission-denied", false, false),
    COMMANDS_INSERT_PLAYER_NAME("commands.insert-player-name", false, false),
    COMMANDS_WRONG_VALUE("commands.wrong-value", false, false),
    COMMANDS_WRONG_COMMAND("commands.wrong-command", false, false),
    COMMANDS_PLAYER_DOES_NOT_EXIST("commands.player-does-not-exist", false, false),
    COMMANDS_PLAYER_NO_DATA("commands.player-no-data", false, false),

    COMMANDS_HELP_HEADER("commands.help.header", false, false),

    COMMANDS_HELP_LINE("commands.help.line", false, false),

    COMMANDS_CHECK_DESC("commands.check.desc", false, false),
    COMMANDS_CHECK_SELF_DESC("commands.check.self.desc", false, false),
    COMMANDS_CHECK_SELF_RESULT("commands.check.self.result", false, false),
    COMMANDS_CHECK_OTHER_DESC("commands.check.other.desc", false, false),
    COMMANDS_CHECK_OTHER_RESULT("commands.check.other.result", false, false),

    COMMANDS_EDIT_DESC("commands.edit.desc", false, false),

    COMMANDS_EDIT_PLAYER_DESC("commands.edit.player.desc", false, false),
    COMMANDS_EDIT_PLAYER_DISCONNECTED("commands.edit.player.disconnected-player", false, false),
    COMMANDS_EDIT_PLAYER_NO_DATA("commands.edit.player.player-no-data", false, false),
    COMMANDS_EDIT_PLAYER_OUT_OF_BOUNDS("commands.edit.player.out-of-bounds", false, false),

    COMMANDS_TEAM_DESC("commands.team.desc", false, false),

    COMMANDS_TEAM_CHECK_DESC("commands.team.check.desc", false, false),
    COMMANDS_TEAM_CHECK_RESULT("commands.team.check.result", false, true),
    COMMANDS_TEAM_CHECK_RESULT_MEMBER_LINE("commands.team.check.result-member-line", false, false),
    COMMANDS_TEAM_CHECK_RESULT_RELATION_LINE("commands.team.check.result-relation-line", false, false),
    COMMANDS_TEAM_CREATE_DESC("commands.team.create.desc", false, false),
    COMMANDS_TEAM_CREATE_RESULT("commands.team.create.result", false, false),

    COMMANDS_TEAM_JOIN_DESC("commands.team.join.desc", false, false),
    COMMANDS_TEAM_JOIN_RESULT("commands.team.join.result", false, false),
    COMMANDS_TEAM_COLOR_DESC("commands.team.color.desc", false, false),
    COMMANDS_TEAM_COLOR_RESULT("commands.team.color.result", false, false),
    COMMANDS_TEAM_OPEN_DESC("commands.team.open.desc", false, false),
    COMMANDS_TEAM_OPEN_RESULT("commands.team.open.result-open", false, false),
    COMMANDS_TEAM_CLOSE_RESULT("commands.team.open.result-close", false, false),
    COMMANDS_TEAM_INVITE_DESC("commands.team.invite.desc", false, false),
    COMMANDS_TEAM_INVITE_RESULT("commands.team.open.result", false, false),
    COMMANDS_TEAM_LEAVE_DESC("commands.team.leave.desc", false, false),
    COMMANDS_TEAM_LEAVE_RESULT("commands.team.leave.result", false, false),
    COMMANDS_TEAM_DISBAND_DESC("commands.team.disband.desc", false, false),
    COMMANDS_TEAM_DISBAND_RESULT("commands.team.disband.result", false, false),

    TERRITORY_HELP("territory-help", false, false),
    TERRITORY_SET_EMPIRE("territory-set-empire", false, false),
    TERRITORY_NEUTRALIZE("territory-neutralize", false, false),
    TERRITORY_VULNERABLE("territory-vulnerable", false, false),
    TERRITORY_INVULNERABLE("territory-invulnerable", false, false),

    TITLE_TERRITORY_NEUTRALIZED("territory.titles.neutralized.title", true, false),
    SUBTITLE_TERRITORY_NEUTRALIZED("territory.titles.neutralized.subtitle", true, false),
    TITLE_TERRITORY_CONQUERED("territory.titles.conquered.title", true, false),
    SUBTITLE_TERRITORY_CONQUERED("territory.titles.conquered.subtitle", true, false),
    TITLE_TERRITORY_DEFENDED("territory.titles.defended.title", true, false),
    SUBTITLE_TERRITORY_DEFENDED("territory.titles.defended.subtitle", true, false),

    COMMANDS_RELOAD_DESC("commands.reload.desc", false, false),
    COMMANDS_RELOAD_RESULT("commands.reload.result", false, false),

    COMMANDS_SAVE_DESC("commands.save.desc", false, false),
    COMMANDS_SAVE_RESULT("commands.save.result", false, false),

    COMMANDS_EVAL_DESC("commands.eval.desc", false, false),
    COMMANDS_EVAL_RESULT("commands.eval.result", false, false),

    PLAYER_ONLINE("player.online", false, false),
    PLAYER_OFFLINE("player.offline", false, false),

    STORAGE_TYPE("storage.type", false, false),
    FORMAT_DATETIME("format.datetime", false, false),
    FORMAT_DATETIME_NEVER("format.datetime-never", false, false),
    FORMAT_COUNTDOWN("format.countdown", false, false),
    ;

    private final String text;
    private String displayText;
    private final boolean nullable;
    private final boolean isList;

    LangMessage(String text, boolean nullable, boolean isList) {
        this.text = text;
        this.nullable = nullable;
        this.isList = isList;
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

    public boolean isList() {
        return isList;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }
}