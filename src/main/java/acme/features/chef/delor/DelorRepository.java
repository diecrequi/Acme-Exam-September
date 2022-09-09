package acme.features.chef.delor;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import acme.entities.artifact.Artifact;
import acme.entities.artifact.ArtifactType;
import acme.entities.delor.Delor;
import acme.entities.systemSetting.SystemSettings;
import acme.framework.repositories.AbstractRepository;

@Repository
public interface DelorRepository extends AbstractRepository{

	@Query("select artifact from Artifact artifact where artifact.id = :id")
	Artifact findArtifactById(int id);
	
	@Query("select p from Delor p where p.artifact.chef.id=:chefId")
	List<Delor> findAllDelorByChef(Integer chefId);
	
	@Query("select a from Artifact a where a.isPublished=false and a.type=:type and a.chef.id=:chefId and (select count(p) from Delor p where p.artifact=a)=0")
	List<Artifact> findAllAbleArtifact(ArtifactType type, Integer chefId);
	
	@Query("select s from SystemSettings s")
	SystemSettings findConfiguration();

	@Query("select p from Delor p where p.id=:id")
	Delor findOneDelorById(int id);
}
