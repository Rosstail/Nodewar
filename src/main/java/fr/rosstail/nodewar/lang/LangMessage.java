package fr.rosstail.nodewar.lang;

public enum LangMessage {
    PLUGIN_PREFIX("prefix", true, false),

    COMMANDS_BY_PLAYER_ONLY("commands.by-player-only", false, false),
    COMMANDS_PERMISSION_DENIED("commands.permission-denied", false, false),
    COMMANDS_INSERT_PLAYER_NAME("commands.insert-player-name", false, false),
    COMMANDS_TOO_FEW_ARGUMENTS("commands.too-few-arguments", false, false),
    COMMANDS_WRONG_VALUE("commands.wrong-value", false, false),
    COMMANDS_WRONG_COMMAND("commands.wrong-command", false, false),
    COMMANDS_PLAYER_DOES_NOT_EXIST("commands.player-does-not-exist", false, false),
    COMMANDS_PLAYER_NO_DATA("commands.player-no-data", false, false),

    COMMANDS_HELP_HEADER("commands.help.header", false, false),

    COMMANDS_HELP_LINE("commands.help.line", false, false),
    COMMANDS_EDIT_PLAYER_DISCONNECTED("commands.edit.player.disconnected-player", false, false),
    COMMANDS_TEAM_DESC("commands.team.desc", false, false),
    COMMANDS_TEAM_ALREADY_EXIST("commands.team.already-exist", false, false),
    COMMANDS_TEAM_DOES_NOT_EXIST("commands.team.does-not-exist", false, false),
    COMMANDS_TEAM_PART_OF_NO_TEAM("commands.team.part-of-no-team", false, false),
    COMMANDS_TEAM_CHECK_DESC("commands.team.check.desc", false, false),
    COMMANDS_TEAM_CHECK_RESULT("commands.team.check.result", false, true),
    COMMANDS_TEAM_CHECK_RESULT_OTHER("commands.team.check.result-other", false, true),
    COMMANDS_TEAM_CHECK_RESULT_MEMBER_LINE("commands.team.check.result-member-line", false, false),
    COMMANDS_TEAM_CHECK_RESULT_RELATION_LINE("commands.team.check.result-relation-line", false, false),
    COMMANDS_TEAM_LIST_DESC("commands.team.list.desc", false, false),
    COMMANDS_TEAM_LIST_RESULT_HEADER("commands.team.list.result-header", false, false),
    COMMANDS_TEAM_LIST_RESULT_LINE("commands.team.list.result-line", false, false),
    COMMANDS_TEAM_INVITES_DESC("commands.team.invites.desc", false, false),
    COMMANDS_TEAM_INVITES_CHECK_DESC("commands.team.invites.check.desc", false, false),
    COMMANDS_TEAM_INVITES_CHECK_RESULT_HEADER("commands.team.invites.check.result-header", false, false),
    COMMANDS_TEAM_INVITES_CHECK_RESULT_LINE("commands.team.invites.check.result-line", false, false),
    COMMANDS_TEAM_INVITES_OPEN_DESC("commands.team.invites.open.desc", false, false),
    COMMANDS_TEAM_INVITES_OPEN_RESULT("commands.team.invites.open.result", false, false),
    COMMANDS_TEAM_INVITES_CLOSE_DESC("commands.team.invites.close.desc", false, false),
    COMMANDS_TEAM_INVITES_CLOSE_RESULT("commands.team.invites.close.result", false, false),
    COMMANDS_TEAM_CREATE_DESC("commands.team.create.desc", false, false),
    COMMANDS_TEAM_CREATE_RESULT("commands.team.create.result", false, false),
    COMMANDS_TEAM_JOIN_DESC("commands.team.join.desc", false, false),
    COMMANDS_TEAM_JOIN_RESULT("commands.team.join.result", false, false),
    COMMANDS_TEAM_LEAVE_DESC("commands.team.leave.desc", false, false),
    COMMANDS_TEAM_LEAVE_RESULT("commands.team.leave.result", false, false),
    COMMANDS_TEAM_MANAGE_DESC("commands.team.manage.desc", false, false),
    COMMANDS_TEAM_MANAGE_COLOR_DESC("commands.team.manage.color.desc", false, false),
    COMMANDS_TEAM_MANAGE_COLOR_RESULT("commands.team.manage.color.result", false, false),
    COMMANDS_TEAM_MANAGE_OPEN_DESC("commands.team.manage.open.desc", false, false),
    COMMANDS_TEAM_MANAGE_OPEN_RESULT("commands.team.manage.open.result", false, false),
    COMMANDS_TEAM_MANAGE_CLOSE_DESC("commands.team.manage.close.desc", false, false),
    COMMANDS_TEAM_MANAGE_CLOSE_RESULT("commands.team.manage.close.result", false, false),
    COMMANDS_TEAM_MANAGE_INVITE_DESC("commands.team.manage.invite.desc", false, false),
    COMMANDS_TEAM_MANAGE_INVITE_RESULT_SENT("commands.team.manage.invite.result-sent", false, false),
    COMMANDS_TEAM_MANAGE_INVITE_RESULT_EFFECTIVE("commands.team.manage.invite.result-effective", false, false),
    COMMANDS_TEAM_MANAGE_INVITE_RESULT_UNCHANGED("commands.team.manage.invite.result-unchanged", false, false),
    COMMANDS_TEAM_MANAGE_INVITE_RESULT_BLOCKED("commands.team.manage.invite.result-blocked", false, false),
    COMMANDS_TEAM_MANAGE_MEMBER_DESC("commands.team.manage.member.desc", false, false),
    COMMANDS_TEAM_MANAGE_MEMBER_KICK_DESC("commands.team.manage.member.kick.desc", false, false),
    COMMANDS_TEAM_MANAGE_MEMBER_KICK_RESULT("commands.team.manage.member.kick.result", false, false),
    COMMANDS_TEAM_MANAGE_MEMBER_PROMOTE_DESC("commands.team.manage.member.promote.desc", false, false),
    COMMANDS_TEAM_MANAGE_MEMBER_PROMOTE_RESULT("commands.team.manage.member.promote.result", false, false),
    COMMANDS_TEAM_MANAGE_MEMBER_DEMOTE_DESC("commands.team.manage.member.demote.desc", false, false),
    COMMANDS_TEAM_MANAGE_MEMBER_DEMOTE_RESULT("commands.team.manage.member.demote.result", false, false),
    COMMANDS_TEAM_MANAGE_MEMBER_TRANSFER_DESC("commands.team.manage.member.transfer.desc", false, false),
    COMMANDS_TEAM_MANAGE_MEMBER_TRANSFER_RESULT("commands.team.manage.member.transfer.result", false, false),
    COMMANDS_TEAM_MANAGE_RELATION_DESC("commands.team.manage.relation.desc", false, false),
    COMMANDS_TEAM_MANAGE_RELATION_OPEN_DESC("commands.team.manage.relation.open.desc", false, false),
    COMMANDS_TEAM_MANAGE_RELATION_OPEN_RESULT("commands.team.manage.relation.open.result", false, false),
    COMMANDS_TEAM_MANAGE_RELATION_CLOSE_DESC("commands.team.manage.relation.close.desc", false, false),
    COMMANDS_TEAM_MANAGE_RELATION_CLOSE_RESULT("commands.team.manage.relation.close.result", false, false),
    COMMANDS_TEAM_MANAGE_RELATION_INVITES_DESC("commands.team.manage.relation.edit.desc", false, false),
    COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_HEADER("commands.team.manage.relation.invites.result-header", false, false),
    COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_LINE("commands.team.manage.relation.invites.result-line", false, false),
    COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_LINE_RECEIVED("commands.team.manage.relation.invites.result-line-received", false, false),
    COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_LINE_SENT("commands.team.manage.relation.invites.result-line-sent", false, false),
    COMMANDS_TEAM_MANAGE_RELATION_EDIT_DESC("commands.team.manage.relation.edit.desc", false, false),
    COMMANDS_TEAM_MANAGE_RELATION_EDIT_RESULT_INVITE("commands.team.manage.relation.edit.result-invite", false, false),
    COMMANDS_TEAM_MANAGE_RELATION_EDIT_RESULT_WEIGHT("commands.team.manage.relation.edit.result-weight", false, false),
    COMMANDS_TEAM_MANAGE_RELATION_EDIT_RESULT_IGNORE("commands.team.manage.relation.edit.result-ignore", false, false),
    COMMANDS_TEAM_MANAGE_DISBAND_DESC("commands.team.manage.disband.desc", false, false),
    COMMANDS_TEAM_MANAGE_DISBAND_RESULT("commands.team.manage.disband.result", false, false),
    COMMANDS_TERRITORY_DESC("commands.territory.desc", false, false),
    COMMANDS_TERRITORY_CHECK_DESC("commands.territory.check.desc", false, false),
    COMMANDS_TERRITORY_CHECK_RESULT("commands.territory.check.result", false, true),
    COMMANDS_TERRITORY_CHECK_RESULT_OTHER("commands.territory.check.result-other", false, true),
    COMMANDS_ADMIN_DESC("commands.admin.desc", false, false),
    COMMANDS_ADMIN_TEAM_DESC("commands.admin.team.desc", false, false),
    COMMANDS_ADMIN_TEAM_CREATE_DESC("commands.admin.team.create.desc", false, false),
    COMMANDS_ADMIN_TEAM_CREATE_RESULT("commands.admin.team.create.result", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_DESC("commands.admin.team.edit.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_COLOR_DESC("commands.admin.team.edit.color.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_COLOR_RESULT("commands.admin.team.edit.color.result", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_OPEN_DESC("commands.admin.team.edit.open.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_OPEN_RESULT("commands.admin.team.edit.open.result", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_CLOSE_DESC("commands.admin.team.edit.close.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_CLOSE_RESULT("commands.admin.team.edit.close.result", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_INVITE_DESC("commands.admin.team.edit.invite.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_INVITE_RESULT("commands.admin.team.edit.invite.result", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_DESC("commands.admin.team.edit.member.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_ADD_DESC("commands.admin.team.edit.member.add.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_ADD_RESULT("commands.admin.team.edit.member.add.result", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_REMOVE_DESC("commands.admin.team.edit.member.remove.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_REMOVE_RESULT("commands.admin.team.edit.member.remove.result", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_PROMOTE_DESC("commands.admin.team.edit.member.promote.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_PROMOTE_RESULT("commands.admin.team.edit.member.promote.result", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_DEMOTE_DESC("commands.admin.team.edit.member.demote.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_DEMOTE_RESULT("commands.admin.team.edit.member.demote.result", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_TRANSFER_DESC("commands.admin.team.edit.member.transfer.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_TRANSFER_RESULT("commands.admin.team.edit.member.transfer.result", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_DESC("commands.admin.team.edit.relation.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_OPEN_DESC("commands.admin.team.edit.relation.open.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_OPEN_RESULT("commands.admin.team.edit.relation.open.result", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_CLOSE_DESC("commands.admin.team.edit.relation.close.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_CLOSE_RESULT("commands.admin.team.edit.relation.close.result", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_INVITES_DESC("commands.admin.team.edit.relation.edit.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_INVITES_RESULT_HEADER("commands.admin.team.edit.relation.invites.result-header", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_INVITES_RESULT_LINE("commands.admin.team.edit.relation.invites.result-line", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_INVITES_RESULT_LINE_RECEIVED("commands.admin.team.edit.relation.invites.result-line-received", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_INVITES_RESULT_LINE_SENT("commands.admin.team.edit.relation.invites.result-line-sent", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_EDIT_DESC("commands.admin.team.edit.relation.edit.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_EDIT_RESULT_INVITE("commands.admin.team.edit.relation.edit.result-invite", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_EDIT_RESULT_WEIGHT("commands.admin.team.edit.relation.edit.result-weight", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_EDIT_RESULT_IGNORE("commands.admin.team.edit.relation.edit.result-ignore", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_SET_DESC("commands.admin.team.edit.relation.set.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_SET_RESULT("commands.admin.team.edit.relation.set.result", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_DISBAND_DESC("commands.admin.team.edit.disband.desc", false, false),
    COMMANDS_ADMIN_TEAM_EDIT_DISBAND_RESULT("commands.admin.team.edit.disband.result", false, false),
    
    COMMANDS_ADMIN_TERRITORY_DESC("commands.admin.territory.desc", false, false),
    COMMANDS_ADMIN_TERRITORY_RELOAD_REGIONS_DESC("commands.admin.territory.reload-regions.desc", false, false),
    COMMANDS_ADMIN_TERRITORY_RELOAD_REGIONS_RESULT("commands.admin.territory.reload-regions.result", false, false),
    COMMANDS_ADMIN_TERRITORY_PROTECT_DESC("commands.admin.territory.protect.desc", false, false),
    COMMANDS_ADMIN_TERRITORY_PROTECT_RESULT("commands.admin.territory.protect.result", false, false),
    COMMANDS_ADMIN_TERRITORY_VULNERABLE_DESC("commands.admin.territory.vulnerable.desc", false, false),
    COMMANDS_ADMIN_TERRITORY_VULNERABLE_RESULT("commands.admin.territory.vulnerable.result", false, false),
    COMMANDS_ADMIN_TERRITORY_TEAM_DESC("commands.admin.territory.team.desc", false, false),
    COMMANDS_ADMIN_TERRITORY_TEAM_SET_DESC("commands.admin.territory.team.set.desc", false, false),
    COMMANDS_ADMIN_TERRITORY_TEAM_SET_RESULT("commands.admin.territory.team.set.result", false, false),
    COMMANDS_ADMIN_TERRITORY_TEAM_RESET_DESC("commands.admin.territory.team.reset.desc", false, false),
    COMMANDS_TERRITORY_TEAM_RESET_RESULT("commands.admin.territory.team.reset.result", false, false),

    PLAYER_ONLINE("player.online", false, false),
    PLAYER_OFFLINE("player.offline", false, false),

    STORAGE_TYPE("storage.type", false, false),
    FORMAT_DATETIME("format.datetime", false, false),
    FORMAT_DATETIME_NEVER("format.datetime-never", false, false),
    FORMAT_COUNTDOWN("format.countdown", false, false),

    TEAM_OPEN("team.open", false, false),
    TEAM_CLOSE("team.close", false, false),
    TEAM_RELATION_NEUTRAL("team.relations.neutral", false, false),
    TEAM_RELATION_CONTROLLED("team.relations.controlled", false, false),
    TEAM_RELATION_TEAM("team.relations.team", false, false),
    TEAM_RELATION_ALLY("team.relations.ally", false, false),
    TEAM_RELATION_TRUCE("team.relations.truce", false, false),
    TEAM_RELATION_ENEMY("team.relations.enemy", false, false),
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