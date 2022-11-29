package com.tmobile.pacman.api.admin.repository.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.cloudwatchevents.model.DisableRuleRequest;
import com.amazonaws.services.cloudwatchevents.model.EnableRuleRequest;
import com.amazonaws.services.cloudwatchevents.model.ListRulesRequest;
import com.amazonaws.services.cloudwatchevents.model.ListRulesResult;
import com.amazonaws.services.cloudwatchevents.model.RuleState;
import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.config.PacmanConfiguration;
import com.tmobile.pacman.api.admin.exceptions.PacManException;
import com.tmobile.pacman.api.admin.repository.JobExecutionManagerRepository;
import com.tmobile.pacman.api.admin.repository.PolicyRepository;
import com.tmobile.pacman.api.admin.repository.model.JobExecutionManager;
import com.tmobile.pacman.api.admin.repository.model.Policy;
import com.tmobile.pacman.api.admin.service.AmazonClientBuilderService;

@Service
public class AdminService {
	
	private static final Logger log = LoggerFactory.getLogger(AdminService.class);
	
	@Autowired
	private PolicyRepository policyRepository;
	
	@Autowired
	private JobExecutionManagerRepository jobRepository;
	
	@Autowired
	private AmazonClientBuilderService amazonClient;
	
	@Autowired
	private PacmanConfiguration config;
	
	public String shutDownAlloperations(String operation, String job) throws PacManException {
		
		String nextToken = null;
		ListRulesResult listRulesResult ;
		List<String> policies = new ArrayList<>();
		do{
			listRulesResult =  amazonClient.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion()).listRules(new ListRulesRequest().withNextToken(nextToken));
			policies.addAll(listRulesResult.getRules().parallelStream().map(rule->rule.getName()).collect(Collectors.toList()));
			nextToken = listRulesResult.getNextToken();
		}while(nextToken!=null);
		
		if(operation.equals(AdminConstants.ENABLE)) {
			if(job.equals(AdminConstants.RULE)) {
				if(enableRules(policies)) {
					return "All Policies has been sucessfully enabled";
				}
			} else if(job.equals(AdminConstants.JOB)) {
				if(enableJobs(policies)) {
					return "All Jobs has been sucessfully enabled";
				}
			} else {
				if(enableRules(policies) && enableJobs(policies)) {
					return "All Policies and Jobs has been sucessfully enabled";
				}
			}
			throw new PacManException("Enabling operation failed");
		} else {
			if(job.equals(AdminConstants.RULE)) {
				if(disablePolicies(policies)) {
					return "All Policies has been sucessfully disabled";
				}
			} else if(job.equals(AdminConstants.JOB)) {
				if(disableJobs(policies)) {
					return "All Jobs has been sucessfully disabled";
				}
			} else {
				if(disablePolicies(policies) && disableJobs(policies)) {
					return "All Policies and Jobs has been sucessfully disabled";
				}
			}
			throw new PacManException("Disabling operation failed");
		}
	}
	
	private boolean disablePolicies(List<String> policiesUUID) {
		List<Policy> policies = policyRepository.findAll();
		try {
			for(Policy policy : policies) {
				if(policiesUUID.contains(policy.getPolicyUUID())) {
					amazonClient.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion())
						.disableRule(new DisableRuleRequest().withName(policy.getPolicyUUID()));
					policy.setStatus(RuleState.DISABLED.name());
					policyRepository.save(policy);
				}
			}
			return true;
		} catch(Exception e) {
			log.error("Error in disable policy",e);
			return false;
		}
		
	}
	
	private boolean disableJobs(List<String> rules) {
		List<JobExecutionManager> jobs = jobRepository.findAll();
		try {
			for(JobExecutionManager job : jobs) {
				if(rules.contains(job.getJobUUID())) {
					job.getJobUUID();
					amazonClient.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion())
						.disableRule(new DisableRuleRequest().withName(job.getJobUUID()));
					job.setStatus(RuleState.DISABLED.name());
					jobRepository.save(job);
				}
			}
			return true;
		} catch(Exception e) {
			log.error("Error in disable jobs",e);
			return false;
		}
	}
	
	private boolean enableRules(List<String> policyUUIDS) {
		List<Policy> policies = policyRepository.findAll();
		try {
			for(Policy policy : policies) {
				if(policyUUIDS.contains(policy.getPolicyUUID())) {
					amazonClient.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion())
							.enableRule(new EnableRuleRequest().withName(policy.getPolicyUUID()));
					policy.setStatus(RuleState.ENABLED.name());
					policyRepository.save(policy);
				}
			}
			return true;
		} catch(Exception e) {
			log.error("Error in enable Policy",e);
			return false;
		}
	}
	
	private boolean enableJobs(List<String> rules) {
		List<JobExecutionManager> jobs = jobRepository.findAll();
		try {
			for(JobExecutionManager job : jobs) {
				if(rules.contains(job.getJobUUID())) {
					amazonClient.getAmazonCloudWatchEvents(config.getRule().getLambda().getRegion())
					.enableRule(new EnableRuleRequest().withName(job.getJobUUID()));
					job.setStatus(RuleState.ENABLED.name());
					jobRepository.save(job);
				}
			}
			return true;
		} catch(Exception e) {
			log.error("Error in enable jobs",e);
			return false;
		}
	}

	public Map<String,String> statusOfSystem() throws PacManException{
		
		Map<String,String> status = new HashMap<>();
		try {
			List<Policy> policies = policyRepository.findAll();
			List<JobExecutionManager> jobs = jobRepository.findAll();
			
			boolean rulesEnabled = false;
			boolean jobsEnabled = false;
			
			for(Policy policy : policies) {
				if(policy.getStatus().equals(RuleState.ENABLED.name())) {
					rulesEnabled = true;
					break;
				}
			}
			
			for(JobExecutionManager job : jobs) {
				if(job.getStatus().equals(RuleState.ENABLED.name())) {
					jobsEnabled = true;
					break;
				}
			}
			
			if(rulesEnabled) {
				status.put("rule", RuleState.ENABLED.name());
			} else {
				status.put("rule", RuleState.DISABLED.name());
			}
			
			if(jobsEnabled) {
				status.put("job", RuleState.ENABLED.name());
			} else {
				status.put("job", RuleState.DISABLED.name());
			}
			return status;
		} catch(Exception e) {
			log.error("Error in fetching status of system",e);
			throw new PacManException("Error in fetching the status of system");
		}
	}

}
