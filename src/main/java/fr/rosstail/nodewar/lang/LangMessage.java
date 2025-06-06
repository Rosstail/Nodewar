package fr.rosstail.nodewar.lang;

public enum LangMessage {
    PLUGIN_PREFIX("prefix", true),

    COMMANDS_BY_PLAYER_ONLY("commands.by-player-only", false),
    COMMANDS_PERMISSION_DENIED("commands.permission-denied", false),
    COMMANDS_INSERT_PLAYER_NAME("commands.insert-player-name", false),
    COMMANDS_TOO_FEW_ARGUMENTS("commands.too-few-arguments", false),
    COMMANDS_WRONG_VALUE("commands.wrong-value", false),
    COMMANDS_WRONG_COMMAND("commands.wrong-command", false),
    COMMANDS_PLAYER_DOES_NOT_EXIST("commands.player-does-not-exist", false),
    COMMANDS_PLAYER_NO_DATA("commands.player-no-data", false),
    COMMANDS_PLAYER_NOT_IN_TEAM("commands.player-not-in-team", false),
    COMMANDS_PLAYER_ALREADY_IN_TEAM("commands.player-already-in-team", false),

    COMMANDS_HELP_HEADER("commands.help.header", false),
    COMMANDS_HELP_LINE("commands.help.line", false),

    COMMANDS_EDIT_PLAYER_DISCONNECTED("commands.edit.player.disconnected-player", false),
    COMMANDS_TEAM_DESC("commands.team.desc", false),
    COMMANDS_TEAM_ALREADY_EXIST("commands.team.already-exist", false),
    COMMANDS_TEAM_DOES_NOT_EXIST("commands.team.does-not-exist", false),
    COMMANDS_TEAM_PART_OF_NO_TEAM("commands.team.part-of-no-team", false),
    COMMANDS_TEAM_FULL("commands.team.team-full", false),
    COMMANDS_TEAM_CHECK_DESC("commands.team.check.desc", false),
    COMMANDS_TEAM_CHECK_RESULT("commands.team.check.result", false),
    COMMANDS_TEAM_CHECK_RESULT_OTHER("commands.team.check.result-other", false),
    COMMANDS_TEAM_CHECK_RESULT_MEMBER_LINE("commands.team.check.result-member-line", false),
    COMMANDS_TEAM_CHECK_RESULT_RELATION_LINE("commands.team.check.result-relation-line", false),
    COMMANDS_TEAM_LIST_DESC("commands.team.list.desc", false),
    COMMANDS_TEAM_LIST_RESULT_HEADER("commands.team.list.result-header", false),
    COMMANDS_TEAM_LIST_RESULT_LINE("commands.team.list.result-line", false),
    COMMANDS_TEAM_INVITES_DESC("commands.team.invites.desc", false),
    COMMANDS_TEAM_INVITES_CHECK_DESC("commands.team.invites.check.desc", false),
    COMMANDS_TEAM_INVITES_CHECK_RESULT_HEADER("commands.team.invites.check.result-header", false),
    COMMANDS_TEAM_INVITES_CHECK_RESULT_LINE("commands.team.invites.check.result-line", false),
    COMMANDS_TEAM_INVITES_OPEN_DESC("commands.team.invites.open.desc", false),
    COMMANDS_TEAM_INVITES_OPEN_RESULT("commands.team.invites.open.result", false),
    COMMANDS_TEAM_INVITES_CLOSE_DESC("commands.team.invites.close.desc", false),
    COMMANDS_TEAM_INVITES_CLOSE_RESULT("commands.team.invites.close.result", false),
    COMMANDS_TEAM_CREATE_DESC("commands.team.create.desc", false),
    COMMANDS_TEAM_CREATE_RESULT("commands.team.create.result", false),
    COMMANDS_TEAM_CREATE_TOO_SHORT("commands.team.create.too-short", false),
    COMMANDS_TEAM_CREATE_TOO_LONG("commands.team.create.too-long", false),
    COMMANDS_TEAM_CREATE_NOT_ENOUGH_MONEY("commands.team.create.not-enough-money", false),
    COMMANDS_TEAM_DEPLOY_DESC("commands.team.deploy.desc", false),
    COMMANDS_TEAM_DEPLOY_TERRITORY("commands.team.deploy.result-territory", false),
    COMMANDS_TEAM_DEPLOY_REGION("commands.team.deploy.result-region", false),
    COMMANDS_TEAM_DEPLOY_FAILURE_TIMER("commands.team.deploy.result-failure-timer", false),
    COMMANDS_TEAM_DEPLOY_FAILURE_TERRITORY("commands.team.deploy.result-failure-territory", false),
    COMMANDS_TEAM_DEPLOY_FAILURE_REGION("commands.team.deploy.result-failure-region", false),
    COMMANDS_TEAM_DEPLOY_CANCELLED("commands.team.deploy.cancelled", false),

    COMMANDS_TEAM_JOIN_DESC("commands.team.join.desc", false),
    COMMANDS_TEAM_JOIN_RESULT("commands.team.join.result", false),
    COMMANDS_TEAM_JOIN_RESULT_UNINVITED("commands.team.join.result-uninvited", false),
    COMMANDS_TEAM_LEAVE_DESC("commands.team.leave.desc", false),
    COMMANDS_TEAM_LEAVE_RESULT("commands.team.leave.result", false),
    COMMANDS_TEAM_LEAVE_RESULT_FAILURE_OWNER("commands.team.leave.result-failure-owner", false),
    COMMANDS_TEAM_MANAGE_DESC("commands.team.manage.desc", false),
    COMMANDS_TEAM_MANAGE_ERROR_CLEARANCE("commands.team.manage.error-clearance", false),
    COMMANDS_TEAM_MANAGE_RESULT_NAME_CONFIRM("commands.team.manage.result-confirm-name", false),
    COMMANDS_TEAM_MANAGE_COLOR_DESC("commands.team.manage.color.desc", false),
    COMMANDS_TEAM_MANAGE_COLOR_RESULT("commands.team.manage.color.result", false),
    COMMANDS_TEAM_MANAGE_OPEN_DESC("commands.team.manage.open.desc", false),
    COMMANDS_TEAM_MANAGE_OPEN_RESULT("commands.team.manage.open.result", false),
    COMMANDS_TEAM_MANAGE_CLOSE_DESC("commands.team.manage.close.desc", false),
    COMMANDS_TEAM_MANAGE_CLOSE_RESULT("commands.team.manage.close.result", false),
    COMMANDS_TEAM_MANAGE_INVITE_DESC("commands.team.manage.invite.desc", false),
    COMMANDS_TEAM_MANAGE_INVITE_RESULT_SENT("commands.team.manage.invite.result-sent", false),
    COMMANDS_TEAM_MANAGE_INVITE_RESULT_ALREADY("commands.team.manage.invite.result-already", false),
    COMMANDS_TEAM_MANAGE_INVITE_RESULT_IGNORE("commands.team.manage.invite.result-ignore", false),
    COMMANDS_TEAM_MANAGE_MEMBER_DESC("commands.team.manage.member.desc", false),
    COMMANDS_TEAM_MANAGE_MEMBER_CANNOT_TARGET_SELF("commands.team.manage.member.cannot-target-self", false),
    COMMANDS_TEAM_MANAGE_MEMBER_KICK_DESC("commands.team.manage.member.kick.desc", false),
    COMMANDS_TEAM_MANAGE_MEMBER_KICK_RESULT("commands.team.manage.member.kick.result", false),
    COMMANDS_TEAM_MANAGE_MEMBER_PROMOTE_DESC("commands.team.manage.member.promote.desc", false),
    COMMANDS_TEAM_MANAGE_MEMBER_PROMOTE_RESULT("commands.team.manage.member.promote.result", false),
    COMMANDS_TEAM_MANAGE_MEMBER_PROMOTE_ERROR("commands.team.manage.member.promote.error", false),
    COMMANDS_TEAM_MANAGE_MEMBER_DEMOTE_DESC("commands.team.manage.member.demote.desc", false),
    COMMANDS_TEAM_MANAGE_MEMBER_DEMOTE_RESULT("commands.team.manage.member.demote.result", false),
    COMMANDS_TEAM_MANAGE_MEMBER_DEMOTE_ERROR("commands.team.manage.member.demote.error", false),
    COMMANDS_TEAM_MANAGE_MEMBER_TRANSFER_DESC("commands.team.manage.member.transfer.desc", false),
    COMMANDS_TEAM_MANAGE_MEMBER_TRANSFER_RESULT("commands.team.manage.member.transfer.result", false),
    COMMANDS_TEAM_MANAGE_RELATION_DESC("commands.team.manage.relation.desc", false),
    COMMANDS_TEAM_MANAGE_RELATION_OPEN_DESC("commands.team.manage.relation.open.desc", false),
    COMMANDS_TEAM_MANAGE_RELATION_OPEN_RESULT("commands.team.manage.relation.open.result", false),
    COMMANDS_TEAM_MANAGE_RELATION_CLOSE_DESC("commands.team.manage.relation.close.desc", false),
    COMMANDS_TEAM_MANAGE_RELATION_CLOSE_RESULT("commands.team.manage.relation.close.result", false),
    COMMANDS_TEAM_MANAGE_RELATION_INVITES_DESC("commands.team.manage.relation.invites.desc", false),
    COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_HEADER("commands.team.manage.relation.invites.result-header", false),
    COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_LINE("commands.team.manage.relation.invites.result-line", false),
    COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_LINE_RECEIVED("commands.team.manage.relation.invites.result-line-received", false),
    COMMANDS_TEAM_MANAGE_RELATION_INVITES_RESULT_LINE_SENT("commands.team.manage.relation.invites.result-line-sent", false),
    COMMANDS_TEAM_MANAGE_RELATION_ACCEPT_DESC("commands.team.manage.relation.accept.desc", false),
    COMMANDS_TEAM_MANAGE_RELATION_ACCEPT_NONE("commands.team.manage.relation.accept.none", false),
    COMMANDS_TEAM_MANAGE_RELATION_REQUEST_DESC("commands.team.manage.relation.request.desc", false),
    COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_SENT("commands.team.manage.relation.request.result-sent", false),
    COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_EFFECTIVE("commands.team.manage.relation.request.result-effective", false),
    COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_SAME_TEAM("commands.team.manage.relation.request.result-same-team", false),
    COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_BLOCKED("commands.team.manage.relation.request.result-blocked", false),
    COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_UNCHANGED("commands.team.manage.relation.request.result-unchanged", false),
    COMMANDS_TEAM_MANAGE_RELATION_REQUEST_RESULT_ALREADY_SENT("commands.team.manage.relation.request.result-already-sent", false),
    COMMANDS_TEAM_MANAGE_DISBAND_DESC("commands.team.manage.disband.desc", false),
    COMMANDS_TEAM_MANAGE_DISBAND_RESULT("commands.team.manage.disband.result", false),
    COMMANDS_TERRITORY_DESC("commands.territory.desc", false),
    COMMANDS_TERRITORY_CHECK_DESC("commands.territory.check.desc", false),
    COMMANDS_TERRITORY_CHECK_RESULT("commands.territory.check.result", false),
    COMMANDS_TERRITORY_CHECK_RESULT_NOT_ON_TERRITORY("commands.territory.check.result-not-on-territory", false),
    COMMANDS_TERRITORY_CHECK_RESULT_ON_MULTIPLE_TERRITORY("commands.territory.check.result-on-multiple-territory", false),

    COMMANDS_BATTLEFIELD_DESC("commands.battlefield.desc", false),
    COMMANDS_BATTLEFIELD_CHECK_DESC("commands.battlefield.check.desc", false),
    COMMANDS_BATTLEFIELD_CHECK_RESULT("commands.battlefield.check.result", false),
    COMMANDS_BATTLEFIELD_CHECK_RESULT_TERRITORY_LIST_LINE("commands.battlefield.check.result-territory-list-line", false),
    COMMANDS_BATTLEFIELD_LIST_DESC("commands.battlefield.list.desc", false),
    COMMANDS_BATTLEFIELD_LIST_RESULT_HEADER("commands.battlefield.list.result-header", false),
    COMMANDS_BATTLEFIELD_LIST_RESULT_LINE("commands.battlefield.list.result-line", false),

    COMMANDS_ADMIN_DESC("commands.admin.desc", false),
    COMMANDS_ADMIN_TEAM_DESC("commands.admin.team.desc", false),
    COMMANDS_ADMIN_TEAM_CREATE_DESC("commands.admin.team.create.desc", false),
    COMMANDS_ADMIN_TEAM_CREATE_RESULT("commands.admin.team.create.result", false),
    COMMANDS_ADMIN_TEAM_EDIT_DESC("commands.admin.team.edit.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_COLOR_DESC("commands.admin.team.edit.color.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_COLOR_RESULT("commands.admin.team.edit.color.result", false),
    COMMANDS_ADMIN_TEAM_EDIT_OPEN_DESC("commands.admin.team.edit.open.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_OPEN_RESULT("commands.admin.team.edit.open.result", false),
    COMMANDS_ADMIN_TEAM_EDIT_CLOSE_DESC("commands.admin.team.edit.close.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_CLOSE_RESULT("commands.admin.team.edit.close.result", false),
    COMMANDS_ADMIN_TEAM_EDIT_INVITE_DESC("commands.admin.team.edit.invite.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_INVITE_RESULT("commands.admin.team.edit.invite.result", false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_DESC("commands.admin.team.edit.member.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_ADD_DESC("commands.admin.team.edit.member.add.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_ADD_RESULT("commands.admin.team.edit.member.add.result", false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_REMOVE_DESC("commands.admin.team.edit.member.remove.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_REMOVE_RESULT("commands.admin.team.edit.member.remove.result", false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_PROMOTE_DESC("commands.admin.team.edit.member.promote.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_PROMOTE_RESULT("commands.admin.team.edit.member.promote.result", false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_DEMOTE_DESC("commands.admin.team.edit.member.demote.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_DEMOTE_RESULT("commands.admin.team.edit.member.demote.result", false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_TRANSFER_DESC("commands.admin.team.edit.member.transfer.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_MEMBER_TRANSFER_RESULT("commands.admin.team.edit.member.transfer.result", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_DESC("commands.admin.team.edit.relation.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_OPEN_DESC("commands.admin.team.edit.relation.open.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_OPEN_RESULT("commands.admin.team.edit.relation.open.result", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_CLOSE_DESC("commands.admin.team.edit.relation.close.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_CLOSE_RESULT("commands.admin.team.edit.relation.close.result", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_INVITES_DESC("commands.admin.team.edit.relation.request.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_INVITES_RESULT_HEADER("commands.admin.team.edit.relation.invites.result-header", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_INVITES_RESULT_LINE("commands.admin.team.edit.relation.invites.result-line", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_INVITES_RESULT_LINE_RECEIVED("commands.admin.team.edit.relation.invites.result-line-received", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_INVITES_RESULT_LINE_SENT("commands.admin.team.edit.relation.invites.result-line-sent", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_REQUEST_DESC("commands.admin.team.edit.relation.request.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_REQUEST_RESULT_INVITE("commands.admin.team.edit.relation.request.result-invite", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_REQUEST_RESULT_WEIGHT("commands.admin.team.edit.relation.request.result-weight", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_REQUEST_RESULT_IGNORE("commands.admin.team.edit.relation.request.result-ignore", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_REQUEST_RESULT_ALREADY("commands.admin.team.edit.relation.request.result-already", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_REQUEST_RESULT_UNCHANGED("commands.admin.team.edit.relation.request.result-unchanged", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_SET_DESC("commands.admin.team.edit.relation.set.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_RELATION_SET_RESULT("commands.admin.team.edit.relation.set.result", false),
    COMMANDS_ADMIN_TEAM_MANAGE_RELATION_SET_RESULT_UNCHANGED("commands.admin.team.edit.relation.set.result-unchanged", false),
    COMMANDS_ADMIN_TEAM_EDIT_DISBAND_DESC("commands.admin.team.edit.disband.desc", false),
    COMMANDS_ADMIN_TEAM_EDIT_DISBAND_RESULT("commands.admin.team.edit.disband.result", false),
    
    COMMANDS_ADMIN_TERRITORY_DESC("commands.admin.territory.desc", false),
    COMMANDS_ADMIN_TERRITORY_RELOAD_REGIONS_DESC("commands.admin.territory.reload-regions.desc", false),
    COMMANDS_ADMIN_TERRITORY_RELOAD_REGIONS_RESULT("commands.admin.territory.reload-regions.result", false),
    COMMANDS_ADMIN_TERRITORY_PROTECT_DESC("commands.admin.territory.protect.desc", false),
    COMMANDS_ADMIN_TERRITORY_PROTECT_RESULT("commands.admin.territory.protect.result", false),
    COMMANDS_ADMIN_TERRITORY_VULNERABLE_DESC("commands.admin.territory.vulnerable.desc", false),
    COMMANDS_ADMIN_TERRITORY_VULNERABLE_RESULT("commands.admin.territory.vulnerable.result", false),
    COMMANDS_ADMIN_TERRITORY_TEAM_DESC("commands.admin.territory.team.desc", false),
    COMMANDS_ADMIN_TERRITORY_TEAM_SET_DESC("commands.admin.territory.team.set.desc", false),
    COMMANDS_ADMIN_TERRITORY_TEAM_SET_RESULT("commands.admin.territory.team.set.result", false),
    COMMANDS_ADMIN_TERRITORY_TEAM_RESET_DESC("commands.admin.territory.team.reset.desc", false),
    COMMANDS_TERRITORY_TEAM_RESET_RESULT("commands.admin.territory.team.reset.result", false),

    STORAGE_TYPE("storage.type", false),
    FORMAT_DATETIME("format.datetime", false),
    FORMAT_DATETIME_NEVER("format.datetime-never", false),
    FORMAT_COUNTDOWN("format.countdown", false),

    PLAYER_ONLINE("player.online", false),
    PLAYER_OFFLINE("player.offline", false),

    TEAM_NONE_DISPLAY("team.none-display", false),
    TEAM_OPEN("team.open", false),
    TEAM_CLOSE("team.close", false),
    TEAM_RANK_OWNER("team.rank.owner", false),
    TEAM_RANK_LIEUTENANT("team.rank.lieutenant", false),
    TEAM_RANK_CAPTAIN("team.rank.captain", false),
    TEAM_RANK_MEMBER("team.rank.member", false),
    TEAM_RANK_RECRUIT("team.rank.recruit", false),
    TEAM_RELATION_NEUTRAL("team.relation.neutral", false),
    TEAM_RELATION_CONTROLLED("team.relation.controlled", false),
    TEAM_RELATION_TEAM("team.relation.team", false),
    TEAM_RELATION_ALLY("team.relation.ally", false),
    TEAM_RELATION_TRUCE("team.relation.truce", false),
    TEAM_RELATION_ENEMY("team.relation.enemy", false),

    BATTLEFIELD_OPEN("battlefield.open", false),
    BATTLEFIELD_CLOSED("battlefield.closed", false),
    BATTLEFIELD_ANNOUNCEMENT_OPEN("battlefield.announcement.open", false),
    BATTLEFIELD_ANNOUNCEMENT_OPEN_DELAY("battlefield.announcement.open-delay", false),
    BATTLEFIELD_ANNOUNCEMENT_CLOSE("battlefield.announcement.close", false),
    BATTLEFIELD_ANNOUNCEMENT_CLOSE_DELAY("battlefield.announcement.close-delay", false),

    TERRITORY_DESCRIPTION("territory.description", false),
    TERRITORY_BATTLE_STATUS_WAITING("territory.battle.status.waiting", false),
    TERRITORY_BATTLE_STATUS_ONGOING("territory.battle.status.ongoing", false),
    TERRITORY_BATTLE_STATUS_ENDING("territory.battle.status.ending", false),
    TERRITORY_BATTLE_STATUS_ENDED("territory.battle.status.ended", false),
    TERRITORY_BATTLE_STATUS_WAITING_SHORT("territory.battle.status.waiting-short", false),
    TERRITORY_BATTLE_STATUS_ONGOING_SHORT("territory.battle.status.ongoing-short", false),
    TERRITORY_BATTLE_STATUS_ENDING_SHORT("territory.battle.status.ending-short", false),
    TERRITORY_BATTLE_STATUS_ENDED_SHORT("territory.battle.status.ended-short", false),

    TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_START("territory.battle.alert.global.defend.start", false),
    TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_ADVANTAGE("territory.battle.alert.global.defend.advantage", false),
    TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_DISADVANTAGE("territory.battle.alert.global.defend.disadvantage", false),
    TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_VICTORY("territory.battle.alert.global.defend.victory", false),
    TERRITORY_BATTLE_ALERT_GLOBAL_DEFEND_DEFEAT("territory.battle.alert.global.defend.defeat", false),
    TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_START("territory.battle.alert.global.attack.start", false),
    TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_ADVANTAGE("territory.battle.alert.global.attack.advantage", false),
    TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_DISADVANTAGE("territory.battle.alert.global.attack.disadvantage", false),
    TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_VICTORY("territory.battle.alert.global.attack.victory", false),
    TERRITORY_BATTLE_ALERT_GLOBAL_ATTACK_DEFEAT("territory.battle.alert.global.attack.defeat", false),

    TERRITORY_ENTER("territory.enter", true),
    TERRITORY_LEAVE("territory.leave", true),
    TERRITORY_BOSSBAR_GLOBAL_WILD("territory.bossbar.global.wild", false),
    TERRITORY_BOSSBAR_GLOBAL_OCCUPIED("territory.bossbar.global.occupied", false),
    TERRITORY_BOSSBAR_GLOBAL_BATTLE("territory.bossbar.global.battle", false),
    TERRITORY_BOSSBAR_GLOBAL_BATTLE_ENDING("territory.bossbar.global.battle-ending", false),
    TERRITORY_BOSSBAR_GLOBAL_BATTLE_ENDED("territory.bossbar.global.battle-ended", false),
    TERRITORY_BOSSBAR_ARROW_NO_ADVANTAGE("territory.bossbar.arrow.no-advantage", false),
    TERRITORY_BOSSBAR_ARROW_ADVANTAGE_LEFT_TO_RIGHT("territory.bossbar.arrow.left-to-right", false),
    TERRITORY_BOSSBAR_ARROW_ADVANTAGE_RIGHT_TO_LEFT("territory.bossbar.arrow.right-to-left", false),
    TERRITORY_PROTECTED("territory.protected", false),
    TERRITORY_PROTECTED_SHORT("territory.protected-short", false),
    TERRITORY_VULNERABLE("territory.vulnerable", false),
    TERRITORY_VULNERABLE_SHORT("territory.vulnerable-short", false),

    WEBMAP_MARKER_SET_LABEL("webmap.marker-set-label", false),


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