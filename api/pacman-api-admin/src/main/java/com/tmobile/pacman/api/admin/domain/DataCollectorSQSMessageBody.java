package com.tmobile.pacman.api.admin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class DataCollectorSQSMessageBody {
	private String jobName;
	private List<String> accounts;
	private String paladinCloudTenantId;
	private String source;

}
