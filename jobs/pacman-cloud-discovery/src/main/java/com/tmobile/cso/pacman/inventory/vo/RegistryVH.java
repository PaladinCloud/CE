package com.tmobile.cso.pacman.inventory.vo;

import com.amazonaws.services.ecr.model.ImageDetail;
import com.amazonaws.services.ecr.model.Repository;
import java.util.List;

public class RegistryVH {

  private Repository repository;
  private List<ImageDetail> imageDetails;

  public Repository getRepository() {
    return repository;
  }

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  public List<ImageDetail> getImageDetails() {
    return imageDetails;
  }

  public void setImageDetails(List<ImageDetail> imageDetails) {
    this.imageDetails = imageDetails;
  }

  public RegistryVH()
  {

  }
  public RegistryVH(Repository repository, List<ImageDetail> imageDetails) {
    this.repository = repository;
    this.imageDetails = imageDetails;
  }
}
