
package acme.features.chef.delor;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.delor.Delor;
import acme.entities.fineDish.FineDish;
import acme.entities.systemSetting.SystemSettings;
import acme.framework.components.models.Model;
import acme.framework.controllers.Errors;
import acme.framework.controllers.HttpMethod;
import acme.framework.controllers.Request;
import acme.framework.controllers.Response;
import acme.framework.datatypes.Money;
import acme.framework.helpers.PrincipalHelper;
import acme.framework.services.AbstractUpdateService;
import acme.roles.Chef;

@Service
public class DelorChefUpdateService implements AbstractUpdateService<Chef, Delor> {

	@Autowired
	protected DelorRepository repository;

	// AbstractUpdateService<Patron, Patronage> interface ---------------------


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

	}

	@Override
	public void unbind(final Request<Delor> request, final Delor entity, final Model model) {
		assert request != null;
		assert entity != null;
		assert model != null;

		request.unbind(entity, model, "keylet", "instantiationMoment", "subject", "explanation", "startPeriod", "finishPeriod", "income", "moreInfo");

		model.setAttribute("artifactId", entity.getArtifact().getId());
		model.setAttribute("artifactPublish", entity.getArtifact().isPublished());

	}

	@Override
	public Delor findOne(final Request<Delor> request) {
		assert request != null;

		Delor delor;
		int id;

		id = request.getModel().getInteger("id");
		delor = this.repository.findOneDelorById(id);

		return delor;
	}

	@Override
	public void validate(final Request<Delor> request, final Delor entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

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
	public void update(final Request<Delor> request, final Delor entity) {
		assert request != null;
		assert entity != null;

		this.repository.save(entity);
	}

	@Override
	public void onSuccess(final Request<Delor> request, final Response<Delor> response) {
		assert request != null;
		assert response != null;

		if (request.isMethod(HttpMethod.POST)) {
			PrincipalHelper.handleUpdate();
		}
	}

}
