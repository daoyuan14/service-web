package sg.ncl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import sg.ncl.testbed_interface.Dataset;
import sg.ncl.testbed_interface.Domain;
import sg.ncl.testbed_interface.Experiment;
import sg.ncl.testbed_interface.LoginForm;
import sg.ncl.testbed_interface.SignUpAccountDetailsForm;
import sg.ncl.testbed_interface.SignUpMergedForm;
import sg.ncl.testbed_interface.SignUpPersonalDetailsForm;
import sg.ncl.testbed_interface.Team;
import sg.ncl.testbed_interface.TeamPageJoinTeamForm;
import sg.ncl.testbed_interface.User;
import sg.ncl.testbed_interface.TeamPageApplyTeamForm;
import sg.ncl.testbed_interface.TeamPageInviteMemberForm;

/**
 * 
 * Spring Controller
 * Direct the views to appropriate locations and invoke the respective REST API
 * @author yeoteye
 * 
 */
@Controller
public class MainController {
    
	private final String SESSION_LOGGED_IN_USER_ID = "loggedInUserId";
    private final int ERROR_NO_SUCH_USER_ID = 0;
    private final static Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private final String host = "http://localhost:8080/";
    private int CURRENT_LOGGED_IN_USER_ID = ERROR_NO_SUCH_USER_ID;
    private boolean IS_USER_ADMIN = false;
    private TeamManager teamManager = TeamManager.getInstance();
    private UserManager userManager = UserManager.getInstance();
    private ExperimentManager experimentManager = ExperimentManager.getInstance();
    private DomainManager domainManager = DomainManager.getInstance();
    private DatasetManager datasetManager = DatasetManager.getInstance();
    
    private String SCENARIOS_DIR_PATH = "src/main/resources/scenarios";
    
    
    @RequestMapping(value="/", method=RequestMethod.GET)
    public String index(Model model) throws Exception {
        model.addAttribute("loginForm", new LoginForm());
        return "index";
    }
    
    @RequestMapping(value="/", method=RequestMethod.POST)
    public String loginSubmit(@ModelAttribute LoginForm loginForm, Model model, HttpSession session) throws Exception {
        // following is to test if form fields can be retrieved via user input
        // pretend as though this is a server side validation
    	
    	String inputEmail = loginForm.getLoginEmail();
    	int userId = userManager.getUserIdByEmail(inputEmail);
    	
        if (userManager.validateLoginDetails(loginForm.getLoginEmail(), loginForm.getLoginPassword()) == false) 
        {
            // case1: invalid login
            loginForm.setErrorMsg("Invalid email/password.");
            return "redirect:/";
        } 
        else if (userManager.isEmailVerified(loginForm.getLoginEmail()) == false) 
        {
            // case2: email address not validated
            model.addAttribute("emailAddress", loginForm.getLoginEmail());
            return "redirect:/email_not_validated";
        } 
        else if (teamManager.getApprovedTeams(userId) == 0 && teamManager.getJoinRequestTeamMap2(userId) != null) 
        {
        	// case3 
        	// user is not a team owner nor a team member
        	// user has request to join a team but has not been approved by the team owner
        	return "redirect:/join_application_awaiting_approval";
        } 
        else if (teamManager.getApprovedTeams(userId) == 0 && teamManager.getUnApprovedTeams(userId) > 0)
        {
            // case4: since it goes through case3, user must be applying for a team
        	// team approval under review
            // email address is supposed to be valid here
            return "redirect:/team_application_under_review";
        } 
        else 
        {
            // all validated
        	// user may have no team at this point due to rejected team application or join request
        	// must allow user to login so that user can apply again
            // set login
            CURRENT_LOGGED_IN_USER_ID = userManager.getUserIdByEmail(loginForm.getLoginEmail());
            IS_USER_ADMIN = userManager.isUserAdmin(CURRENT_LOGGED_IN_USER_ID);
            session.setAttribute("isUserAdmin", IS_USER_ADMIN);
            session.setAttribute(SESSION_LOGGED_IN_USER_ID, CURRENT_LOGGED_IN_USER_ID);
            return "redirect:/dashboard";
        }
    }
    
    @RequestMapping("/passwordreset")
    public String passwordreset(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "passwordreset";
    }
    
    @RequestMapping("/dashboard")
    public String dashboard(Model model) {
        return "dashboard";
    }
    
    @RequestMapping(value="/logout", method=RequestMethod.GET)
    public String logout(HttpSession session) {
        CURRENT_LOGGED_IN_USER_ID = ERROR_NO_SUCH_USER_ID;
        session.removeAttribute("isUserAdmin");
        session.removeAttribute(SESSION_LOGGED_IN_USER_ID);
        return "redirect:/";
    }
    
    //--------------------------Sign Up Page--------------------------
    
    @RequestMapping(value="/signup2", method=RequestMethod.GET)
    public String signup2(Model model) {
    	// TODO get each model data and put into relevant ones
    	model.addAttribute("loginForm", new LoginForm());
    	model.addAttribute("signUpMergedForm", new SignUpMergedForm());
    	return "signup2";
    }
    
    @RequestMapping(value="/signup2", method=RequestMethod.POST)
    public String validateDetails(@ModelAttribute("loginForm") LoginForm loginForm, @ModelAttribute("signUpMergedForm") SignUpMergedForm signUpMergedForm) {
    	// TODO get each model data and put into relevant ones
    	
    	// add to User model
    	User newUser = new User();
    	newUser.setEmail(signUpMergedForm.getEmail());
    	newUser.setPassword(signUpMergedForm.getPassword());
    	newUser.setConfirmPassword(signUpMergedForm.getPassword());
    	newUser.setRole("normal");
    	newUser.setEmailVerified(false);
    	newUser.setName(signUpMergedForm.getName());
    	newUser.setJobTitle(signUpMergedForm.getJobTitle());
    	newUser.setInstitution(signUpMergedForm.getInstitution());
    	newUser.setInstitutionAbbreviation(signUpMergedForm.getInstitutionAbbreviation());
    	newUser.setWebsite(signUpMergedForm.getWebsite());
    	newUser.setAddress1(signUpMergedForm.getAddress1());
    	newUser.setAddress2(signUpMergedForm.getAddress2());
    	newUser.setCountry(signUpMergedForm.getCountry());
    	newUser.setCity(signUpMergedForm.getCity());
    	newUser.setProvince(signUpMergedForm.getProvince());
    	newUser.setPostalCode(signUpMergedForm.getPostalCode());
    	userManager.addNewUser(newUser);
    	
    	int newGeneratedUserId = newUser.getUserId();
    	
    	// check if user chose create new team or join existing team by checking team name
    	String createNewTeamName = signUpMergedForm.getTeamName();
    	String joinNewTeamName = signUpMergedForm.getJoinTeamName();
    	
    	// System.out.println("New team name: " + createNewTeamName);
    	// System.out.println("Join existing team name: " + joinNewTeamName);
    	
    	if (createNewTeamName.isEmpty() == false) {
    		// System.out.println("apply for new team");
        	// add to team model
        	Team newTeam = new Team();
        	newTeam.setName(createNewTeamName);
        	newTeam.setDescription(signUpMergedForm.getTeamDescription());
        	newTeam.setWebsite(signUpMergedForm.getTeamDescription());
        	newTeam.setOrganizationType(signUpMergedForm.getTeamOrganizationType());
        	newTeam.setIsPublic(signUpMergedForm.getIsPublic());
        	newTeam.setTeamOwnerId(newGeneratedUserId);
        	newTeam.setIsApproved(false);
        	teamManager.addNewTeam(newTeam);
        	// redirect to application submitted
        	return "redirect:/team_application_submitted";
        	
    	} else if (joinNewTeamName.isEmpty() == false) {
    		// System.out.println("join existing new team");
        	// add user request to join team
            int teamId = teamManager.getTeamIdByTeamName(joinNewTeamName);
            teamManager.addJoinRequestTeamMap2(newGeneratedUserId, teamId, userManager.getUserById(newGeneratedUserId));
            // redirect to join request submitted
            return "redirect:/join_application_submitted";
            
    	} else {
    		// logic error not suppose to reach here
    		return "redirect:/signup2";
    	}
    }
    
    //--------------------------Account Settings Page--------------------------
    @RequestMapping(value="/account_settings", method=RequestMethod.GET)
    public String accountDetails(Model model, HttpSession session) {
    	User editUser = userManager.getUserById(getSessionIdOfLoggedInUser(session));
    	model.addAttribute("editUser", editUser);
        return "account_settings";
    }
    
    @RequestMapping(value="/account_settings", method=RequestMethod.POST)
    public String editAccountDetails(@ModelAttribute("editUser") User editUser, final RedirectAttributes redirectAttributes, HttpSession session) {
    	// Need to be this way to "edit" details
    	// If not, the form details will overwrite existing user's details
    	// TODO for email changes need to resend email confirmation
    	User originalUser = userManager.getUserById(getSessionIdOfLoggedInUser(session));
    	
    	String editedName = editUser.getName();
    	String editedPassword = editUser.getPassword();
    	String editedConfirmPassword = editUser.getConfirmPassword();
    	String editedJobTitle = editUser.getJobTitle();
    	String editedInstitution = editUser.getInstitution();
    	String editedInstitutionAbbreviation = editUser.getInstitutionAbbreviation();
    	String editedWebsite = editUser.getWebsite();
    	String editedAddress1 = editUser.getAddress1();
    	String editedAddress2 = editUser.getAddress2();
    	String editedCountry = editUser.getCountry();
    	String editedCity = editUser.getCity();
    	String editedProvince = editUser.getProvince();
    	String editedPostalCode = editUser.getPostalCode();
    	
    	if (originalUser.updateName(editedName) == true) {
        	redirectAttributes.addFlashAttribute("editName", "success");
    	}
    	
    	if (editedPassword.equals(editedConfirmPassword) == false) {
    		redirectAttributes.addFlashAttribute("editPasswordMismatch", "unsuccess");
    	} else if (originalUser.updatePassword(editedPassword) == true) {
    		redirectAttributes.addFlashAttribute("editPassword", "success");
    	} else {
    		redirectAttributes.addFlashAttribute("editPassword", "unsuccess");
    	}
    	
    	if (originalUser.updateJobTitle(editedJobTitle) == true) {
    		redirectAttributes.addFlashAttribute("editJobTitle", "success");
    	}
    	
    	if (originalUser.updateInstitution(editedInstitution) == true) {
    		redirectAttributes.addFlashAttribute("editInstitution", "success");
    	}
    	
    	if (originalUser.updateInstitutionAbbreviation(editedInstitutionAbbreviation) == true) {
    		redirectAttributes.addFlashAttribute("editInstitutionAbbreviation", "success");
    	}
    	
    	if (originalUser.updateWebsite(editedWebsite) == true) {
    		redirectAttributes.addFlashAttribute("editWebsite", "success");
    	}
    	
    	if (originalUser.updateAddress1(editedAddress1) == true) {
    		redirectAttributes.addFlashAttribute("editAddress1", "success");
    	}
    	
    	if (originalUser.updateAddress2(editedAddress2) == true) {
    		redirectAttributes.addFlashAttribute("editAddress2", "success");
    	}
    	
    	if (originalUser.updateCountry(editedCountry) == true) {
    		redirectAttributes.addFlashAttribute("editCountry", "success");
    	}
    	
    	if (originalUser.updateCity(editedCity) == true) {
    		redirectAttributes.addFlashAttribute("editCity", "success");
    	}
    	
    	if (originalUser.updateProvince(editedProvince) == true) {
    		redirectAttributes.addFlashAttribute("editProvince", "success");
    	}
    	
    	if (originalUser.updatePostalCode(editedPostalCode) == true) {
    		redirectAttributes.addFlashAttribute("editPostalCode", "success");
    	}
    	
    	userManager.updateUserDetails(originalUser);
        return "redirect:/account_settings";
    }
    
    //--------------------User Side Approve Members Page------------
    
    @RequestMapping("/approve_new_user")
    public String approveNewUser(Model model, HttpSession session) {
    	HashMap<Integer, Team> rv = new HashMap<Integer, Team>();
    	rv = teamManager.getTeamMapByTeamOwner(getSessionIdOfLoggedInUser(session));
    	boolean userHasAnyJoinRequest = hasAnyJoinRequest(rv);
    	model.addAttribute("teamMapOwnedByUser", rv);
    	model.addAttribute("userHasAnyJoinRequest", userHasAnyJoinRequest);
    	return "approve_new_user";
    }
    
    @RequestMapping("/approve_new_user/accept/{teamId}/{userId}")
    public String userSideAcceptJoinRequest(@PathVariable Integer teamId, @PathVariable Integer userId) {
        teamManager.acceptJoinRequest(userId, teamId);
        return "redirect:/approve_new_user";
    }
    
    @RequestMapping("/approve_new_user/reject/{teamId}/{userId}")
    public String userSideRejectJoinRequest(@PathVariable Integer teamId, @PathVariable Integer userId) {
        teamManager.rejectJoinRequest(userId, teamId);
        return "redirect:/approve_new_user";
    }
    
    //--------------------------Teams Page--------------------------
    
    @RequestMapping("/teams")
    public String teams(Model model, HttpSession session) {
    	int currentLoggedInUserId = getSessionIdOfLoggedInUser(session);
        model.addAttribute("infoMsg", teamManager.getInfoMsg());
        model.addAttribute("currentLoggedInUserId", currentLoggedInUserId);
        model.addAttribute("teamMap", teamManager.getTeamMap(currentLoggedInUserId));
        model.addAttribute("publicTeamMap", teamManager.getPublicTeamMap());
        model.addAttribute("invitedToParticipateMap2", teamManager.getInvitedToParticipateMap2(currentLoggedInUserId));
        model.addAttribute("joinRequestMap2", teamManager.getJoinRequestTeamMap2(currentLoggedInUserId));
        // REST Client Code
        // final String uri = host + "teams/?";
        // RestTemplate restTemplate = new RestTemplate();
        // TeamsList result = restTemplate.getForObject(uri, TeamsList.class);
        return "teams";
    }
    
    @RequestMapping("/accept_participation/{teamId}")
    public String acceptParticipationRequest(@PathVariable Integer teamId, Model model, HttpSession session) {
    	int currentLoggedInUserId = getSessionIdOfLoggedInUser(session);
        // get user's participation request list
        // add this user id to the requested list
        teamManager.acceptParticipationRequest(currentLoggedInUserId, teamId);
        // remove participation request since accepted
        teamManager.removeParticipationRequest(currentLoggedInUserId, teamId);
        
        // must get team name
        String teamName = teamManager.getTeamNameByTeamId(teamId);
        teamManager.setInfoMsg("You have just joined Team " + teamName + " !");
        
        return "redirect:/teams";
    }
    
    @RequestMapping("/ignore_participation/{teamId}")
    public String ignoreParticipationRequest(@PathVariable Integer teamId, Model model, HttpSession session) {
        // get user's participation request list
        // remove this user id from the requested list
        String teamName = teamManager.getTeamNameByTeamId(teamId);
        teamManager.ignoreParticipationRequest2(getSessionIdOfLoggedInUser(session), teamId);
        teamManager.setInfoMsg("You have just ignored a team request from Team " + teamName + " !");
        
        return "redirect:/teams";
    }
    
    @RequestMapping("/withdraw/{teamId}")
    public String withdrawnJoinRequest(@PathVariable Integer teamId, Model model, HttpSession session) {
        // get user team request
        // remove this user id from the user's request list
        String teamName = teamManager.getTeamNameByTeamId(teamId);
        teamManager.removeUserJoinRequest2(getSessionIdOfLoggedInUser(session), teamId);
        teamManager.setInfoMsg("You have withdrawn your join request for Team " + teamName);
        
        return "redirect:/teams";
    }
    
    @RequestMapping(value="/teams/invite_members/{teamId}", method=RequestMethod.GET)
    public String inviteMember(@PathVariable Integer teamId, Model model) {
        model.addAttribute("teamIdVar", teamId);
        model.addAttribute("teamPageInviteMemberForm", new TeamPageInviteMemberForm());
        return "team_page_invite_members";
    }
    
    @RequestMapping(value="/teams/invite_members/{teamId}", method=RequestMethod.POST)
    public String sendInvitation(@PathVariable Integer teamId, @ModelAttribute TeamPageInviteMemberForm teamPageInviteMemberForm,Model model) {
        int userId = userManager.getUserIdByEmail(teamPageInviteMemberForm.getInviteUserEmail());
        teamManager.addInvitedToParticipateMap(userId, teamId);
        return "redirect:/teams";
    }
    
    @RequestMapping(value="/teams/members_approval/{teamId}", method=RequestMethod.GET)
    public String membersApproval(@PathVariable Integer teamId, Model model) {
        model.addAttribute("team", teamManager.getTeamByTeamId(teamId));
        return "team_page_approve_members";
    }
    
    @RequestMapping("/teams/members_approval/accept/{teamId}/{userId}")
    public String acceptJoinRequest(@PathVariable Integer teamId, @PathVariable Integer userId) {
        teamManager.acceptJoinRequest(userId, teamId);
        return "redirect:/teams/members_approval/{teamId}";
    }
    
    @RequestMapping("/teams/members_approval/reject/{teamId}/{userId}")
    public String rejectJoinRequest(@PathVariable Integer teamId, @PathVariable Integer userId) {
        teamManager.rejectJoinRequest(userId, teamId);
        return "redirect:/teams/members_approval/{teamId}";
    }
    
    //--------------------------Team Profile Page--------------------------
    
    @RequestMapping("/team_profile/{teamId}")
    public String teamProfile(@PathVariable Integer teamId, Model model, HttpSession session) {
        model.addAttribute("currentLoggedInUserId", getSessionIdOfLoggedInUser(session));
        model.addAttribute("team", teamManager.getTeamByTeamId(teamId));
        model.addAttribute("membersMap", teamManager.getTeamByTeamId(teamId).getMembersMap());
        model.addAttribute("userManager", userManager);
        model.addAttribute("teamExpMap", experimentManager.getTeamExperimentsMap(teamId));
        return "team_profile";
    }
    
    @RequestMapping("/remove_member/{teamId}/{userId}")
    public String removeMember(@PathVariable Integer teamId, @PathVariable Integer userId, Model model) {
        // TODO check if user is indeed in the team
        // TODO what happens to active experiments of the user?
        // remove member from the team
        // reduce the team count
        teamManager.removeMembers(userId, teamId);
        return "redirect:/team_profile/{teamId}";
    }
    
    @RequestMapping("/team_profile/{teamId}/start_experiment/{expId}")
    public String startExperimentFromTeamProfile(@PathVariable Integer teamId, @PathVariable Integer expId, Model model, HttpSession session) {
        // start experiment
        // ensure experiment is stopped first before starting
        experimentManager.startExperiment(getSessionIdOfLoggedInUser(session), expId);
    	return "redirect:/team_profile/{teamId}";
    }
    
    @RequestMapping("/team_profile/{teamId}/stop_experiment/{expId}")
    public String stopExperimentFromTeamProfile(@PathVariable Integer teamId, @PathVariable Integer expId, Model model, HttpSession session) {
        // stop experiment
        // ensure experiment is in ready mode before stopping
        experimentManager.stopExperiment(getSessionIdOfLoggedInUser(session), expId);
        return "redirect:/team_profile/{teamId}";
    }
    
    @RequestMapping("/team_profile/{teamId}/remove_experiment/{expId}")
    public String removeExperimentFromTeamProfile(@PathVariable Integer teamId, @PathVariable Integer expId, Model model, HttpSession session) {
        // remove experiment
        // TODO check userid is indeed the experiment owner or team owner
        // ensure experiment is stopped first
        experimentManager.removeExperiment(getSessionIdOfLoggedInUser(session), expId);
        model.addAttribute("experimentList", experimentManager.getExperimentListByExperimentOwner(getSessionIdOfLoggedInUser(session)));
        // decrease exp count to be display on Teams page
        teamManager.decrementExperimentCount(teamId);
        return "redirect:/team_profile/{teamId}";
    }
    
    @RequestMapping(value="/team_profile/invite_user/{teamId}", method=RequestMethod.GET)
    public String inviteUserFromTeamProfile(@PathVariable Integer teamId, Model model) {
        model.addAttribute("teamIdVar", teamId);
        model.addAttribute("teamPageInviteMemberForm", new TeamPageInviteMemberForm());
        return "team_profile_invite_members";
    }
    
    @RequestMapping(value="/team_profile/invite_user/{teamId}", method=RequestMethod.POST)
    public String sendInvitationFromTeamProfile(@PathVariable Integer teamId, @ModelAttribute TeamPageInviteMemberForm teamPageInviteMemberForm, Model model) {
        int userId = userManager.getUserIdByEmail(teamPageInviteMemberForm.getInviteUserEmail());
        teamManager.addInvitedToParticipateMap(userId, teamId);
        return "redirect:/team_profile/{teamId}";
    }
    
    //--------------------------Apply for New Team Page--------------------------
    
    @RequestMapping(value="/teams/apply_team", method=RequestMethod.GET)
    public String teamPageApplyTeam(Model model) {
        model.addAttribute("teamPageApplyTeamForm", new TeamPageApplyTeamForm());
        return "team_page_apply_team";
    }
    
    @RequestMapping(value="/teams/apply_team", method=RequestMethod.POST)
    public String checkApplyTeamInfo(@Valid TeamPageApplyTeamForm teamPageApplyTeamForm, BindingResult bindingResult) {
       if (bindingResult.hasErrors()) {
           return "redirect:/team_page_apply_team";
       }
       // log data to ensure data has been parsed
       LOGGER.log(Level.INFO, "--------Apply for new team info---------");
       LOGGER.log(Level.INFO, teamPageApplyTeamForm.toString());
       return "redirect:/teams/team_application_submitted";
    }
    
    @RequestMapping(value="/team_owner_policy", method=RequestMethod.GET)
    public String teamOwnerPolicy() {
        return "team_owner_policy";
    }
    
    //--------------------------Join Team Page--------------------------
    
    @RequestMapping(value="/teams/join_team",  method=RequestMethod.GET)
    public String teamPageJoinTeam(Model model) {
        model.addAttribute("teamPageJoinTeamForm", new TeamPageJoinTeamForm());
        return "team_page_join_team";
    }
    
    @RequestMapping(value="/teams/join_team", method=RequestMethod.POST)
    public String checkJoinTeamInfo(@Valid TeamPageJoinTeamForm teamPageJoinForm, BindingResult bindingResult, Model model, HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "team_page_join_team";
        }
        // log data to ensure data has been parsed
        LOGGER.log(Level.INFO, "--------Join team---------");
        LOGGER.log(Level.INFO, teamPageJoinForm.toString());
        
        // perform join team request here
        // add to user join team list
        // ensure user is not already in the team or have submitted the application
        // add to team join request map also for members approval function
        User currentUser = userManager.getUserById(getSessionIdOfLoggedInUser(session));
        int teamId = teamManager.getTeamIdByTeamName(teamPageJoinForm.getTeamName());
        teamManager.addJoinRequestTeamMap2(getSessionIdOfLoggedInUser(session), teamId, currentUser);
        return "redirect:/teams/join_application_submitted/" + teamId;
    }
    
    //--------------------------Experiment Page--------------------------
    
    @RequestMapping(value="/experiments", method=RequestMethod.GET)
    public String experiments(Model model, HttpSession session) {
        model.addAttribute("teamManager", teamManager);
        model.addAttribute("experimentList", experimentManager.getExperimentListByExperimentOwner(getSessionIdOfLoggedInUser(session)));
        return "experiments";
    }
    
    @RequestMapping(value="/experiments/create", method=RequestMethod.GET)
    public String createExperiment(Model model, HttpSession session) {
    	List<String> scenarioFileNameList = getScenarioFileNameList();
        model.addAttribute("experiment", new Experiment());
        model.addAttribute("scenarioFileNameList", scenarioFileNameList);
        model.addAttribute("teamMap", teamManager.getTeamMap(getSessionIdOfLoggedInUser(session)));
        return "experiment_page_create_experiment";
    }
    
    @RequestMapping(value="/experiments/create", method=RequestMethod.POST)
    public String validateExperiment(@ModelAttribute Experiment experiment, Model model, HttpSession session) {
        model.addAttribute("teamMap", teamManager.getTeamMap(getSessionIdOfLoggedInUser(session)));
        // add current experiment to experiment manager
        experimentManager.addExperiment(getSessionIdOfLoggedInUser(session), experiment);
        // increase exp count to be display on Teams page
        teamManager.incrementExperimentCount(experiment.getTeamId());
        
        return "redirect:/experiments";
    }
    
    @RequestMapping("/experiments/configuration/{expId}")
    public String viewExperimentConfiguration(@PathVariable Integer expId, Model model) {
    	// get experiment from expid
    	// retrieve the scenario contents to be displayed
    	Experiment currExp = experimentManager.getExperimentByExpId(expId);
    	model.addAttribute("scenarioContents", currExp.getScenarioContents());
    	return "experiment_scenario_contents";
    }
    
    @RequestMapping("/remove_experiment/{expId}")
    public String removeExperiment(@PathVariable Integer expId, Model model, HttpSession session) {
        // remove experiment
        // TODO check userid is indeed the experiment owner or team owner
        // ensure experiment is stopped first
    	int teamId = experimentManager.getExperimentByExpId(expId).getTeamId();
        experimentManager.removeExperiment(getSessionIdOfLoggedInUser(session), expId);
        model.addAttribute("experimentList", experimentManager.getExperimentListByExperimentOwner(getSessionIdOfLoggedInUser(session)));
        
        // decrease exp count to be display on Teams page
        teamManager.decrementExperimentCount(teamId);
        return "redirect:/experiments";
    }
    
    @RequestMapping("/start_experiment/{expId}")
    public String startExperiment(@PathVariable Integer expId, Model model, HttpSession session) {
        // start experiment
        // ensure experiment is stopped first before starting
        experimentManager.startExperiment(getSessionIdOfLoggedInUser(session), expId);
        model.addAttribute("experimentList", experimentManager.getExperimentListByExperimentOwner(getSessionIdOfLoggedInUser(session)));
        return "redirect:/experiments";
    }
    
    @RequestMapping("/stop_experiment/{expId}")
    public String stopExperiment(@PathVariable Integer expId, Model model, HttpSession session) {
        // stop experiment
        // ensure experiment is in ready mode before stopping
        experimentManager.stopExperiment(getSessionIdOfLoggedInUser(session), expId);
        model.addAttribute("experimentList", experimentManager.getExperimentListByExperimentOwner(getSessionIdOfLoggedInUser(session)));
        return "redirect:/experiments";
    }
    
    //---------------------------------Dataset Page--------------------------
    
    @RequestMapping("/data")
    public String data(Model model, HttpSession session) {
    	model.addAttribute("datasetOwnedByUserList", datasetManager.getDatasetContributedByUser(getSessionIdOfLoggedInUser(session)));
    	model.addAttribute("datasetAccessibleByUserList", datasetManager.getDatasetAccessibleByuser(getSessionIdOfLoggedInUser(session)));
    	model.addAttribute("userManager", userManager);
    	return "data";
    }
    
    @RequestMapping(value="/data/contribute", method=RequestMethod.GET)
    public String contributeData(Model model) {
    	model.addAttribute("dataset", new Dataset());
    	return "contribute_data";
    }
    
    @RequestMapping(value="/data/contribute", method=RequestMethod.POST)
    public String validateContributeData(@ModelAttribute("dataset") Dataset dataset, HttpSession session) {
    	// TODO
    	// validation
    	// get file from user upload to server
    	datasetManager.addDataset(getSessionIdOfLoggedInUser(session), dataset);
    	return "redirect:/data";
    }
    
    @RequestMapping(value="/data/edit/{datasetId}", method=RequestMethod.GET)
    public String datasetInfo(@PathVariable Integer datasetId, Model model) {
    	Dataset dataset = datasetManager.getDataset(datasetId);
    	model.addAttribute("editDataset", dataset);
    	return "edit_data";
    }
    
    @RequestMapping(value="/data/edit/{datasetId}", method=RequestMethod.POST)
    public String editDatasetInfo(@PathVariable Integer datasetId, @ModelAttribute("editDataset") Dataset dataset, final RedirectAttributes redirectAttributes) {
    	Dataset origDataset = datasetManager.getDataset(datasetId);
    	
    	String editedDatasetName = dataset.getDatasetName();
    	String editedDatasetDesc = dataset.getDatasetDescription();
    	String editedDatasetLicense = dataset.getLicense();
    	String editedDatasetRestricted = dataset.getIsRestricted();
    	boolean editedDatasetIsRequiredAuthorization = dataset.getRequireAuthorization();
    	
    	System.out.println(origDataset.getDatasetId());
    	System.out.println(dataset.getDatasetId());
    	
    	if (origDataset.updateName(editedDatasetName) == true) {
    		redirectAttributes.addFlashAttribute("editName", "success");
    	}
    	
    	if (origDataset.updateDescription(editedDatasetDesc) == true) {
    		redirectAttributes.addFlashAttribute("editDesc", "success");
    	}
    	
    	if (origDataset.updateLicense(editedDatasetLicense) == true) {
    		redirectAttributes.addFlashAttribute("editLicense", "success");
    	}
    	
    	if (origDataset.updateRestricted(editedDatasetRestricted) == true) {
    		redirectAttributes.addFlashAttribute("editRestricted", "success");
    	}
    	
    	if (origDataset.updateAuthorization(editedDatasetIsRequiredAuthorization) == true) {
    		redirectAttributes.addFlashAttribute("editIsRequiredAuthorization", "success");
    	}
    	
    	datasetManager.updateDatasetDetails(origDataset);
    	
    	return "redirect:/data/edit/{datasetId}";
    }
    
    @RequestMapping("/data/remove_dataset/{datasetId}")
    public String removeDataset(@PathVariable Integer datasetId) {
    	datasetManager.removeDataset(datasetId);
    	return "redirect:/data";
    }
    
    @RequestMapping("/data/public")
    public String openDataset(Model model) {
    	model.addAttribute("publicDataMap", datasetManager.getDatasetMap());
    	model.addAttribute("userManager", userManager);
    	return "data_public";
    }
    
    @RequestMapping("/data/public/request_access/{dataOwnerId}")
    public String requestAccessForDataset(@PathVariable Integer dataOwnerId, Model model) {
    	// TODO
    	// send reuqest to team owner
    	// show feedback to users
    	User rv = userManager.getUserById(dataOwnerId);
    	model.addAttribute("ownerName", rv.getName());
    	model.addAttribute("ownerEmail", rv.getEmail());
    	return "data_request_access";
    }
    
    //---------------------------------Admin---------------------------------
    @RequestMapping("/admin")
    public String admin(Model model) {
    	model.addAttribute("domain", new Domain());
    	model.addAttribute("domainTable", domainManager.getDomainTable());
    	model.addAttribute("usersMap", userManager.getUserMap());
    	model.addAttribute("teamsMap", teamManager.getTeamMap());
    	model.addAttribute("teamManager", teamManager);
    	model.addAttribute("teamsPendingApprovalMap", teamManager.getTeamsPendingApproval());
    	model.addAttribute("experimentMap", experimentManager.getExperimentMap());
    	
    	model.addAttribute("totalTeamCount", teamManager.getTotalTeamsCount());
    	model.addAttribute("totalExpCount", experimentManager.getTotalExpCount());
    	model.addAttribute("totalMemberCount", teamManager.getTotalMembersCount());
    	model.addAttribute("totalMemberAwaitingApprovalCount", teamManager.getTotalMembersAwaitingApproval());
    	return "admin";
    }
    
    @RequestMapping(value="/admin/domains/add", method=RequestMethod.POST)
    public String addDomain(@Valid Domain domain, BindingResult bindingResult) {
    	if (bindingResult.hasErrors()) {
    		return "redirect:/admin";
    	} else {
    		domainManager.addDomains(domain.getDomainName());
    	}
    	return "redirect:/admin";
    }
    
    @RequestMapping("/admin/domains/remove/{domainKey}")
    public String removeDomain(@PathVariable String domainKey) {
    	domainManager.removeDomains(domainKey);
    	return "redirect:/admin";
    }
    
    @RequestMapping("/admin/teams/accept/{teamId}")
    public String approveTeam(@PathVariable Integer teamId) {
    	// set the approved flag to true
    	teamManager.approveTeamApplication(teamId);
    	return "redirect:/admin";
    }
    
    @RequestMapping("/admin/teams/reject/{teamId}")
    public String rejectTeam(@PathVariable Integer teamId) {
    	// need to cleanly remove the team application
    	teamManager.rejectTeamApplication(teamId);
    	return "redirect:/admin";
    }
    
    @RequestMapping("/admin/users/ban/{userId}")
    public String banUser(@PathVariable Integer userId) {
    	// TODO
    	// perform ban action here
    	// need to cleanly remove user info from teams, user. etc
    	return "redirect:/admin";
    }
    
    //--------------------------Static pages for teams--------------------------
    @RequestMapping("/teams/team_application_submitted")
    public String teamAppSubmitFromTeamsPage() {
        return "team_page_application_submitted";
    }
    
    @RequestMapping("/teams/join_application_submitted/{teamId}")
    public String teamAppJoinFromTeamsPage(@PathVariable Integer teamId, Model model) {
        int teamOwnerId = teamManager.getTeamByTeamId(teamId).getTeamOwnerId();
        model.addAttribute("teamOwner", userManager.getUserById(teamOwnerId));
        return "team_page_join_application_submitted";
    }
    
    //--------------------------Static pages for sign up--------------------------
    
    @RequestMapping("/team_application_submitted")
    public String teamAppSubmit(Model model) {
    	model.addAttribute("loginForm", new LoginForm());
    	model.addAttribute("signUpMergedForm", new SignUpMergedForm());
        return "team_application_submitted";
    }
    
    @RequestMapping("/join_application_submitted")
    public String joinTeamAppSubmit(Model model) {
    	model.addAttribute("loginForm", new LoginForm());
    	model.addAttribute("signUpMergedForm", new SignUpMergedForm());
        return "join_team_application_submitted";
    }
    
    @RequestMapping("/email_not_validated")
    public String emailNotValidated(Model model) {
    	model.addAttribute("loginForm", new LoginForm());
    	model.addAttribute("signUpMergedForm", new SignUpMergedForm());
        return "email_not_validated";
    }
    
    @RequestMapping("/team_application_under_review")
    public String teamAppUnderReview(Model model) {
    	model.addAttribute("loginForm", new LoginForm());
    	model.addAttribute("signUpMergedForm", new SignUpMergedForm());
        return "team_application_under_review";
    }
    
    @RequestMapping("/join_application_awaiting_approval")
    public String joinTeamAppAwaitingApproval(Model model) {
    	model.addAttribute("loginForm", new LoginForm());
    	model.addAttribute("signUpMergedForm", new SignUpMergedForm());
        return "join_team_application_awaiting_approval";
    }
    
    //--------------------------Get List of scenarios filenames--------------------------
    private List<String> getScenarioFileNameList() {
		List<String> scenarioFileNameList = new ArrayList<String>();
		File[] files = new File(SCENARIOS_DIR_PATH).listFiles();
		for (File file : files) {
			if (file.isFile()) {
				scenarioFileNameList.add(file.getName());
			}
		}
		return scenarioFileNameList;
    }
    
    //---Check if user is a team owner and has any join request waiting for approval----
    private boolean hasAnyJoinRequest(HashMap<Integer, Team> teamMapOwnedByUser) {
        for (Map.Entry<Integer, Team> entry : teamMapOwnedByUser.entrySet()) {
            Team currTeam = entry.getValue();
            if (currTeam.isUserJoinRequestEmpty() == false) {
            	// at least one team has join user request
            	return true;
            }
        }
        
        // loop through all teams but never return a single true
        // therefore, user's controlled teams has no join request
        return false;
    }
    
    //--------------------------MISC--------------------------
    public int getSessionIdOfLoggedInUser(HttpSession session) {
    	return Integer.parseInt(session.getAttribute(SESSION_LOGGED_IN_USER_ID).toString());
    }
}