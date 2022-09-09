
package acme.features.chef.delor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.artifact.Artifact;
import acme.entities.artifact.ArtifactType;
import acme.entities.delor.Delor;
import acme.entities.systemSetting.SystemSettings;
import acme.framework.components.models.Model;
import acme.framework.controllers.Errors;
import acme.framework.controllers.Request;
import acme.framework.datatypes.Money;
import acme.framework.services.AbstractCreateService;
import acme.roles.Chef;

@Service
public class DelorChefCreateService implements AbstractCreateService<Chef, Delor> {

	@Autowired
	protected DelorRepository repository;

	// AbstractCreateService<Patron, Patronage> interface ---------------------


	@Override
	public boolean authorise(final Request<Delor> request) {
		assert request != null;

		return true;
	}

	@Override
	public void bind(final Request<Delor> request, final Delor entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

		request.bind(entity, errors, "keylet", "subject", "explanation", "startPeriod", "finishPeriod", "income", "moreInfo");

		Model model;
		Artifact selectedArtifact;

		model = request.getModel();
		selectedArtifact = this.repository.findArtifactById(Integer.parseInt(model.getString("artifacts")));

		entity.setArtifact(selectedArtifact);

	}

	@Override
	public void unbind(final Request<Delor> request, final Delor entity, final Model model) {
		assert request != null;
		assert entity != null;
		assert model != null;

		List<Artifact> artifacts;

		artifacts = this.repository.findAllAbleArtifact(ArtifactType.INGREDIENT, request.getPrincipal().getActiveRoleId());

		request.unbind(entity, model, "keylet", "instantiationMoment", "subject", "explanation", "startPeriod", "finishPeriod", "income", "moreInfo");

		model.setAttribute("isNew", true);
		model.setAttribute("artifacts", artifacts);
	}

	@Override
	public Delor instantiate(final Request<Delor> request) {
		assert request != null;

		Delor result;

		result = new Delor();
		LocalDate localDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
		String formattedString = "000000:" + localDate.format(formatter);
		result.setKeylet(formattedString);
		result.setInstantiationMoment(Calendar.getInstance().getTime());

		return result;
	}

	@Override
	public void validate(final Request<Delor> request, final Delor entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

		entity.setInstantiationMoment(Calendar.getInstance().getTime());
		
		Calendar now = Calendar.getInstance();
		now.setTime(entity.getInstantiationMoment());
		now.add(Calendar.MONTH, 1);
		now.add(Calendar.MINUTE, -1);

		if (!errors.hasErrors("startPeriod")) {

			errors.state(request, entity.getStartPeriod().after(now.getTime()), "startPeriod", "chef.delor.error.month.startPeriod");
		}

		now = Calendar.getInstance();
		if (entity.getStartPeriod() != null) {
			now.setTime(entity.getStartPeriod());
		}
		now.add(Calendar.DAY_OF_YEAR, 7);
		now.add(Calendar.MINUTE, -1);
		

		if (!errors.hasErrors("finishPeriod")) {

			errors.state(request, entity.getFinishPeriod().after(now.getTime()), "finishPeriod", "chef.delor.error.week.finishPeriod");
		}

		Money money = entity.getIncome();
		final SystemSettings c = this.repository.findConfiguration();
		if (!errors.hasErrors("income")) {

			errors.state(request, money.getAmount() >= 0., "income", "chef.delor.error.income");

			errors.state(request, c.getAcceptedCurrencies().contains(money.getCurrency()), "income", "chef.delor.not-able-currency");
		}

	}

	@Override
	public void create(final Request<Delor> request, final Delor entity) {
		assert request != null;
		assert entity != null;

		entity.setInstantiationMoment(Calendar.getInstance().getTime());
		this.repository.save(entity);
	}

}
