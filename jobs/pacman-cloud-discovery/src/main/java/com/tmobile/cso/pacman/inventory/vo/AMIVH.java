package com.tmobile.cso.pacman.inventory.vo;

import java.util.List;

import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Tag;

public class AMIVH {
	
	/** AMI image */
	private Image image;
	
	/** BlockDeviceMapping */
	private List<BlockDeviceMapping> blockDeviceMapping;
	
	/** The tags. */
	private List<Tag> tags;

	public AMIVH(Image image, List<BlockDeviceMapping> blockDeviceMapping, List<Tag> tags) {
		super();
		this.image = image;
		this.blockDeviceMapping = blockDeviceMapping;
		this.tags = tags;
	}
	
	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public List<BlockDeviceMapping> getBlockDeviceMapping() {
		return blockDeviceMapping;
	}

	public void setBlockDeviceMapping(List<BlockDeviceMapping> blockDeviceMapping) {
		this.blockDeviceMapping = blockDeviceMapping;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	

	
}
