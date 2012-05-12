package fr.ippon.tatami.web.rest;

import static fr.ippon.tatami.web.view.RestAPIConstants.USER_PREVIEW_REST;
import static fr.ippon.tatami.web.view.RestAPIConstants.USER_SEARCH_REST;
import static fr.ippon.tatami.web.view.RestAPIConstants.USER_SHOW_REST;
import static fr.ippon.tatami.web.view.RestAPIConstants.USER_STATS_REST;
import static fr.ippon.tatami.web.view.RestAPIConstants.USER_UPDATE_REST;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.ippon.tatami.domain.User;
import fr.ippon.tatami.domain.json.UserSearch;
import fr.ippon.tatami.exception.FunctionalException;
import fr.ippon.tatami.service.user.UserService;
import fr.ippon.tatami.web.json.view.UserView;

/**
 * 
 * @author Julien Dubois
 * @author DuyHai DOAN
 */
@Controller
public class UserController extends AbstractRestController
{

	private final Logger log = LoggerFactory.getLogger(UserController.class);

	private UserService userService;

	// /rest/usersStats/{login}
	@RequestMapping(value = USER_STATS_REST, method = RequestMethod.GET, produces = "application/json")
	public void getUserStats(@PathVariable("id") String login, HttpServletResponse response) throws FunctionalException
	{
		log.debug("REST request to get Profile : {}", login);
		this.writeWithView(userService.getUserByLogin(login), response, UserView.Stats.class);
	}

	// /rest/usersDetails/{login}
	@RequestMapping(value = USER_PREVIEW_REST, method = RequestMethod.GET, produces = "application/json")
	public void getUserPreview(@PathVariable("id") String login, HttpServletResponse response) throws FunctionalException
	{
		log.debug("REST request to get Details : {}", login);
		this.writeWithView(userService.getUserByLogin(login), response, UserView.Details.class);
	}

	// /rest/usersProfile/{login}
	@RequestMapping(value = USER_SHOW_REST, method = RequestMethod.GET, produces = "application/json")
	public void getUserShow(@PathVariable("id") String login, HttpServletResponse response) throws FunctionalException
	{
		log.debug("REST request to get profile of : {}", login);
		this.writeWithView(userService.getUserByLogin(login), response, UserView.Full.class);
	}

	// /rest/users/{login}
	@RequestMapping(value = USER_UPDATE_REST, method = RequestMethod.POST, consumes = "application/json")
	@ResponseBody
	public void updateUser(@Valid @RequestBody User user)
	{
		log.debug("REST request to update user : {}", user.getLogin());
		userService.updateUser(user);
	}

	// /rest/usersSearch
	@RequestMapping(value = USER_SEARCH_REST, method = RequestMethod.POST, consumes = "application/json")
	@ResponseBody
	public void searchUser(@Valid @RequestBody UserSearch userSearch, HttpServletResponse response) throws FunctionalException
	{
		log.debug("REST request to search for user with input {}", userSearch);
		this.writeWithView(userService.findUser(userSearch.getSearchString()), response, UserView.Minimum.class);
	}

	public void setUserService(UserService userService)
	{
		this.userService = userService;
	}
}
