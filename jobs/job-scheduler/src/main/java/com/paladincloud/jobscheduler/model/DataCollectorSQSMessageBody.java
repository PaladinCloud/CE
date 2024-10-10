package com.paladincloud.jobscheduler.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class DataCollectorSQSMessageBody {
	private String jobName;
	private List<String> accounts;
	private String tenant_id;
	private String tenant_name;
	private String source;

}
