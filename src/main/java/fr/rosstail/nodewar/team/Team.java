package fr.rosstail.nodewar.team;

public class Team {

    private TeamModel teamModel;
    public Team(String name, String display) {
        teamModel = new TeamModel(name, display);
    }

    public Team(TeamModel teamModel) {
        this.teamModel = teamModel;
    }

    public TeamModel getTeamModel() {
        return teamModel;
    }

    public void setTeamModel(TeamModel teamModel) {
        this.teamModel = teamModel;
    }
}
