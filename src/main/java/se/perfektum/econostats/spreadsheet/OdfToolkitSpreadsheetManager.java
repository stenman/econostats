package se.perfektum.econostats.spreadsheet;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.odftoolkit.simple.SpreadsheetDocument;
import se.perfektum.econostats.common.JsonUtils;
import se.perfektum.econostats.dao.AccountTransactionDao;
import se.perfektum.econostats.domain.AccountTransaction;
import se.perfektum.econostats.domain.PayeeFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO: This class needs to be refactored (and renamed)!

/**
 * A wrapper class that simply manages spreadsheet creation and saving the produced spreadsheet file.
 */
public class OdfToolkitSpreadsheetManager implements SpreadsheetManager {

    private SpreadsheetProcessor spreadsheetProcessor;
    private AccountTransactionDao accountTransactionDao;

    public OdfToolkitSpreadsheetManager(AccountTransactionDao accountTransactionDao, SpreadsheetProcessor spreadsheetProcessor) {
        this.accountTransactionDao = accountTransactionDao;
        this.spreadsheetProcessor = spreadsheetProcessor;
    }

    @Override
    public void createNewSpreadsheet(List<AccountTransaction> accountTransactions, List<PayeeFilter> payeeFilters) throws Exception {
        // *** NOTE: exclude from odf: all objects that do not exist in premade configuration list of names ***
        // 3. create spreadsheet
        SpreadsheetDocument doc = spreadsheetProcessor.createSpreadsheet(accountTransactions, payeeFilters);

        //TODO: path should be configurable!
        final File file = new File("c:/temp/testdata/simpleodf.ods");
        doc.save(file);
//        OOUtils.open(file); //TODO: find another way of opening odf files locally
    }

    @Override
    public List<PayeeFilter> syncPayeeFilters(List<PayeeFilter> localPayeeFilters, String transactions) throws IOException {

        //Get PayeeFilter from datastore
        List<PayeeFilter> dataStorePayeeFilters = JsonUtils.getJsonElement(PayeeFilter.class, transactions);

        List<PayeeFilter> result = Stream.concat(localPayeeFilters.stream(), dataStorePayeeFilters.stream()).distinct().collect(Collectors.toList());

        return result;
    }

    @Override
    public List<AccountTransaction> mergeAccountTransactions(List<AccountTransaction> importedAccountTransactions, String transactions) throws IOException {
        return null;
    }
}
