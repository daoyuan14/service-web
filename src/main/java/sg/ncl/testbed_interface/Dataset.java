package sg.ncl.testbed_interface;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import sg.ncl.domain.DataAccessibility;
import sg.ncl.domain.DataVisibility;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Slf4j
public class Dataset implements Serializable {

	private Integer id;
    @NotEmpty
	private String name;
    @NotEmpty
	private String description;
	private String contributorId;
	private DataVisibility visibility;
	private DataAccessibility accessibility;
    private ZonedDateTime releasedDate;
	private List<DataResource> dataResources;
	private List<String> approvedUsers;

	private User2 contributor;
	
	public Dataset() {
        visibility = DataVisibility.PUBLIC;
        releasedDate = ZonedDateTime.now();
	    dataResources = new ArrayList<>();
	    approvedUsers = new ArrayList<>();
    }

    public void setAccessibility(DataAccessibility accessibility) {
        if (accessibility == null) {
            this.accessibility = DataAccessibility.OPEN;
        } else {
            this.accessibility = accessibility;
        }
    }

	public boolean isOpen() {
	    return accessibility == DataAccessibility.OPEN;
    }

	public boolean isPublic() {
	    return visibility == DataVisibility.PUBLIC;
    }

    public boolean isAccessible() {
        return isOpen();
    }

    public boolean isAccessible(String userId) {
        return isOpen() || contributorId.equals(userId) || isApprovedUser(userId);
    }

    public boolean isApprovedUser(String userId) {
        return approvedUsers.contains(userId);
    }

	public void addApprovedUser(String userId) {
		approvedUsers.add(userId);
	}

	public void addResource(DataResource dataResource) {
	    dataResources.add(dataResource);
    }

    public void addAccessibility(String accessibility) {
        if (accessibility.equals(DataAccessibility.OPEN.toString())) {
            setAccessibility(DataAccessibility.OPEN);
        } else if (accessibility.equals(DataAccessibility.RESTRICTED.toString())) {
            setAccessibility(DataAccessibility.RESTRICTED);
        }
    }

    public void addVisibility(String visibility) {
        if (visibility.equals(DataVisibility.PRIVATE.toString())) {
            setVisibility(DataVisibility.PRIVATE);
        } else if (visibility.equals(DataVisibility.PROTECTED.toString())) {
            setVisibility(DataVisibility.PROTECTED);
        } else if (visibility.equals(DataVisibility.PUBLIC.toString())) {
            setVisibility(DataVisibility.PUBLIC);
        }
    }

    public String getResourceIdsInArrayString() {
        List<String> idList = new ArrayList<>();
        dataResources.forEach(temp -> idList.add("\"" + temp.getId() + "\""));
        String ids = idList.toString();
        log.debug(ids);
        return ids;
    }

    public String getResourceUrisInArrayString() {
        List<String> uriList = new ArrayList<>();
        dataResources.forEach(temp -> uriList.add("\"" + temp.getUri() + "\""));
        String uris = uriList.toString();
        log.debug(uris);
        return uris;
    }

    public String getReleasedDateString() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM-d-yyyy");
        return releasedDate.format(format);
    }

}
