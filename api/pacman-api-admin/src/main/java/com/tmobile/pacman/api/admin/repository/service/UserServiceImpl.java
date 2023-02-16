package com.tmobile.pacman.api.admin.repository.service;

import static com.tmobile.pacman.api.admin.common.AdminConstants.UNEXPECTED_ERROR_OCCURRED;

import java.util.List;
import java.util.Map;

import com.tmobile.pacman.api.admin.domain.CognitoUserDetails;
import com.tmobile.pacman.api.admin.domain.CreateCognitoUserDetails;
import com.tmobile.pacman.api.admin.repository.model.CognitoUser;
import com.tmobile.pacman.api.admin.service.AmazonCognitoConnector;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.tmobile.pacman.api.admin.domain.UserDetails;
import com.tmobile.pacman.api.admin.exceptions.PacManException;
import com.tmobile.pacman.api.admin.repository.UserRepository;
import com.tmobile.pacman.api.admin.repository.UserRolesMappingRepository;
import com.tmobile.pacman.api.admin.repository.model.User;
import com.tmobile.pacman.api.admin.repository.model.UserPreferences;

/**
 * User Service Implementations
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserPreferencesService userPreferencesService;

	@Autowired
	private UserRolesMappingRepository userRolesMappingRepository;

	@Autowired
	private AmazonCognitoConnector amazonCognitoConnector;

	private static final Integer DEFAULT_CURSOR=1;
	private static final Integer DEFAULT_LIMIT=50;

	@Override
	public List<User> getAllLoginUsers() throws PacManException {
		return userRepository.findAll();
	}

	@Override
	public List<CognitoUser> getAllUsers(Integer cursor, Integer limit, String filter) throws PacManException {
		if(cursor==null){
			cursor=DEFAULT_CURSOR;
		}
		if(limit==null){
			limit=DEFAULT_LIMIT;
		}
		return amazonCognitoConnector.listAllUsers(cursor,limit,filter);
	}

	@Override
	public Map<String, Object> updateRoleMembership(String username, CognitoUserDetails details) {
		return amazonCognitoConnector.updateRoleMembership(username,details);
	}

	@Override
	public Object editStatusToUser(String username) throws PacManException {
		return amazonCognitoConnector.enableOrDisableUser(username);
	}

	@Override
	public Map<String, Object> createUser(CreateCognitoUserDetails details) throws PacManException {
		return amazonCognitoConnector.createUser(details);
	}

	@Override
	public Map<String, Object> deleteUser(String username) throws PacManException {
		return amazonCognitoConnector.removeUser(username);
	}


	@Override
	public UserDetails getUserByEmailId(final String emailId) throws PacManException {
		User user;
		if (emailId != null) {
			user = userRepository.findByEmailIgnoreCase(emailId);
			return fetchUserDetails(user, emailId);
		} else {
			throw new PacManException("Email Id cannot be empty");
		}
	}

	private UserDetails fetchUserDetails(User user, String emailId) throws PacManException {
		UserDetails userDetails = new UserDetails();
		try {
			if (user == null) {
				List<String> roles = Lists.newArrayList();
				roles.add("ROLE_USER");
				userDetails.setUserRoles(roles);
				userDetails.setUserName(StringUtils.EMPTY);
				userDetails.setUserId(StringUtils.EMPTY);
				if (emailId != null) {
					userDetails.setEmail(emailId);
				} else {
					userDetails.setEmail(StringUtils.EMPTY);
				}
				userDetails.setLastName(StringUtils.EMPTY);
				userDetails.setFirstName(StringUtils.EMPTY);
				userDetails.setDefaultAssetGroup("");
			} else {
				List<String[]> userRoles = userRolesMappingRepository.findAllUserRoleDetailsByUserIdIgnoreCase(user.getUserId());
				UserPreferences userPreferences = userPreferencesService.getUserPreferencesByNtId(user.getUserId());
				userDetails.setUserRoles(userRoles);
				userDetails.setUserName(user.getUserName());
				userDetails.setUserId(user.getUserId());
				userDetails.setLastName(user.getLastName());
				userDetails.setFirstName(user.getFirstName());
				userDetails.setEmail(user.getEmail());
				userDetails.setDefaultAssetGroup(userPreferences.getDefaultAssetGroup());
			}
		} catch (Exception exception) {
			throw new PacManException(UNEXPECTED_ERROR_OCCURRED);
		}
		return userDetails;
	}
}
