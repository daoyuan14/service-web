package sg.ncl.testbed_interface;


/**
 * 
 * Experiment model
 * @author yeoteye
 * 
 */
public class Experiment {
    
    private int experimentId;
    private String name;
    private String description;
    private int experimentOwnerId;
    private int teamId;
    private String scenarioFileName;
    private String scenarioContents;
    private int nodesCount;
    private int hoursRunning;
    private int hoursIdle;
    private String status; // READY, ALLOCATING, ERROR
    
    public Experiment() {
    }
    
    public Experiment(int experimentId, String name, String description, int experimentOwnerId, int teamId, String scenarioFileName) {
        this.experimentId = experimentId;
        this.name = name;
        this.description = description;
        this.experimentOwnerId = experimentOwnerId;
        this.teamId = teamId;
        this.scenarioFileName = scenarioFileName;
    }
    
    public int getExperimentId() {
        return experimentId;
    }
    
    public void setExperimentId(int experimentId) {
        this.experimentId = experimentId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getExperimentOwnerId() {
        return experimentOwnerId;
    }
    
    public void setExperimentOwnerId(int experimentOwnerId) {
        this.experimentOwnerId = experimentOwnerId;
    }
    
    public int getTeamId() {
        return teamId;
    }
    
    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }
    
    public String getScenarioFileName() {
        return scenarioFileName;
    }
    
    public void setScenarioFileName(String scenarioFileName) {
        this.scenarioFileName = scenarioFileName;
    }

    public int getNodesCount() {
        return nodesCount;
    }

    public void setNodesCount(int nodesCount) {
        this.nodesCount = nodesCount;
    }

    public int getHoursRunning() {
        return hoursRunning;
    }

    public void setHoursRunning(int hoursRunning) {
        this.hoursRunning = hoursRunning;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getHoursIdle() {
        return hoursIdle;
    }

    public void setHoursIdle(int hoursIdle) {
        this.hoursIdle = hoursIdle;
    }

	public String getScenarioContents() {
		return scenarioContents;
	}

	public void setScenarioContents(String scenarioContents) {
		this.scenarioContents = scenarioContents;
	}
}
