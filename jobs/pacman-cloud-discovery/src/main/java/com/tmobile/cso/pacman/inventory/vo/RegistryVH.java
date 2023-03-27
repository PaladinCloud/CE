package com.tmobile.cso.pacman.inventory.vo;

import com.amazonaws.services.ecr.model.ImageDetail;
import com.amazonaws.services.ecr.model.Repository;

public class RegistryVH {

  private Repository repository;
  private ImageDetail imageDetail;

  public Repository getRepository() {
    return repository;
  }

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  public ImageDetail getImageDetail() {
    return imageDetail;
  }

  public void setImageDetail(ImageDetail imageDetail) {
    this.imageDetail = imageDetail;
  }

  public RegistryVH()
  {

  }
  public RegistryVH(Repository repository, ImageDetail imageDetail) {
    this.repository = repository;
    this.imageDetail = imageDetail;
  }
}
