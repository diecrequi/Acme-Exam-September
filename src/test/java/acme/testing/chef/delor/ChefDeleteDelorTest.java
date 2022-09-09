package acme.testing.chef.delor;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import acme.testing.TestHarness;

public class ChefDeleteDelorTest extends TestHarness{
	
	@ParameterizedTest
	@CsvFileSource(resources = "/chef/delor/create.csv", encoding = "utf-8", numLinesToSkip = 1)
	@Order(10)
	public void delete(final int recordIndex, final String keylet, final String request, final String income,
		final String initialDate, final String finishDate, final String url) {
		super.signIn("Chef1", "chef1");
		super.clickOnMenu("Chef", "Delors");
		super.checkListingExists();
		
		super.clickOnListingRecord(recordIndex);
		super.checkFormExists();
		super.clickOnSubmit("Delete");

		super.checkNotErrorsExist();

		super.signOut();
	}

}
